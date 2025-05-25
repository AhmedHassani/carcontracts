package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.AuthRequest;
import com.ahd.backend.carcontracts.appuser.models.AuthResponse;
import com.ahd.backend.carcontracts.appuser.models.RefreshRequest;
import com.ahd.backend.carcontracts.appuser.models.CreateUserRequest;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.config.jwt.JwtProperties;
import com.ahd.backend.carcontracts.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user and issue both access & refresh tokens.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );
            log.info("User '{}' logged in successfully", request.getUsername());
            return buildAuthResponse(authentication);
        } catch (AuthenticationException ex) {
            log.warn("Login failed for user '{}': {}", request.getUsername(), ex.getMessage());
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password");
        }
    }

    /**
     * Validate refresh token and re-issue tokens.
     */
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
        log.debug("Refreshing tokens for user '{}'", username);
        AppUser user = (AppUser) userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
        return buildAuthResponse(authentication);
    }

    /**
     * Common routine to build the AuthResponse DTO.
     */
    private AuthResponse buildAuthResponse(Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        String accessToken  = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .username(user.getUsername())
                .expiresIn(jwtProperties.getExpirationMs())
                .refreshToken(refreshToken)
                .refreshExpiresIn(jwtProperties.getRefreshExpirationMs())
                .roles(roleNames)
                .build();
    }

    /**
     * Create a new user with the specified roles.
     */
    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Username already exists");
        }

        // Get roles from roleIds
        Set<Role> roles = request.getRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Role not found with id: " + id)))
                .collect(Collectors.toSet());

        // Create and save the new user
        AppUser newUser = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .image(request.getImage())
                .roles(roles)
                .build();

        log.info("Creating new user with username: {}", request.getUsername());
        return userRepository.save(newUser);
    }
}
