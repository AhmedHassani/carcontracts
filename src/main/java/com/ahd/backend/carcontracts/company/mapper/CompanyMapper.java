package com.ahd.backend.carcontracts.company.mapper;


import com.ahd.backend.carcontracts.appuser.models.CreateUserRequest;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.dto.CompanyRequest;
import com.ahd.backend.carcontracts.company.dto.CompanyResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;


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

    public static CreateUserRequest toCreateUserRequest(CompanyRequest req, Role ownerRole) {
        Objects.requireNonNull(req,       "req must not be null");
        Objects.requireNonNull(ownerRole, "ownerRole must not be null");
        return CreateUserRequest.builder()
                .username(req.companyUsername().trim())
                .password(req.companyPassword())
                .email(req.companyEmail().toLowerCase(Locale.ROOT))
                .phone(req.ownerContact())
                .fullName(req.ownerName())
                .roleIds(Set.of(ownerRole.getId()))
                .build();
    }
}
