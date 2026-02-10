package com.ahd.backend.carcontracts.payment.service;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.payment.dto.*;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.payment.model.Installment;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
//import java.math.RoundingMode;
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
    private final NotificationService notificationService;

    private final ContractsRepository contractsRepository;
    //the notfication in the contract
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

        PaymentPlan savedPaymentPlan = paymentPlanRepository.findByIdAndCompanyIdWithInstallments(paymentPlan.getId() , getCompanyId())
                .orElse(paymentPlan);

        return mapToResponse(savedPaymentPlan);
    }

    public PaymentPlanResponse getPaymentPlan(Long id) {
        PaymentPlan paymentPlan = paymentPlanRepository.findByIdAndCompanyIdWithInstallments(id , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Payment plan not found with id: " + id));
        return mapToResponse(paymentPlan);
    }

    public List<InstallmentResponse> getInstallments(Long paymentPlanId) {

        List<Installment> installments = installmentRepository.findByPaymentPlanIdAndCompanyIdOrderByInstallmentNumber(   paymentPlanId , getCompanyId());
        return installments.stream()
                .map(this::mapInstallmentToResponse)
                .collect(Collectors.toList());
    }


    public List<InstallmentResponse> getOverdueInstallments() {
          List<Installment> overdueInstallments = installmentRepository.findOverdueInstallmentsByCompanyId(
                LocalDate.now(), InstallmentStatus.PENDING ,  getCompanyId());
        return overdueInstallments.stream()
                .map(this::mapInstallmentToResponse)
                .collect(Collectors.toList());
    }
    //notification
    //request.getInstallmentId() قام المستخدم  ;helper.getCurrentUser.userName تحديث تاريخ الدفعة رقم
    //to all company
    @Auditable(operation = "تحديث تاريخ الدفعة", captureArgs = true, captureResult = true)
    public PaymentResponse updatePInstallmentDate(PaymentDateRequest request) {
        Installment installment = installmentRepository.findByIdAndCompanyId(request.getInstallmentId() , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Installment not found"));
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Installment already paid");
        }
        installment.setOldPaidDate(installment.getDueDate());
        installment.setDueDate(request.getDueDate());
        installment.setStatus(InstallmentStatus.OVERDUE);
        installmentRepository.save(installment);

        return PaymentResponse.builder()
                .success(true)
                .message("Installment processed successfully")
                .paymentDate(LocalDate.now())
                .build();
    }
    @Auditable(operation = "تحديث حالة الدفعة", captureArgs = true, captureResult = true)
    //notification
    //id قام المستخدم  ;helper.getCurrentUser.userName تحديث حاله الدفعة رقم
    //to all company
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

    // FIXED: Correct syntax for comparison
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
    if(installment.getPaidDate() == null){
        installment.setPaidDate(LocalDate.now());
    }
    installmentRepository.save(installment);

    // Update payment plan
    Long paymentPlanId = installment.getPaymentPlan().getId();
    PaymentPlan paymentPlan = paymentPlanRepository.findById(paymentPlanId)
            .orElseThrow(() -> new EntityNotFoundException("Payment plan not found"));

    // FIXED: Subtract paidAmount, not installment.getAmount()
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

    return PaymentResponse.builder()
            .success(true)
            .message("Installment processed successfully")
            .paymentDate(LocalDate.now())
            .build();
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
    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }
}

