package com.ahd.backend.carcontracts.car;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
    private Integer passengerCount;
    private String engineType;
    private String origin;
    private LocalDateTime createdAt;
}