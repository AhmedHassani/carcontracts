package com.ahd.backend.carcontracts.company.dto;


import com.ahd.backend.carcontracts.company.enums.CompanyUserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Optional;

@Builder
public record UpdateUserInCompanyRequest(
        @NotNull
        Long companyId,
        @NotNull
        Long userId,
        Optional<@Email String> email,
        Optional<@Size(min = 4, max = 20) String> username,
        Optional<@Size(min = 6, max = 25) String> password,
        Optional<String> fullName,
        Optional<String> phone,
        Optional<CompanyUserRole> companyUserRole
){}