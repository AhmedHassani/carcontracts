package com.ahd.backend.carcontracts.company.model;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CompanyRequest(
        /** تاريخ الانتهاء */
        @NotNull
        LocalDate expirationDate,

        /** اسم صاحب الشركة */
        @NotBlank
        @Size(max = 150)
        String ownerName,

        /** رقم صاحب الشركة (هاتف أو هوية) */
        @NotBlank
        @Size(max = 50)
        String ownerContact,

        /** اسم الشركة */
        @NotBlank
        @Size(max = 200)
        String companyName,

        /** عدد المستخدمين */
        @NotNull
        @Positive
        Integer userCount,

        /** موقع الشركة (اختياري) */
        @Size(max = 250)
        String companyLocation,
        @Email
        String companyEmail,
        @NotNull(message = "Username is required")
        @Size(min = 4,max = 20, message = "Username must be at most 4 characters long")
        @Pattern(regexp = "^[\\x00-\\x7F]*$", message = "Username must not contain Arabic or non-ASCII characters")
        String companyUsername,

        @NotNull(message = "Password is required")
        @Size(min = 8,max = 25, message = "Password must be at most 6 characters long")
        String companyPassword

) {}
