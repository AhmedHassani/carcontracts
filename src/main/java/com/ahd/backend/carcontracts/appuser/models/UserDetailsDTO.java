package com.ahd.backend.carcontracts.appuser.models;

import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.config.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailsDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    
    @JsonIgnore
    private String image; // S3 key
    
    private String imageUrl; // Full S3 URL

    public static UserDetailsDTO fromAppUser(AppUser user) {
        S3UrlService s3UrlService = ApplicationContextProvider.getApplicationContext().getBean(S3UrlService.class);
        
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .image(user.getImage())
                .imageUrl(s3UrlService.getImageUrl(user.getImage()))
                .build();
    }
} 