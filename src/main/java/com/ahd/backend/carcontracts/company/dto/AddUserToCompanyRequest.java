package com.ahd.backend.carcontracts.company.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddUserToCompanyRequest(

        long companyId,

        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "FullName is required")
        String fullName,

        @NotNull(message = "phone is required")
        String phone,

        @NotNull(message = "Username is required")
        @Size(min = 4,max = 20, message = "Username must be at most 4 characters long")
        @Pattern(regexp = "^[\\x00-\\x7F]*$", message = "Username must not contain Arabic or non-ASCII characters")
        String username,

        @NotNull(message = "Password is required")
        @Size(min = 8,max = 25, message = "Password must be at most 6 characters long")
        String password
) {}
