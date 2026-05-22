package com.ahd.backend.carcontracts.payment.dto;

import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.payment.model.Installment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class PaymentPlanRequest {
    @NotNull
    private PaymentType paymentType;
    @NotNull
    private Long companyid;
    @NotNull
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.00", message = "Down payment cannot be negative")
    private BigDecimal downPayment;

    @Min(value = 1, message = "Number of installments must be at least 1")
    private Integer numberOfInstallments;

    @Min(value = 1, message = "Installment period must be at least 1 day")
    private Integer installmentPeriodDays;

    private LocalDate firstInstallmentDate;

    private List<Installment> Installment;
}
