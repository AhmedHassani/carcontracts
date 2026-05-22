package com.ahd.backend.carcontracts.car.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarRequestDTO {
    @NotBlank
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
    
    private Long compnayId;
    
    private String walletNumber;
    
    private String typeOfCarPlate;
    
    private String initPrice;
    
    private String description;
    
    private String currentPossessorId;  // Change from Long to String
    
    private String carPrice;
    
    private String annualContractNumber;
    
    private LocalDate annualContractDate;
    
    private LocalDate inspectionDate;
}