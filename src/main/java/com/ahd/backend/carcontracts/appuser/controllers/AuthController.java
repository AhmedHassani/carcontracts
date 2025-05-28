package com.ahd.backend.carcontracts.appuser.controllers;

import com.ahd.backend.carcontracts.appuser.models.AuthResponse;
import com.ahd.backend.carcontracts.appuser.models.AuthRequest;
import com.ahd.backend.carcontracts.appuser.models.RefreshRequest;
import com.ahd.backend.carcontracts.appuser.models.CreateUserRequest;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.services.AuthService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("${application.api.base-path}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody AuthRequest req) {
        AuthResponse authResponse = authService.login(req);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest req) {
        AuthResponse authResponse = authService.refresh(req);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Object> createUser(@Validated @RequestBody CreateUserRequest request) {
        authService.createUser(request);
        var response  = ApiResponse.builder()
                .message("User created successfully")
                .code(200)
                .date(Instant.now())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }
}
