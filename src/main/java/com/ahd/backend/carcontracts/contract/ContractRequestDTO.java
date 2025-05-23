package com.ahd.backend.carcontracts.contract;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

@Data
public class ContractRequestDTO {

    private String contractNumber;
    private LocalDate contractDate;
    private Long carId;
    private Long sellerId;
    private Long buyerId;
    private Long branchId;
    private String saleType;
    private BigInteger totalAmount;
    private BigInteger amountPaid;
    private String paymentMethod;
    private String paymentStatus;
    private Long createdBy;
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted = false;
}
