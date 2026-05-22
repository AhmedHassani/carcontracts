package com.ahd.backend.carcontracts.payment.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentRequest {
    @NotNull
    private Long installmentId;
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    private String paymentReference;
    private String notes;
}