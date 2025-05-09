package com.ahd.backend.carcontracts.Car;

import lombok.Data;

@Data
public class CarRequestDTO {
    private String model;
    private String color;
    private String type;
    private String name;
    private String plateNumber;
    private String chassisNumber;
    private Integer kilometers;
    private Integer cylinderCount;
    private Integer passengerCount;
    private String engineType;
    private String origin;
}
