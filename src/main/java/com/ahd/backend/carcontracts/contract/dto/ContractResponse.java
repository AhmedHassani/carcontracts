package com.ahd.backend.carcontracts.contract.dto;

import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime; 
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
    private PersonDTO possessor;
    private PersonDTO guarantor;
    private CarDTO car;
    private PaymentPlanDTO paymentPlan;
    private Long templateId;
    private boolean onus;


    @Getter @Setter
    public static class PersonDTO {
        Long id;
        String fullName;
        String phone;
        String firstName;
        String fatherName;
        String grandfatherName;
        String fourthName;
        String surname;
        String nationalId;
        String residenceCardNo;
        String residence;
        String district;
        String alley;
        String houseNo;
        String issuingAuthority;
        String infoOffice;
        Long companyId;
    }

    @Getter @Setter
    public static class CarDTO {
        Long id;
        String model;
        String plateNumber;
        String color;
        // Integer modelYear;
        String name;
        String initPrice;
        String chassisNumber;
        String type;
        Integer kilometers;
        Integer cylinderCount;
        String passengerCount;
        String engineType;
        String origin;
        LocalDateTime createdAt;
        String walletNumber;
        String typeOfCarPlate;
        String status;
        String description;
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
        BigDecimal remainingAmount;

    }

    @Getter @Setter
    public static class InstallmentDTO {
        Long id;
        LocalDate dueDate;
        BigDecimal amount;
        Boolean paid;
    }
}