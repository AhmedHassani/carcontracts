package com.ahd.backend.carcontracts.contract;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
public class ContractResponseDTO {

    private Long id;
    private String contractNumber;
    private LocalDate contractDate;
    private Long carId;
    private Long sellerId;
    private Long buyerId;
    private Long branchId;
    private int installmentAmount;
    private Long daysAmountBetweenInstallments;
    private String saleType;
    private BigInteger totalAmount;
    private BigInteger amountPaid;
    private String paymentMethod;
    private String paymentStatus;
    private Long createdBy;
    private Date createdAt;
    private Date updatedAt;
    @Builder.Default
    private boolean deleted = false;
}