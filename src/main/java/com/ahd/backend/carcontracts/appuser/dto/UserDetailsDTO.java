package com.ahd.backend.carcontracts.appuser.dto;

import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.config.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
    
    @JsonIgnore
    private String image; // S3 key
    
    private String imageUrl; // Full S3 URL

    public static UserDetailsDTO fromAppUser(AppUser user) {
        S3UrlService s3UrlService = ApplicationContextProvider.getApplicationContext().getBean(S3UrlService.class);
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .image(user.getImage())
                .roles(roleNames)
                .imageUrl(s3UrlService.getImageUrl(user.getImage()))
                .build();
    }

    public static UserDetailsDTO fromAppUser(AppUser user,Long companyUserId) {
        S3UrlService s3UrlService = ApplicationContextProvider.getApplicationContext().getBean(S3UrlService.class);
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .image(user.getImage())
                .imageUrl(s3UrlService.getImageUrl(user.getImage()))
                .companyUserId(companyUserId)
                .roles(roleNames)
                .build();
    }
} 