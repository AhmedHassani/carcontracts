package com.ahd.backend.carcontracts.payment.controller;


import com.ahd.backend.carcontracts.payment.dto.*;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.service.PaymentPlanService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/payment")
@Validated
@Slf4j
public class PaymentPlanController {

    @Autowired
    private PaymentPlanService paymentPlanService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PAYMENT_PLAN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createPaymentPlan(
            @Valid @RequestBody PaymentPlanRequest request) {
        PaymentPlanResponse response = paymentPlanService.createPaymentPlan(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET_PAYMENT_PLAN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getPaymentPlan(@PathVariable Long id) {
        PaymentPlanResponse response = paymentPlanService.getPaymentPlan(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GET_PAYMENT_PLAN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllPaymentPlans() {
        List<PaymentPlanResponse> response = paymentPlanService.getAllPaymentPlans();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('GET_PAYMENT_PLAN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getPaymentPlansByStatus(
            @PathVariable PaymentStatus status) {
        List<PaymentPlanResponse> response = paymentPlanService.getPaymentPlansByStatus(status);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('UPDATE_PAYMENT_PLAN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentPlanService.processPayment(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/updateInstallmentDate")
    @PreAuthorize("hasAuthority('UPDATE_INSTALLMENT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePaymentDate(
            @Valid @RequestBody PaymentDateRequest request) {
        PaymentResponse response = paymentPlanService.updatePInstallmentDate(request);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}/updateInstallmentStatus")
    @PreAuthorize("hasAuthority('UPDATE_INSTALLMENT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id) {
        PaymentResponse response = paymentPlanService.updatePInstallmentStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/installments")
    @PreAuthorize("hasAuthority('GET_INSTALLMENT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getInstallments(@PathVariable Long id) {
        List<InstallmentResponse> installments = paymentPlanService.getInstallments(id);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/installments/overdue")
    @PreAuthorize("hasAuthority('GET_INSTALLMENT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getOverdueInstallments() {
        List<InstallmentResponse> overdueInstallments = paymentPlanService.getOverdueInstallments();
        return ResponseEntity.ok(overdueInstallments);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('UPDATE_PAYMENT_PLAN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePaymentPlanStatus(
            @PathVariable Long id, @RequestParam PaymentStatus status) {
        PaymentPlanResponse response = paymentPlanService.updatePaymentPlanStatus(id, status);
        return ResponseEntity.ok(response);
    }

}
