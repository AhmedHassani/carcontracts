package com.ahd.backend.carcontracts.appuser.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
        private String tokenType;
        private String accessToken;
        private String username;
        private long expiresIn;
        private String refreshToken;
        private long refreshExpiresIn;
        private List<String> roles;
}
