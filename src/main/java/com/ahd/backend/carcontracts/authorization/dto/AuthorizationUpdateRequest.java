package com.ahd.backend.carcontracts.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationUpdateRequest {
    private Long authorizationId;
    private Long newBuyerId;
    private Long oldBuyerId;  // This can be auto-filled from existing data
    private Long userId;      // This will be auto-filled from helper
}