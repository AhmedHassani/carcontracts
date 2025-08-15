package com.ahd.backend.carcontracts.dashbord.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponseDTO {
    private Integer totalCar;
    private Integer allPaidCar;
    private Integer allPaidInstallment;
}