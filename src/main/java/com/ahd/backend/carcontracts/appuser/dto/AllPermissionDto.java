package com.ahd.backend.carcontracts.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllPermissionDto {
    private Long id;
    private String name;
    private String displayNameAr;
    private String description;
}