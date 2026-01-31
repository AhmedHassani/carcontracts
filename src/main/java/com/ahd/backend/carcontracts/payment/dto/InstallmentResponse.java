package com.ahd.backend.carcontracts.payment.dto;

import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class InstallmentResponse {
    private Long id;
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private LocalDate oldPaidDate;

    private InstallmentStatus status;
    private String paymentReference;

}