package com.ahd.backend.carcontracts.car.dto;


import com.ahd.backend.carcontracts.util.base.BaseCriteria;
import lombok.Builder;

@Builder
public record CarSearchCriteria(
        /* BaseCriteria fields */
        String keyword,
        String sortBy,
        String sortDirection,
        /* Car-specific filters */
        String type,
        String color,
        String engineType,
        String origin,
        Boolean deleted,
        Integer minKm,
        Integer maxKm,
        Integer minCylinders,
        Integer maxCylinders,
        Long companyId,
        String model,
        String plateNumber,
        String chassisNumber,
        String status,
        String description ,
        String name


) implements BaseCriteria { }
