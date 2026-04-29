package com.ahd.backend.carcontracts.contract.mapper;

import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.contract.dto.ContractPaymentsResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.person.model.Person;

import java.math.BigDecimal;
import java.util.Optional;

public final class ContractMapper {

    private ContractMapper() { }

    public static ContractResponse toDetails(Contracts c) {
        if (c == null) return null;

        ContractResponse dto = new ContractResponse();
        dto.setId(c.getId());
        dto.setContractDate(c.getContractDate());
        dto.setSeller(toPersonDTO(c.getSeller()));
        dto.setBuyer(toPersonDTO(c.getBuyer()));
        dto.setGuarantor(toPersonDTO(c.getGuarantor()));
        dto.setPossessor(toPersonDTO(c.getPossessor()));  // ADD THIS LINE
        dto.setCar(toCarDTO(c.getCar()));
        dto.setPaymentPlan(toPaymentPlanDTO(c.getPaymentPlan()));
        dto.setTemplateId(c.getTemplateId());
        return dto;
    }

    public static Contracts fromRequest(ContractRequest req,
                                        Person seller,
                                        Person buyer,
                                        Person guarantor,
                                        Person possessor,  // ADD THIS PARAMETER
                                        Car car,
                                        PaymentPlan plan) {

        Contracts c = new Contracts();
        c.setContractDate(req.getContractDate());
        c.setSeller(seller);
        c.setBuyer(buyer);
        c.setGuarantor(guarantor);
        c.setPossessor(possessor);  // ADD THIS LINE
        c.setCar(car);
        c.setPaymentPlan(plan);
        return c;
    }

    private static ContractResponse.PersonDTO toPersonDTO(Person p) {
        if (p == null) return null;

        ContractResponse.PersonDTO dto = new ContractResponse.PersonDTO();
        
        dto.setId(p.getId());
        dto.setCompanyId(p.getCompanyId());
        
        dto.setFirstName(p.getFirstName());
        dto.setFatherName(p.getFatherName());
        dto.setGrandfatherName(p.getGrandfatherName());
        dto.setFourthName(p.getFourthName());
        dto.setSurname(p.getSurname());
        
        // CORRECTED: Fixed the duplicate first name issue
        dto.setFullName(p.getFirstName() + " " + p.getFatherName() + " " + p.getGrandfatherName());
        
        dto.setPhone(p.getPhoneNumber());
        
        dto.setNationalId(p.getNationalId());
        dto.setResidenceCardNo(p.getResidenceCardNo());
        
        dto.setResidence(p.getResidence());
        dto.setDistrict(p.getDistrict());
        dto.setAlley(p.getAlley());
        dto.setHouseNo(p.getHouseNo());
        
        dto.setIssuingAuthority(p.getIssuingAuthority());
        dto.setInfoOffice(p.getInfoOffice());
        
        return dto;
    }

    private static ContractResponse.CarDTO toCarDTO(Car car) {
        if (car == null) return null;

        ContractResponse.CarDTO dto = new ContractResponse.CarDTO();
        
        dto.setId(car.getId());
        dto.setName(car.getName());
        dto.setModel(car.getModel());
        dto.setPlateNumber(car.getPlateNumber());
        dto.setColor(car.getColor());
        dto.setInitPrice(car.getInitPrice());
        dto.setChassisNumber(car.getChassisNumber());
        dto.setType(car.getType());
        dto.setKilometers(car.getKilometers());
        dto.setCylinderCount(car.getCylinderCount());
        dto.setPassengerCount(car.getPassengerCount());
        dto.setEngineType(car.getEngineType());
        dto.setOrigin(car.getOrigin());
        dto.setWalletNumber(car.getWalletNumber());
        dto.setTypeOfCarPlate(car.getTypeOfCarPlate());
        dto.setStatus(car.getStatus());
        dto.setDescription(car.getDescription());
        dto.setCreatedAt(car.getCreatedAt());
        
        return dto;
    }

    private static ContractResponse.PaymentPlanDTO toPaymentPlanDTO(PaymentPlan paymentPlan) {
        if (paymentPlan == null) return null;

        ContractResponse.PaymentPlanDTO dto = new ContractResponse.PaymentPlanDTO();
        dto.setId(paymentPlan.getId());
        dto.setStatus(paymentPlan.getStatus());
        dto.setPaymentType(paymentPlan.getPaymentType());
        
        BigDecimal total = paymentPlan.getTotalAmount() != null ? paymentPlan.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal remaining = paymentPlan.getRemainingAmount() != null ? paymentPlan.getRemainingAmount() : BigDecimal.ZERO;
        BigDecimal paid = total.subtract(remaining);
        
        if (paid.compareTo(BigDecimal.ZERO) < 0) {
            paid = paid.multiply(BigDecimal.valueOf(-1L));
        }
        
        dto.setRemainingAmount(paymentPlan.getRemainingAmount());
        dto.setTotalAmount(paymentPlan.getTotalAmount());
        dto.setPaidAmount(paid);
        
        return dto;
    }

    public static ContractPaymentsResponse toPayments(Contracts contract) {
        ContractPaymentsResponse dto = new ContractPaymentsResponse();
        dto.setContractId(contract.getId());
        
        String customerName = Optional.ofNullable(contract.getBuyer())
                .map(buyer -> 
                    Optional.ofNullable(buyer.getFirstName()).orElse("") + " " +
                    Optional.ofNullable(buyer.getFatherName()).orElse("") + " " +
                    Optional.ofNullable(buyer.getGrandfatherName()).orElse("") + " " +
                    Optional.ofNullable(buyer.getFourthName()).orElse("")
                ).orElse("").trim();
        
        dto.setCustomerName(customerName);
        dto.setCarName(Optional.ofNullable(contract.getCar())
                .map(Car::getName)
                .orElse(null));
        
        Optional.ofNullable(contract.getPaymentPlan()).ifPresent(plan -> {
            dto.setTotalAmount(plan.getTotalAmount());
            dto.setPaymentPlanCreationDate(plan.getCreatedAt().toLocalDate());
            dto.setDownPayment(plan.getDownPayment());
            dto.setRemainingAmount(plan.getRemainingAmount());
            dto.setStatus(Optional.ofNullable(plan.getStatus()).map(Enum::name).orElse(null));
            dto.setPaymentType(Optional.ofNullable(plan.getPaymentType()).map(Enum::name).orElse(null));
            dto.setInstallments(plan.getInstallments());
        });
        
        return dto;
    }
}