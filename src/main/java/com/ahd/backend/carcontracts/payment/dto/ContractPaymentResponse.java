package com.ahd.backend.carcontracts.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractPaymentResponse {
    private Long contractId;
    private String customerName;
    private String carType;
    private LocalDate contractDate;
    private BigDecimal totalAmount;
    private BigDecimal downPayment;
    private BigDecimal remainingAmount;
    private String paymentStatus;
    private List<InstallmentResponse> installments;
}
