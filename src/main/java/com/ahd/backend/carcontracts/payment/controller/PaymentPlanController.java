package com.ahd.backend.carcontracts.payment.controller;


import com.ahd.backend.carcontracts.payment.dto.*;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.service.PaymentPlanService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createPaymentPlan(
            @Valid @RequestBody PaymentPlanRequest request) {
        PaymentPlanResponse response = paymentPlanService.createPaymentPlan(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentPlan(@PathVariable Long id) {
        PaymentPlanResponse response = paymentPlanService.getPaymentPlan(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllPaymentPlans() {
        List<PaymentPlanResponse> response = paymentPlanService.getAllPaymentPlans();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentPlansByStatus(
            @PathVariable PaymentStatus status) {
        List<PaymentPlanResponse> response = paymentPlanService.getPaymentPlansByStatus(status);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/payments")
    public ResponseEntity<?> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentPlanService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/installments")
    public ResponseEntity<?> getInstallments(@PathVariable Long id) {
        List<InstallmentResponse> installments = paymentPlanService.getInstallments(id);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/installments/overdue")
    public ResponseEntity<?> getOverdueInstallments() {
        List<InstallmentResponse> overdueInstallments = paymentPlanService.getOverdueInstallments();
        return ResponseEntity.ok(overdueInstallments);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePaymentPlanStatus(
            @PathVariable Long id, @RequestParam PaymentStatus status) {
        PaymentPlanResponse response = paymentPlanService.updatePaymentPlanStatus(id, status);
        return ResponseEntity.ok(response);
    }

}
