package com.ahd.backend.carcontracts.car.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UpdateCarRequestDTO {
    @Size(max = 50)
    private String name;
    @Size(max = 50)
    private String type;
    @Size(max = 50)
    private String color;
    @Size(max = 50)
    private String model;
    @Size(max = 20)
    private String plateNumber;
    @Size(max = 50)
    private String chassisNumber;
    @PositiveOrZero
    private Integer kilometers;
    private Integer cylinderCount;
    private String passengerCount;
    @Size(max = 50)
    private String engineType;
    @Size(max = 50)
    private String origin;
    private String walletNumber;
    private String typeOfCarPlate;
    private String initPrice;
    private String description;
    private Long currentPossessorId;
    private String carPrice;
    private String annualContractNumber;
    private LocalDate annualContractDate;
    private LocalDate inspectionDate;

    public boolean isEmpty() {
        // Only check fields that CANNOT be null in a meaningful update
        // Exclude currentPossessorId and carPrice because they can be explicitly set to null
        return name == null &&
               type == null &&
               color == null &&
               model == null &&
               plateNumber == null &&
               chassisNumber == null &&
               kilometers == null &&
               cylinderCount == null &&
               passengerCount == null &&
               engineType == null &&
               origin == null &&
               walletNumber == null &&
               typeOfCarPlate == null &&
               annualContractNumber == null &&
               annualContractDate == null &&
               inspectionDate == null &&
               description == null &&
               initPrice == null;
        // NOTE: currentPossessorId and carPrice are NOT checked here
        // because they can be explicitly set to null
    }
    
    // Add a method to check if this DTO contains ANY fields that need processing
    public boolean hasAnyField() {
        return name != null ||
               type != null ||
               color != null ||
               model != null ||
               plateNumber != null ||
               chassisNumber != null ||
               kilometers != null ||
               cylinderCount != null ||
               passengerCount != null ||
               engineType != null ||
               origin != null ||
               walletNumber != null ||
               typeOfCarPlate != null ||
               annualContractNumber != null ||
               annualContractDate != null ||
               inspectionDate != null ||
               description != null ||
               initPrice != null ||
               currentPossessorId != null ||
               carPrice != null;
    }
}