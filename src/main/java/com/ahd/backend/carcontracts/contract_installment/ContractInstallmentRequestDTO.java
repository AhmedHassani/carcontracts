package com.ahd.backend.carcontracts.contract_installment;

import com.ahd.backend.carcontracts.contract.Contract;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractInstallmentRequestDTO {

    private Long contractId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private BigInteger amountDue;
    private BigInteger amountPaid;
    private String status;
    private LocalDate paidAt;
    private LocalDateTime createdAt;
    private boolean deleted = false;

}
