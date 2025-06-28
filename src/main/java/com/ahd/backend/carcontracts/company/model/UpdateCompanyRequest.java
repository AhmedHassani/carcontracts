package com.ahd.backend.carcontracts.company.model;


import java.time.LocalDate;
import java.util.Optional;

public record UpdateCompanyRequest(
        Optional<String> companyName,
        Optional<String> ownerName,
        Optional<String> ownerContact,
        Optional<Integer> userCount,
        Optional<LocalDate> expirationDate,
        Optional<String> companyLocation,
        Optional<String> companyUsername,
        Optional<String> companyPassword,
        Optional<String> companyEmail
) {}

