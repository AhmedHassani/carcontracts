package com.ahd.backend.carcontracts.contract.dto;

import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponse {
    private Long id;
    private LocalDate contractDate;
    private PersonDTO seller;
    private PersonDTO buyer;
    private PersonDTO guarantor;
    private CarDTO car;
    private PaymentPlanDTO paymentPlan;

    @Getter @Setter
    public static class PersonDTO {
        Long id;
        String fullName;
        String phone;
        String nationalId;
    }

    @Getter @Setter
    public static class CarDTO {
        Long id;
        String model;
        String plateNumber;
        String color;
        Integer modelYear;
    }

    @Getter @Setter
    public static class PaymentPlanDTO {
        Long id;
        PaymentType method;
        PaymentStatus status;
        BigDecimal totalAmount;
        BigDecimal paidAmount;
        PaymentType paymentType;
        List<InstallmentDTO> installments;
    }

    @Getter @Setter
    public static class InstallmentDTO {
        Long id;
        LocalDate dueDate;
        BigDecimal amount;
        Boolean paid;
    }
}