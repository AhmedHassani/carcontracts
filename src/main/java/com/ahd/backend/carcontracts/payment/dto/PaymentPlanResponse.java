package com.ahd.backend.carcontracts.payment.dto;

import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentPlanResponse {
    private Long id;
    private PaymentType paymentType;
    private BigDecimal totalAmount;
    private BigDecimal downPayment;
    private BigDecimal remainingAmount;
    private Integer numberOfInstallments;
    private Integer installmentPeriodDays;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InstallmentResponse> installments;
}