package com.ahd.backend.carcontracts.contract_installment;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractInstallmentResponseDTO {
    private Long id;
    private Long contractId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private BigInteger amountDue;
    private BigInteger amountPaid;
    private String status;
    private LocalDate paidAt;
    private LocalDateTime createdAt;
    @Builder.Default
    private boolean deleted = false;

}