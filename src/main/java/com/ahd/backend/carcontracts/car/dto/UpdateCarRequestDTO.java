package com.ahd.backend.carcontracts.car.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

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


    public boolean isEmpty() {
        return  name            == null &&
                type            == null &&
                color           == null &&
                model           == null &&
                plateNumber     == null &&
                chassisNumber   == null &&
                kilometers      == null &&
                cylinderCount   == null &&
                passengerCount  == null &&
                engineType      == null &&
                origin          == null &&
                walletNumber == null &&
                typeOfCarPlate == null ;
    }
}
