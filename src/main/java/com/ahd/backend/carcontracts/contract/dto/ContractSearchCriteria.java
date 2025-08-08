package com.ahd.backend.carcontracts.contract.dto;

import com.ahd.backend.carcontracts.util.base.BaseCriteria;
import lombok.Builder;

@Builder
public record ContractSearchCriteria(
        /* BaseCriteria fields */
        String keyword,
        String sortBy,
        String sortDirection,
        /* -specific filters */
        String carType,
        String carNumber,
        String StatusPaymant,
        String BuyerName,
        String BuyerPhone,
        String SellerName,
        String SellerPhone



) implements BaseCriteria { }
