package com.ahd.backend.carcontracts.dashbord.dto;

import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DashbordRequestDTO {
    private LocalDate StartDate;
    private DateType dateType;
}
