package com.ahd.backend.carcontracts.contract.mapper;




import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.payment.model.Installment;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.person.model.Person;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public final class ContractMapper {

    private ContractMapper() { }

    public static ContractResponse toDetails(Contracts c) {
        if (c == null) return null;

        ContractResponse dto = new ContractResponse();
        dto.setId           (c.getId());
        dto.setContractDate (c.getContractDate());
        dto.setSeller       (toPersonDTO(c.getSeller()));
        dto.setBuyer        (toPersonDTO(c.getBuyer()));
        dto.setGuarantor    (toPersonDTO(c.getGuarantor()));
        dto.setCar          (toCarDTO(c.getCar()));
        return dto;
    }

    public static Contracts fromRequest(ContractRequest req,
                                        Person seller,
                                        Person buyer,
                                        Person guarantor,
                                        Car car,
                                        PaymentPlan plan) {

        Contracts c = new Contracts();
        c.setContractDate(req.getContractDate());
        c.setSeller(seller);
        c.setBuyer(buyer);
        c.setGuarantor(guarantor);
        c.setCar(car);
        c.setPaymentPlan(plan);
        return c;
    }


    private static ContractResponse.PersonDTO toPersonDTO(Person p) {
        if (p == null) return null;

        ContractResponse.PersonDTO dto = new ContractResponse.PersonDTO();
        dto.setId        (p.getId());
        dto.setFullName  (p.getFirstName());
        dto.setPhone     (p.getPhoneNumber());
        dto.setNationalId(p.getNationalId());   // adjust fields as needed
        return dto;
    }

    private static ContractResponse.CarDTO toCarDTO(Car car) {
        if (car == null) return null;

        ContractResponse.CarDTO dto = new ContractResponse.CarDTO();
        dto.setId         (car.getId());
        dto.setModel      (car.getModel());
        dto.setPlateNumber(car.getPlateNumber());
        dto.setColor      (car.getColor());
        return dto;
    }

}

