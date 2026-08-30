package com.ahd.backend.carcontracts.car.dto;

import com.ahd.backend.carcontracts.car.model.CarAttachment;
import com.ahd.backend.carcontracts.util.base.Pagination;
import lombok.Builder;
import lombok.Data;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.person.model.Person;
import java.time.LocalDateTime;
import java.time.LocalDate;

import java.util.List;

@Data
@Builder
public class CarResponseDTO {
    private Long id;
    private String model;
    private String color;
    private String type;
    private String name;
    private String plateNumber;
    private String chassisNumber;
    private Integer kilometers;
    private Integer cylinderCount;
    private String passengerCount;
    private String engineType;
    private String origin;
    private List<CarAttachment> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String walletNumber;
    private String typeOfCarPlate;
    private String status;
    private String initPrice;
    private String description;
    private ContractResponse.PersonDTO currentPossessor;
    private String carPrice;
    private String annualContractNumber;
    private LocalDate annualContractDate;
    private LocalDate inspectionDate;
}