package com.ahd.backend.carcontracts.contract.dto;


import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record InstallmentRowDto(
        Long contractId,
        Long installmentId,          // optional if you have an Installment entity
        String buyerName,
        String carName,
        BigDecimal amountDue,
        Integer installmentNo,       // e.g., 4
        Integer totalInstallments,   // e.g., 10
        LocalDate dueDate,
        PaymentStatus status
) {
    public static InstallmentRowDto of(Long contractId,
                                       Long installmentId,
                                       String buyerName,
                                       String carName,
                                       BigDecimal amountDue,
                                       Integer installmentNo,
                                       Integer totalInstallments,
                                       LocalDate dueDate,
                                       PaymentStatus status) {
        return new InstallmentRowDto(
                contractId,
                installmentId,
                buyerName,
                carName,
                amountDue,
                installmentNo,
                totalInstallments,
                dueDate,
                status
        );
    }
}
