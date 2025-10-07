package com.ahd.backend.carcontracts.authorization.dto;

import com.ahd.backend.carcontracts.util.base.BaseCriteria;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AuthorizationSearchCriteria(
        /* BaseCriteria fields */
        String keyword,
        String sortBy,
        String sortDirection,
        /* -specific filters */
        Long authorizationNumber,
        String companyAgent,
        LocalDate authorizationDateStart,
        LocalDate authorizationDateEnd,
        Long companyId

) implements BaseCriteria { }
