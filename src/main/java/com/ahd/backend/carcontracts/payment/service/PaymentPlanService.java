package com.ahd.backend.carcontracts.payment.service;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.ahd.backend.carcontracts.payment.dto.*;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.payment.model.Installment;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;
import com.ahd.backend.carcontracts.notification.service.NotificationSender;
import com.ahd.backend.carcontracts.notification.service.MessageService;
import com.ahd.backend.carcontracts.contract.model.Contract;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentPlanService {

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;
    private final Helper helper;

    @Autowired
    private InstallmentRepository installmentRepository;

    private final ContractsRepository contractsRepository;
    private final NotificationSender notificationSender;  // ✅ Add NotificationSender
    private final MessageService messageService;  // ✅ Add MessageService

    // ... (existing methods remain the same until getOverdueInstallments)

    /**
     * Get overdue installments and send notification
     */
    public List<InstallmentResponse> getOverdueInstallments() {
        List<Installment> overdueInstallments = installmentRepository.findOverdueInstallmentsByCompanyId(
                LocalDate.now(), InstallmentStatus.PENDING, getCompanyId());
        
        // ✅ Send notification if there are overdue installments
        if (overdueInstallments != null && !overdueInstallments.isEmpty()) {
            BigDecimal totalOverdueAmount = overdueInstallments.stream()
                .map(Installment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            NotificationContext context = notificationSender.createOverdueInstallmentsContext(
                overdueInstallments, totalOverdueAmount);
            notificationSender.notifyPaymentOperation(context);
        }
        
        return overdueInstallments.stream()
                .map(this::mapInstallmentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update installment due date with notification
     */
    @Auditable(operation = "تحديث تاريخ الدفعة", captureArgs = true, captureResult = true)
    public PaymentResponse updatePInstallmentDate(PaymentDateRequest request) {
        Installment installment = installmentRepository.findByIdAndCompanyId(request.getInstallmentId(), getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Installment not found"));
        
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Installment already paid");
        }
        
        // Store old due date for notification
        LocalDate oldDueDate = installment.getDueDate();
        
        if (installment.getOldPaidDate() == null) {
            installment.setOldPaidDate(installment.getDueDate());
        }
        
        installment.setNote(request.getNote());
        installment.setDueDate(request.getDueDate());
        installment.setStatus(InstallmentStatus.OVERDUE);
        installmentRepository.save(installment);
        
        // ✅ Send notification for date update
        String contractNumber = getContractNumberByPaymentPlanId(installment.getPaymentPlan().getId());
        NotificationContext context = notificationSender.createInstallmentDateContext(
            installment, oldDueDate, request.getDueDate(), contractNumber);
        notificationSender.notifyPaymentOperation(context);
        
        return PaymentResponse.builder()
                .success(true)
                .message("Installment processed successfully")
                .paymentDate(LocalDate.now())
                .build();
    }
    
    /**
     * Update installment status with notification
     */
    @Auditable(operation = "تحديث حالة الدفعة", captureArgs = true, captureResult = true)
    public PaymentResponse updateInstallmentStatus(Long id, BigDecimal paidAmount) {
        // Validate input
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Paid amount must be positive");
        }

        Installment installment = installmentRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Installment not found"));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Installment already paid");
        }

        // Get current remaining amount (handle null)
        BigDecimal currentRemainingAmount = installment.getRemainingAmount() != null 
                ? installment.getRemainingAmount() 
                : installment.getAmount();
        
        if (currentRemainingAmount == null) {
            throw new IllegalStateException("Installment amount is not set");
        }

        // Check if paid amount exceeds remaining amount for partially paid installments
        if (paidAmount.compareTo(currentRemainingAmount) > 0 && 
            installment.getStatus() == InstallmentStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("The amount is more than the remaining installment amount");
        }

        // Store old status for notification
        InstallmentStatus oldStatus = installment.getStatus();
        
        // Calculate new remaining amount
        BigDecimal newRemainingAmount = currentRemainingAmount.subtract(paidAmount);
        
        // Update status based on payment
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            // Fully paid
            installment.setStatus(InstallmentStatus.PAID);
            installment.setRemainingAmount(BigDecimal.ZERO);
        } else {
            // Partially paid
            installment.setStatus(InstallmentStatus.PARTIALLY_PAID);
            installment.setRemainingAmount(newRemainingAmount);
        }
        
        if (installment.getPaidDate() == null) {
            installment.setPaidDate(LocalDate.now());
        }
        
        installmentRepository.save(installment);

        // Update payment plan
        Long paymentPlanId = installment.getPaymentPlan().getId();
        PaymentPlan paymentPlan = paymentPlanRepository.findById(paymentPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Payment plan not found"));

        BigDecimal planRemainingAmount = paymentPlan.getRemainingAmount() != null 
                ? paymentPlan.getRemainingAmount() 
                : BigDecimal.ZERO;
        BigDecimal newPlanRemainingAmount = planRemainingAmount.subtract(paidAmount);
        
        if (newPlanRemainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            newPlanRemainingAmount = BigDecimal.ZERO;
        }
        
        paymentPlan.setRemainingAmount(newPlanRemainingAmount);

        // Check if all installments are paid
        boolean allPaid = installmentRepository
                .findByPaymentPlanIdAndCompanyId(paymentPlanId, getCompanyId())
                .stream()
                .allMatch(inst -> inst.getStatus() == InstallmentStatus.PAID);

        if (allPaid) {
            paymentPlan.setStatus(PaymentStatus.COMPLETED);
            paymentPlan.setComplete_date(LocalDate.now());
            
            var contract = contractsRepository.findByPaymentPlanId(paymentPlan.getId());
            if (contract != null && contract.getCar() != null) {
                contract.getCar().setStatus("Paid");
                contractsRepository.save(contract);
            }
        } else {
            paymentPlan.setStatus(PaymentStatus.PENDING);
        }

        paymentPlanRepository.save(paymentPlan);
        
        // ✅ Send notification for status update
        String contractNumber = getContractNumberByPaymentPlanId(paymentPlanId);
        NotificationContext context = notificationSender.createInstallmentStatusContext(
            installment, paidAmount, contractNumber);
        notificationSender.notifyPaymentOperation(context);
        
        // ✅ Also send notification if this payment completed the entire plan
        if (allPaid) {
            NotificationContext completionContext = NotificationContext.builder()
                .operation("PLAN_COMPLETED")
                .title("✅ اكتمال خطة الدفع")
                .message(messageService.getMessage("notification.payment.plan.completed",
                    String.valueOf(paymentPlan.getId()),
                    contractNumber != null ? contractNumber : "",
                    paymentPlan.getTotalAmount() != null ? paymentPlan.getTotalAmount().toString() : "0"))
                .actionType("PAYMENT_PLAN_COMPLETED")
                .entity(paymentPlan)
                .entityId(paymentPlan.getId())
                .entityName("خطة دفع رقم " + paymentPlan.getId())
                .additionalData(Map.of(
                    "paymentPlanId", String.valueOf(paymentPlan.getId()),
                    "contractNumber", contractNumber != null ? contractNumber : "",
                    "totalAmount", paymentPlan.getTotalAmount() != null ? paymentPlan.getTotalAmount().toString() : "0"
                ))
                .build();
            notificationSender.notifyPaymentOperation(completionContext);
        }

        return PaymentResponse.builder()
                .success(true)
                .message("Installment processed successfully")
                .paymentDate(LocalDate.now())
                .build();
    }

    // Helper method to get contract number by payment plan ID
    private String getContractNumberByPaymentPlanId(Long paymentPlanId) {
        try {
            Contract contract = contractsRepository.findByPaymentPlanId(paymentPlanId);
            return contract != null ? contract.getContractNumber() : null;
        } catch (Exception e) {
            log.warn("Could not find contract for payment plan: {}", paymentPlanId);
            return null;
        }
    }

    // ... (rest of your existing methods remain the same)
    
    private PaymentPlanResponse mapToResponse(PaymentPlan paymentPlan) {
        List<InstallmentResponse> installmentResponses = paymentPlan.getInstallments().stream()
                .map(this::mapInstallmentToResponse)
                .collect(Collectors.toList());
        return PaymentPlanResponse.builder()
                .id(paymentPlan.getId())
                .paymentType(paymentPlan.getPaymentType())
                .totalAmount(paymentPlan.getTotalAmount())
                .downPayment(paymentPlan.getDownPayment())
                .remainingAmount(paymentPlan.getRemainingAmount())
                .numberOfInstallments(paymentPlan.getNumberOfInstallments())
                .installmentPeriodDays(paymentPlan.getInstallmentPeriodDays())
                .status(paymentPlan.getStatus())
                .createdAt(paymentPlan.getCreatedAt())
                .updatedAt(paymentPlan.getUpdatedAt())
                .installments(installmentResponses)
                .build();
    }

    private InstallmentResponse mapInstallmentToResponse(Installment installment) {
        return InstallmentResponse.builder()
                .id(installment.getId())
                .installmentNumber(installment.getInstallmentNumber())
                .amount(installment.getAmount())
                .dueDate(installment.getDueDate())
                .paidDate(installment.getPaidDate())
                .status(installment.getStatus())
                .paymentReference(installment.getPaymentReference())
                .oldPaidDate(installment.getOldPaidDate())
                .build();
    }
    
    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
}