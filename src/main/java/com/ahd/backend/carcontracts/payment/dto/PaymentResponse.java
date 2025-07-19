package com.ahd.backend.carcontracts.payment.dto;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private String message;
    private String paymentReference;
    private LocalDate paymentDate;
    private BigDecimal amount;
}