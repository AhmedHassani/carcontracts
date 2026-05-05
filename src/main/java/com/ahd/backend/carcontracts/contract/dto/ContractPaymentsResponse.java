package com.ahd.backend.carcontracts.contract.dto;

import com.ahd.backend.carcontracts.payment.model.Installment;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContractPaymentsResponse {
    private Long contractId;
    private String customerName;
    private String contractNumber;
    private String carName;
    private BigDecimal totalAmount;
    private BigDecimal downPayment;
    private BigDecimal remainingAmount;
    private String status;
    private LocalDate paymentPlanCreationDate;
    private String paymentType;
    private List<Installment> installments = new ArrayList<>();
}
