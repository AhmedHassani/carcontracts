package com.ahd.backend.carcontracts.appuser.dto;


import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionResponseDTO {
    private Long roleId;
    private String roleName;
    private String roleDisplayName;
    private List<PermissionDTO> permissions;
    private Integer totalPermissions;
    private String message;
}
