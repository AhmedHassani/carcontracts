package com.ahd.backend.carcontracts.appuser.dto;

import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Permission;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.company.enums.PaymentCompanyType;
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.config.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
public class UserDetailsDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Long companyUserId;
    private List<String> roles;
    Set<Permission> permissions;
    private PaymentCompanyType paymentCompanyType;
    
    @JsonIgnore
    private String image; // S3 key
    
    private String imageUrl; // Full S3 URL

    public static UserDetailsDTO fromAppUser(AppUser user) {
        S3UrlService s3UrlService = ApplicationContextProvider.getApplicationContext().getBean(S3UrlService.class);
        var result = user.getRoles().stream()
                .collect(Collectors.teeing(
                        Collectors.mapping(Role::getName, Collectors.toList()),
                        Collectors.flatMapping(role -> role.getPermissions().stream(), Collectors.toSet()),
                        (roleNames, permissions) -> Map.of(
                                "roleNames", roleNames,
                                "permissions", permissions
                        )
                ));
        @SuppressWarnings("unchecked")
        List<String> roleNames = (List<String>) result.get("roleNames");
        @SuppressWarnings("unchecked")
        Set<Permission> allPermissions = (Set<Permission>) result.get("permissions");
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .roles(roleNames)
                .permissions(allPermissions)
                .image(user.getImage())
                .imageUrl(s3UrlService.getImageUrl(user.getImage()))
                .build();
    }

    public static UserDetailsDTO fromAppUser(AppUser user, Company company) {
        S3UrlService s3UrlService = ApplicationContextProvider.getApplicationContext().getBean(S3UrlService.class);
        var result = user.getRoles().stream()
                .collect(Collectors.teeing(
                        Collectors.mapping(Role::getName, Collectors.toList()),
                        Collectors.flatMapping(role -> role.getPermissions().stream(), Collectors.toSet()),
                        (roleNames, permissions) -> Map.of(
                                "roleNames", roleNames,
                                "permissions", permissions
                        )
                ));
        @SuppressWarnings("unchecked")
        List<String> roleNames = (List<String>) result.get("roleNames");
        @SuppressWarnings("unchecked")
        Set<Permission> allPermissions = (Set<Permission>) result.get("permissions");
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .image(user.getImage())
                .imageUrl(s3UrlService.getImageUrl(user.getImage()))
                .companyUserId(company.getId())
                .paymentCompanyType(company.getPaymentCompanyType())
                .roles(roleNames)
                .permissions(allPermissions)
                .build();
    }
} 