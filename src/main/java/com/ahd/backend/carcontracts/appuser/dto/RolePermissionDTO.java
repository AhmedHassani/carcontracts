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
public class RolePermissionDTO {
    private Long roleId;
    private String roleName;
    private String roleNameAr;
    private List<PermissionDTO> permissions;
    private boolean allPermissionsEnabled;
}