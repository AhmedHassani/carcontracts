package com.ahd.backend.carcontracts.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    private String displayName;
    private String displayNameAr;
    private String description;
    private List<Long> permissionIds;
}
