package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.S3.ImageStorageService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.UpdateProfileRequest;
import com.ahd.backend.carcontracts.appuser.models.UserDetailsDTO;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;

    /**
     * Get the currently authenticated user's details
     */
    @Transactional(readOnly = true)
    public UserDetailsDTO getCurrentUserDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return UserDetailsDTO.fromAppUser(user);
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
        // Get current user from security context
        AppUser user = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
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