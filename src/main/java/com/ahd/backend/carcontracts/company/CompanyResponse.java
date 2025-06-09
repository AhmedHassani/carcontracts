package com.ahd.backend.carcontracts.company;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CompanyResponse {
    Long id;
    String companyName;
    String ownerName;
    String ownerContact;
    Integer userCount;
    LocalDate subscriptionDate;
    LocalDate expirationDate;
    String companyLocation;
    CompanyStatus status;
}
