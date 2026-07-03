package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.dto.AuthRequest;
import com.ahd.backend.carcontracts.appuser.dto.AuthResponse;
import com.ahd.backend.carcontracts.appuser.dto.RefreshRequest;
import com.ahd.backend.carcontracts.appuser.dto.CreateUserRequest;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.models.UserSession;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserSessionRepository;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.config.jwt.JwtProperties;
import com.ahd.backend.carcontracts.config.jwt.JwtTokenProvider;
import com.ahd.backend.carcontracts.config.jwt.TokenBlacklistService;
import com.ahd.backend.carcontracts.util.Helper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final Helper helper;
    private final UserSessionRepository userSessionRepository; // Add this
    private final TokenBlacklistService tokenBlacklistService;
    /**
     * Authenticate user and issue both access & refresh tokens.
     */
    @Transactional
    @Auditable(operation = "تسجيل دخول", captureArgs = true, captureResult = true)
public AuthResponse login(AuthRequest request) {
    try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        AppUser user = (AppUser) authentication.getPrincipal();
        
        // Get old active session BEFORE deactivating
        Optional<UserSession> oldSession = userSessionRepository
            .findByUserIdAndIsActiveTrue(user.getId());
        
        // Deactivate all existing sessions
        userSessionRepository.deactivateAllSessionsForUser(user.getId());
        
        // BLACKLIST OLD TOKENS - Both refresh AND access tokens
        if (oldSession.isPresent()) {
            // Blacklist old refresh token
            String oldRefreshToken = oldSession.get().getRefreshToken();
            tokenBlacklistService.blacklistToken(
                oldRefreshToken, 
                jwtTokenProvider.getRefreshTokenExpirationMs() // Use the new method
            );
            
            // Blacklist old access token too!
            String oldAccessToken = oldSession.get().getAccessToken();
            if (oldAccessToken != null && !oldAccessToken.isEmpty()) {
                tokenBlacklistService.blacklistToken(
                    oldAccessToken, 
                    jwtTokenProvider.getAccessTokenExpirationMs() // Use the new method
                );
                log.info("Blacklisted old access token for user: {}", user.getUsername());
            }
            
            log.info("Blacklisted all old tokens for user: {}", user.getUsername());
        }
        
        log.info("User '{}' logged in successfully from device: {}", 
                 request.getUsername(), request.getDeviceId());
        
        AuthResponse response = buildAuthResponse(authentication);
        
        // Save new session with BOTH tokens
        if (request.getDeviceId() != null && !request.getDeviceId().isEmpty()) {
            UserSession session = UserSession.builder()
                    .userId(user.getId())
                    .deviceId(request.getDeviceId())
                    .refreshToken(response.getRefreshToken())
                    .accessToken(response.getAccessToken()) // STORE ACCESS TOKEN
                    .loginTime(LocalDateTime.now())
                    .isActive(true)
                    .build();
            userSessionRepository.save(session);
            
            log.info("New session saved for user: {} with device: {}", 
                     user.getUsername(), request.getDeviceId());
        }
        
        return response;
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
        if(!isCompanyActive()){
            throw new ResponseStatusException(BAD_REQUEST, "Company expire or deleted");
        }
        
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }
        
        // Check if this session is still active
        Optional<UserSession> session = userSessionRepository.findByRefreshToken(refreshToken);
        if (session.isEmpty() || !session.get().getIsActive()) {
            log.warn("Attempt to use inactive session");
            throw new ResponseStatusException(UNAUTHORIZED, "Session expired. Please login again");
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

        List<String> roleNames = user.getRoles() == null ? List.of()
                : user.getRoles().stream().map(Role::getName).toList();
        if(!roleNames.stream().anyMatch("ROLE_SUPER_ADMIN"::equals)){
            boolean isActiveCompany = isCompanyActiveInlogin(user.getId());

            if ( !isActiveCompany) {
                throw new ResponseStatusException(BAD_REQUEST, "Company expire or deleted");
            }
        }

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
    @Auditable(operation = "اضافة حساب", captureArgs = true, captureResult = true)
    public AppUser createUser(CreateUserRequest request) {
        if( !isCompanyActive()){
            throw new ResponseStatusException(BAD_REQUEST, "Company expire or deleted");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Username already exists");
        }
        Set<Role> roles = request.getRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Role not found with id: " + id)))
                .collect(Collectors.toSet());
        AppUser newUser = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .image(request.getImage())
                .roles(roles)
                .build();
        return userRepository.save(newUser);
    }

    /**
     * Get user by ID
     */
    public Optional<AppUser> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Auditable(operation = "تحديث معلومات حساب", captureArgs = true, captureResult = true)
    public void updateUser(AppUser user) {
        if(! isCompanyActive()){
            throw new ResponseStatusException(BAD_REQUEST, "Company expire or deleted");
        }
        userRepository.save(user);
    }

    public boolean isCompanyActive() {
        System.out.println("test1");
       var user = helper.getCurrentUser();
        List<String> roleNames = user.getRoles() == null ? List.of()
                : user.getRoles().stream().map(Role::getName).toList();
        if(!roleNames.stream().anyMatch("ROLE_SUPER_ADMIN"::equals)){
            LocalDate today = LocalDate.now();
            return companyRepository.findByIdAndDeletedFalseAndExpirationDateGreaterThanEqual( helper.getCurrentCompanyId() , today).isPresent();
        }
       return true;
    }
    
    public boolean isCompanyActiveInlogin(Long id) {
        CompanyUser companyUser = companyUserRepository.findByUserId(id);
        LocalDate today = LocalDate.now();
        return companyRepository.findByIdAndDeletedFalseAndExpirationDateGreaterThanEqual(companyUser.getCompany().getId(), today).isPresent();
    }
}