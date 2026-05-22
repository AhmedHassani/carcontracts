package com.ahd.backend.carcontracts.contract.dto;


import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContractInstallmentsDto(
        Long contractId,
        String buyerName,
        String carName,
        BigDecimal totalAmountDue,       // e.g., 3,000,000 for installments 4,5,6
        String installmentNumbersLabel,  // e.g., "4,5,6 / 10"
        Integer totalInstallments,       // e.g., 10
        PaymentStatus status,            // PAID if all paid, else UNPAID
        List<InstallmentRowDto> details  // the child rows (4/10, 5/10, 6/10, ...)
) {

    /**
     * Build a grouped row (one per contract) from its detail rows.
     * All rows must belong to the same contract.
     */
    public static ContractInstallmentsDto fromDetails(List<InstallmentRowDto> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("details must not be null or empty");
        }

        // Use the first row as the source for invariant fields
        InstallmentRowDto head = details.get(0);
        Long contractId = head.contractId();
        String buyerName = head.buyerName();
        String carName = head.carName();
        Integer totalInstallments = head.totalInstallments();

        // Sum amounts
        BigDecimal totalAmount = details.stream()
                .map(InstallmentRowDto::amountDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build label like "4,5,6 / 10"
        String numbersLabel = details.stream()
                .map(InstallmentRowDto::installmentNo)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + " / " + totalInstallments;

        // If any installment is UNPAID => group is UNPAID
        boolean allPaid = details.stream().allMatch(d -> d.status() == PaymentStatus.ACTIVE);
        PaymentStatus groupStatus = allPaid ? PaymentStatus.ACTIVE : PaymentStatus.PENDING;

        return new ContractInstallmentsDto(
                contractId,
                buyerName,
                carName,
                totalAmount,
                numbersLabel,
                totalInstallments,
                groupStatus,
                details
        );
    }
}
