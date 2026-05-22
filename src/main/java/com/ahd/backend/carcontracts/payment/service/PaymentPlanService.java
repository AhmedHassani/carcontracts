package com.ahd.backend.carcontracts.payment.service;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final NotificationSender notificationSender;
    private final MessageService messageService;

    @Auditable(operation = "انشاء خطة دفع", captureArgs = true, captureResult = true)
    public PaymentPlanResponse createPaymentPlan(PaymentPlanRequest request) {
        PaymentPlan paymentPlan = PaymentPlan.builder()
                .paymentType(request.getPaymentType())
                .totalAmount(request.getTotalAmount())
                .downPayment(request.getDownPayment() != null ? request.getDownPayment() : BigDecimal.ZERO)
                .installmentPeriodDays(request.getInstallmentPeriodDays())
                .companyId(getCompanyId())
                .status(PaymentStatus.PENDING)
                .installments(new ArrayList<>())
                .build();

        if (request.getPaymentType() == PaymentType.CASH) {
            System.out.println("CASH payment detected - setting status to COMPLETED");
            paymentPlan.setStatus(PaymentStatus.COMPLETED);
        }
        BigDecimal remainingAmount = paymentPlan.getTotalAmount().subtract(paymentPlan.getDownPayment());
        paymentPlan.setRemainingAmount(remainingAmount);
        if (request.getPaymentType() == PaymentType.INSTALLMENT) {
            paymentPlan.setIntInstallment(paymentPlan.getDownPayment());
            paymentPlan.setNumberOfInstallments(request.getNumberOfInstallments());
            PaymentPlan savedPlan = paymentPlanRepository.save(paymentPlan);
            List<Installment> installments = request.getInstallment().stream()
                    .map(installment -> {
                        installment.setPaymentPlan(savedPlan);
                        installment.setCompanyId(getCompanyId());
                        return installment;
                    })
                    .toList();
            savedPlan.setInstallments(installments);
        } else {
            paymentPlan.setNumberOfInstallments(1);
            paymentPlan = paymentPlanRepository.save(paymentPlan);
        }

        PaymentPlan savedPaymentPlan = paymentPlanRepository.findByIdAndCompanyIdWithInstallments(paymentPlan.getId(), getCompanyId())
                .orElse(paymentPlan);

        return mapToResponse(savedPaymentPlan);
    }

    public PaymentPlanResponse getPaymentPlan(Long id) {
        PaymentPlan paymentPlan = paymentPlanRepository.findByIdAndCompanyIdWithInstallments(id, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Payment plan not found with id: " + id));
        return mapToResponse(paymentPlan);
    }

    public List<InstallmentResponse> getInstallments(Long paymentPlanId) {
        List<Installment> installments = installmentRepository.findByPaymentPlanIdAndCompanyIdOrderByInstallmentNumber(paymentPlanId, getCompanyId());
        return installments.stream()
                .map(this::mapInstallmentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue installments and send notification
     */
    public List<InstallmentResponse> getOverdueInstallments() {
        List<Installment> overdueInstallments = installmentRepository.findOverdueInstallmentsByCompanyId(
                LocalDate.now(), InstallmentStatus.PENDING, getCompanyId());
        
        // Send notification if there are overdue installments
        if (overdueInstallments != null && !overdueInstallments.isEmpty()) {
            BigDecimal totalOverdueAmount = overdueInstallments.stream()
                    .map(Installment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Build detailed message
            StringBuilder detailsBuilder = new StringBuilder();
            for (Installment inst : overdueInstallments) {
                String contractNumber = getContractNumberByPaymentPlanId(inst.getPaymentPlan().getId());
                detailsBuilder.append("• القسط رقم ").append(inst.getInstallmentNumber())
                        .append(" (العقد: ").append(contractNumber != null ? contractNumber : "N/A")
                        .append("): ").append(inst.getAmount())
                        .append(" ريال - تاريخ الاستحقاق: ").append(inst.getDueDate()).append("\n");
            }
            
            String title = messageService.getMessage("notification.payment.installment.overdue.title");
            String message = messageService.getMessage("notification.payment.installment.overdue.body",
                    String.valueOf(overdueInstallments.size()),
                    totalOverdueAmount.toString()
            ) + "\n\n" + detailsBuilder.toString();
            
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("overdueCount", overdueInstallments.size());
            additionalData.put("totalOverdueAmount", totalOverdueAmount.toString());
            additionalData.put("checkDate", LocalDate.now().toString());
            
            NotificationContext context = NotificationContext.builder()
                    //.type("PAYMENT")
                    .operation("OVERDUE")
                    .title(title)
                    .message(message)
                    .actionType("OVERDUE_INSTALLMENTS")
                    .entityId(null)
                    .entityName("الأقساط المتأخرة")
                    .additionalData(additionalData)
                    .build();
            
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
        
        // Send notification for date update
        String contractNumber = getContractNumberByPaymentPlanId(installment.getPaymentPlan().getId());
        
        String title = messageService.getMessage("notification.payment.installment.date.update.title");
        String message = messageService.getMessage("notification.payment.installment.date.update.body",
                String.valueOf(installment.getInstallmentNumber()),
                contractNumber != null ? contractNumber : String.valueOf(installment.getPaymentPlan().getId()),
                oldDueDate != null ? oldDueDate.toString() : "غير محدد",
                request.getDueDate() != null ? request.getDueDate().toString() : "غير محدد"
        );
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("installmentId", installment.getId());
        additionalData.put("installmentNumber", installment.getInstallmentNumber());
        additionalData.put("paymentPlanId", installment.getPaymentPlan().getId());
        additionalData.put("oldDueDate", oldDueDate != null ? oldDueDate.toString() : "");
        additionalData.put("newDueDate", request.getDueDate() != null ? request.getDueDate().toString() : "");
        additionalData.put("contractNumber", contractNumber != null ? contractNumber : "");
        
        NotificationContext context = NotificationContext.builder()
                //.type("PAYMENT")type("PAYMENT")
                .operation("DATE_UPDATE")
                .title(title)
                .message(message)
                .actionType("INSTALLMENT_DATE_UPDATE")
                .entityId(installment.getId())
                .entityName("قسط رقم " + installment.getInstallmentNumber())
                .additionalData(additionalData)
                .build();
        
        notificationSender.notifyPaymentOperation(context);

        return PaymentResponse.builder()
                .success(true)
                .message("Installment date updated successfully")
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

        if (paidAmount.compareTo(currentRemainingAmount) > 0 && 
            installment.getStatus() == InstallmentStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("The amount is more than the remaining installment amount");
        }

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
        
        // Send notification for status update
        String contractNumber = getContractNumberByPaymentPlanId(paymentPlanId);
        String title;
        String message;
        String operation;
        
        if (installment.getStatus() == InstallmentStatus.PAID) {
            title = messageService.getMessage("notification.payment.installment.paid.title");
            message = messageService.getMessage("notification.payment.installment.paid.body",
                    String.valueOf(installment.getInstallmentNumber()),
                    contractNumber != null ? contractNumber : String.valueOf(paymentPlanId),
                    paidAmount.toString()
            );
            operation = "PAID";
        } else if (installment.getStatus() == InstallmentStatus.PARTIALLY_PAID) {
            title = messageService.getMessage("notification.payment.installment.partial.title");
            message = messageService.getMessage("notification.payment.installment.partial.body",
                    String.valueOf(installment.getInstallmentNumber()),
                    contractNumber != null ? contractNumber : String.valueOf(paymentPlanId),
                    paidAmount.toString(),
                    newRemainingAmount.toString()
            );
            operation = "PARTIAL";
        } else {
            title = messageService.getMessage("notification.payment.installment.update.title");
            message = messageService.getMessage("notification.payment.installment.update.body",
                    String.valueOf(installment.getInstallmentNumber()),
                    contractNumber != null ? contractNumber : String.valueOf(paymentPlanId),
                    installment.getAmount().toString(),
                    installment.getStatus().toString()
            );
            operation = "UPDATE";
        }
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("installmentId", installment.getId());
        additionalData.put("installmentNumber", installment.getInstallmentNumber());
        additionalData.put("paymentPlanId", paymentPlanId);
        additionalData.put("paidAmount", paidAmount.toString());
        additionalData.put("remainingAmount", newRemainingAmount.toString());
        additionalData.put("status", installment.getStatus().toString());
        additionalData.put("contractNumber", contractNumber != null ? contractNumber : "");
        
        NotificationContext context = NotificationContext.builder()
                //.type("PAYMENT")
                .operation(operation)
                .title(title)
                .message(message)
                .actionType("INSTALLMENT_" + operation)
                .entityId(installment.getId())
                .entityName("قسط رقم " + installment.getInstallmentNumber())
                .additionalData(additionalData)
                .build();
        
        notificationSender.notifyPaymentOperation(context);
        
        // Send completion notification if plan is fully paid
        if (allPaid) {
            String completionTitle = "✅ اكتمال خطة الدفع";
            String completionMessage = "تم اكتمال خطة الدفع رقم " + paymentPlanId + 
                    " للعقد " + (contractNumber != null ? contractNumber : "") +
                    " بالمبلغ الإجمالي " + paymentPlan.getTotalAmount() + " ريال";
            
            Map<String, Object> completionData = new HashMap<>();
            completionData.put("paymentPlanId", paymentPlanId);
            completionData.put("contractNumber", contractNumber != null ? contractNumber : "");
            completionData.put("totalAmount", paymentPlan.getTotalAmount().toString());
            
            NotificationContext completionContext = NotificationContext.builder()
                    //.type("PAYMENT")
                    .operation("PLAN_COMPLETED")
                    .title(completionTitle)
                    .message(completionMessage)
                    .actionType("PAYMENT_PLAN_COMPLETED")
                    .entityId(paymentPlanId)
                    .entityName("خطة دفع رقم " + paymentPlanId)
                    .additionalData(completionData)
                    .build();
            notificationSender.notifyPaymentOperation(completionContext);
        }

        return PaymentResponse.builder()
                .success(true)
                .message("Installment processed successfully")
                .paymentDate(LocalDate.now())
                .build();
    }

    /**
     * Helper method to get contract number by payment plan ID
     */
    private String getContractNumberByPaymentPlanId(Long paymentPlanId) {
        try {
            Contracts contract = contractsRepository.findByPaymentPlanId(paymentPlanId);
            return contract != null ? contract.getContractNumber() : null;
        } catch (Exception e) {
            log.warn("Could not find contract for payment plan: {}", paymentPlanId);
            return null;
        }
    }

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