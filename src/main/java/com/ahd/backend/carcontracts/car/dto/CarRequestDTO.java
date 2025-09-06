package com.ahd.backend.carcontracts.car.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
    private Integer passengerCount;
    @Size(max = 50)
    private String engineType;
    @Size(max = 50)
    private String origin;
    private Long compnayId;
}
