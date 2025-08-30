package com.ahd.backend.carcontracts.appuser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
    @NotBlank(message = "Display name is required")
    private String displayName;
    private String displayNameAr;
    private String description;
    private List<Long> permissionIds;
}
