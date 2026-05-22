package com.ahd.backend.carcontracts.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentDateRequest {
    @NotNull
    private Long installmentId;
    @NotNull
    private LocalDate dueDate;
    private String note;
}
