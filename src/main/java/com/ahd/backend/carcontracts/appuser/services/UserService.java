package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.models.UpdateProfileRequest;
import com.ahd.backend.carcontracts.appuser.models.UserDetailsDTO;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3FileStorageService imageStorageService;
    private final CompanyUserRepository companyUserRepository;

    /**
     * Get the currently authenticated user's details
     */
    @Transactional(readOnly = true)
    public UserDetailsDTO getCurrentUserDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if(isSuperAdmin(user.getRoles())){
            return UserDetailsDTO.fromAppUser(user);
        }else{
            CompanyUser companyUsers = companyUserRepository.findByUserId(user.getId());
            Company company = companyUsers.getCompany();
            return UserDetailsDTO.fromAppUser(user,company.getId());
        }
    }

    private boolean isSuperAdmin(Collection<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .anyMatch("ROLE_SUPER_ADMIN"::equals);
    }

    /**
     * Get user details by username
     */
    @Transactional(readOnly = true)
    public UserDetailsDTO getUserByUsername(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return UserDetailsDTO.fromAppUser(user);
    }

    /**
     * Get all users in the system
     */
    @Transactional(readOnly = true)
    public List<UserDetailsDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDetailsDTO::fromAppUser)
                .collect(Collectors.toList());
    }

    /**
     * Update the current user's profile
     */
    @Transactional
    public UserDetailsDTO updateProfile(UpdateProfileRequest request) {
        AppUser user = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        Optional.ofNullable(request.getFullName()).ifPresent(user::setFullName);
        Optional.ofNullable(request.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(request.getPhone()).ifPresent(user::setPhone);
        Optional.ofNullable(request.getPassword())
               .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));
        log.info("Updating profile for user: {}", user.getUsername());
        return UserDetailsDTO.fromAppUser(userRepository.save(user));
    }

    /**
     * Update the current user's profile photo
     */
    @Transactional
    public UserDetailsDTO updateProfilePhoto(MultipartFile photo) {
        AppUser user = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        // Delete old photo if exists
        if (user.getImage() != null) {
            imageStorageService.delete(user.getImage());
        }
        String imageKey = imageStorageService.upload(photo);
        user.setImage(imageKey);
        log.info("Updating profile photo for user: {}", user.getUsername());
        return UserDetailsDTO.fromAppUser(userRepository.save(user));
    }
} 