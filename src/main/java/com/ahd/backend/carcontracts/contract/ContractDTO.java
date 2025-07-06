package com.ahd.backend.carcontracts.contract;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class ContractDTO {
    private String contractNumber;
    private LocalDate contractDate;

    private Long carId;
    private Long sellerId;
    private Long buyerId;
    private Long branchId;

    private String saleType;
    private BigInteger totalAmount;
    private BigInteger amountPaid;
    private int installmentAmount;
    private String paymentMethod;
    private String paymentStatus;
    private Long createdBy;
    private Long daysAmountBetweenInstallments;
}
