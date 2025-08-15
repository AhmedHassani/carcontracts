package com.ahd.backend.carcontracts.contract.dto;

import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.util.base.BaseCriteria;
import lombok.Builder;

@Builder
public record ContractPaymentsSearchCriteria(
        /* BaseCriteria fields */
        String keyword,
        String sortBy,
        String sortDirection,
        /* -specific filters */
        String startDate,
        String endDate,
        PaymentStatus status
) implements BaseCriteria { }
