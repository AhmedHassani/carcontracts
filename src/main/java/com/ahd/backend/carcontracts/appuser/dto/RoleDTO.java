package com.ahd.backend.carcontracts.appuser.dto;


import com.ahd.backend.carcontracts.appuser.models.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String displayName;
    private String displayNameAr;
    private String description;
    private RoleType roleType;
    private Long companyId;
    private String companyName;
    private boolean isEditable;
    private boolean isDeletable;
    private List<PermissionDTO> permissions;
    private Integer userCount;
}