package com.ahd.backend.carcontracts.payment.service;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
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

    @Auditable(operation = "CREATE_PAYMENT_PLAN", captureArgs = true, captureResult = true)
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
        BigDecimal remainingAmount = paymentPlan.getTotalAmount().subtract(paymentPlan.getDownPayment());
        paymentPlan.setRemainingAmount(remainingAmount);
        if (request.getPaymentType() == PaymentType.INSTALLMENT) {
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

    public List<PaymentPlanResponse> getAllPaymentPlans() {
        List<PaymentPlan> paymentPlans = paymentPlanRepository.findByCompanyId(getCompanyId());
        return paymentPlans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public List<PaymentPlanResponse> getPaymentPlansByStatus(PaymentStatus status) {

        List<PaymentPlan> paymentPlans = paymentPlanRepository.findByStatusAndCompanyId(status , getCompanyId());
        return paymentPlans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

    @Auditable(operation = "PROCESS_PAYMENT_PLAN", captureArgs = true, captureResult = true)
    public PaymentResponse processPayment(PaymentRequest request) {
        Installment installment = installmentRepository.findByIdAndCompanyId(request.getInstallmentId() , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Installment not found"));
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Installment already paid");
        }
        if (!installment.getAmount().equals(request.getAmount())) {
            throw new IllegalArgumentException("Payment amount does not match installment amount");
        }
        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaidDate(LocalDate.now());
        installment.setPaymentReference(request.getPaymentReference());
        installmentRepository.save(installment);
        notificationService.sendNotificationToDevice(
                " دفع قسط",
                "تم دفع القسط رقم" + installment.getId() + " بنجاح "
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("حذف عقد");
        notif.setBody("تم حذف العقد رقم " + installment.getId() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
        //    notif.setCompany(savedCompany);
        notif.setPermisson("CompanyUsers");
        notificationService.insertNotificationAsync(notif);
        checkAndUpdatePaymentPlanStatus(installment.getPaymentPlan());
        return PaymentResponse.builder()
                .success(true)
                .message("Payment processed successfully")
                .paymentReference(request.getPaymentReference())
                .paymentDate(LocalDate.now())
                .amount(request.getAmount())
                .build();
    }
    @Auditable(operation = "UPDATE_INSTALLMENT_DATE", captureArgs = true, captureResult = true)
    public PaymentResponse updatePInstallmentDate(PaymentDateRequest request) {
        Installment installment = installmentRepository.findByIdAndCompanyId(request.getInstallmentId() , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Installment not found"));
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Installment already paid");
        }

        installment.setDueDate(request.getDueDate());
        installmentRepository.save(installment);
        notificationService.sendNotificationToDevice(
                "تعديل عقد",
                "تم تغير تاريخ القسط رقم" + installment.getId() + " بنجاح "
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("تعديل عقد");
        notif.setBody("تم تغير تاريخ القسط رقم " + installment.getId() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
        //    notif.setCompany(savedCompany);
        notif.setPermisson("CompanyUsers");
        notificationService.insertNotificationAsync(notif);
        return PaymentResponse.builder()
                .success(true)
                .message("Installment processed successfully")
                .paymentDate(LocalDate.now())
                .build();
    }
    @Auditable(operation = "UPDATE_INSTALLMENT_STATUS", captureArgs = true, captureResult = true)
    public PaymentResponse updatePInstallmentStatus(Long id) {
        Installment installment = installmentRepository.findByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Installment not found"));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new IllegalStateException("Installment already paid");
        }

        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaidDate(LocalDate.now());

        installmentRepository.save(installment);

        Long paymentPlanId = installment.getPaymentPlan().getId();

        notificationService.sendNotificationToDevice(
                "دفع قسط",
                "تم دفع القسط رقم" + installment.getId() + " بنجاح "
        );

        AppNotification notif = new AppNotification();
        notif.setTitle("دفع قسط");
        notif.setBody("تم دفع القسط رقم " + installment.getId() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
        //    notif.setCompany(savedCompany);
        notif.setPermisson("CompanyUsers");
        notificationService.insertNotificationAsync(notif);
        boolean allPaid = installmentRepository
                .findByPaymentPlanIdAndCompanyId(paymentPlanId , getCompanyId())
                .stream()
                .allMatch(inst -> inst.getStatus() == InstallmentStatus.PAID);

        if (allPaid) {
            PaymentPlan paymentPlan = paymentPlanRepository.findById(paymentPlanId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment plan not found"));

            paymentPlan.setStatus(PaymentStatus.COMPLETED);
            paymentPlan.setComplete_date(LocalDate.now());
            paymentPlanRepository.save(paymentPlan);
        }

        return PaymentResponse.builder()
                .success(true)
                .message("Installment processed successfully")
                .paymentDate(LocalDate.now())
                .build();
    }
    @Auditable(operation = "UPDATE_PAYMENT_STATUS", captureArgs = true, captureResult = true)
    public PaymentPlanResponse updatePaymentPlanStatus(Long id, PaymentStatus status) {
        PaymentPlan paymentPlan = paymentPlanRepository.findByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Payment plan not found"));
        paymentPlan.setStatus(status);
        paymentPlan = paymentPlanRepository.save(paymentPlan);
        return mapToResponse(paymentPlan);
    }

    private void checkAndUpdatePaymentPlanStatus(PaymentPlan paymentPlan) {
        List<Installment> installments = installmentRepository.findByPaymentPlanIdAndCompanyId(paymentPlan.getId() , getCompanyId());
        boolean allPaid = installments.stream().allMatch(i -> i.getStatus() == InstallmentStatus.PAID);
        if (allPaid) {
            paymentPlan.setStatus(PaymentStatus.COMPLETED);
            paymentPlan.setComplete_date(LocalDate.now());
            paymentPlanRepository.save(paymentPlan);
            notificationService.sendNotificationToDevice(
                    "اكمال اقساط",
                    "تم اكمل جميع اقساط خطة الدفع رقم" + paymentPlan.getId() + " بنجاح "
            );

            AppNotification notif = new AppNotification();
            notif.setTitle("اكمال اقساط");
            notif.setBody("تم اكمل جميع اقساط خطة الدفع رقم" + paymentPlan.getId() + " بنجاح");
            notif.setNotificationDate(LocalDateTime.now());
            //    notif.setCompany(savedCompany);
            notif.setPermisson("CompanyUsers");
            notificationService.insertNotificationAsync(notif);
        } else if (paymentPlan.getStatus() == PaymentStatus.PENDING) {
            paymentPlan.setStatus(PaymentStatus.ACTIVE);
            paymentPlanRepository.save(paymentPlan);
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
                .build();
    }
    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }
}

