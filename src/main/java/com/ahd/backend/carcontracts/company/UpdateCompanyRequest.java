package com.ahd.backend.carcontracts.company;


import java.time.LocalDate;
import java.util.Optional;

public record UpdateCompanyRequest(
        Optional<String> companyName,
        Optional<String> ownerName,
        Optional<String> ownerContact,
        Optional<Integer> userCount,
        Optional<LocalDate> subscriptionDate,
        Optional<LocalDate> expirationDate,
        Optional<String> companyLocation,
        Optional<CompanyStatus> status
) {}

