package com.ahd.backend.carcontracts.company.dto;

import com.ahd.backend.carcontracts.company.enums.CompanyStatus;
import com.ahd.backend.carcontracts.company.enums.PaymentCompanyType;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
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
    @Builder.Default
    String companyUsername = null;
    @Builder.Default
    String companyPassword = null;
    @Builder.Default
    String companyEmail = null;
    PaymentCompanyType paymentCompanyType;
}
