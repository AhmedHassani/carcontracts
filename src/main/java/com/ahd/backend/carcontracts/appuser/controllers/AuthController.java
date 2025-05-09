package com.ahd.backend.carcontracts.appuser.controllers;


import com.ahd.backend.carcontracts.appuser.models.JwtResponse;
import com.ahd.backend.carcontracts.appuser.models.LoginRequest;
import com.ahd.backend.carcontracts.config.jwt.JwtTokenProvider;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Validated @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        String token = jwtProvider.generateToken(auth);
        return ResponseEntity.ok(new JwtResponse("Bearer", token));
    }



}
