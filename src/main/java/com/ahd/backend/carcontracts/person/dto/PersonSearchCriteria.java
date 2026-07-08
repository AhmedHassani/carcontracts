package com.ahd.backend.carcontracts.person.dto;

import com.ahd.backend.carcontracts.util.base.BaseCriteria;
import lombok.Builder;

@Builder
public record PersonSearchCriteria(
        /* BaseCriteria fields */
        String keyword,
        String sortBy,
        String sortDirection,
        /* Car-specific filters */
        String phoneNumber,
        String nationalId,
        String residenceCardNo,
        Long companyId,
        String residence,
        String nationalIdStatic
) implements BaseCriteria { }
