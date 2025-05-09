package com.ahd.backend.carcontracts.appuser.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {
        private final String type;
        private final String token;
}
