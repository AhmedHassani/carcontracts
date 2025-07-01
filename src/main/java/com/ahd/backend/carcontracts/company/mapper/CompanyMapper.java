package com.ahd.backend.carcontracts.company.mapper;


import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.model.CompanyRequest;
import com.ahd.backend.carcontracts.company.model.CompanyResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequest request) {
        if (request == null) {
            return null;
        }
        return Company.builder()
                .companyName(request.companyName())
                .ownerName(request.ownerName())
                .ownerContact(request.ownerContact())
                .userCount(request.userCount())
                .subscriptionDate(LocalDate.now())
                .expirationDate(request.expirationDate())
                .companyLocation(request.companyLocation())
                .build();
    }

    public CompanyResponse toResponse(Company company,String password,String username,String email) {
        if (company == null) {
            return null;
        }
        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .ownerName(company.getOwnerName())
                .ownerContact(company.getOwnerContact())
                .userCount(company.getUserCount())
                .subscriptionDate(company.getSubscriptionDate())
                .expirationDate(company.getExpirationDate())
                .companyLocation(company.getCompanyLocation())
                .status(company.getStatus())
                .companyPassword(password)
                .companyUsername(username)
                .companyEmail(email)
                .build();
    }
}
