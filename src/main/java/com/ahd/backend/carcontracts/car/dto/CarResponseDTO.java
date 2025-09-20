package com.ahd.backend.carcontracts.car.dto;

import com.ahd.backend.carcontracts.car.model.CarAttachment;
import com.ahd.backend.carcontracts.util.base.Pagination;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
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
    private Integer passengerCount;
    private String engineType;
    private String origin;
    private List<CarAttachment> attachments;
    private LocalDateTime createdAt;
    private String walletNumber;
    private String typeOfCarPlate;
}