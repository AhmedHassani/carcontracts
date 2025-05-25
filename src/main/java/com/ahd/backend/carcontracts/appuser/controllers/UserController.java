package com.ahd.backend.carcontracts.appuser.controllers;

import com.ahd.backend.carcontracts.S3.ImageStorageService;
import com.ahd.backend.carcontracts.appuser.models.UpdateProfileRequest;
import com.ahd.backend.carcontracts.appuser.models.UserDetailsDTO;
import com.ahd.backend.carcontracts.appuser.services.UserService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ImageStorageService imageStorageService;

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserDetails());
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDetailsDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<Object> updateProfile(@Validated @RequestBody UpdateProfileRequest request) {
        UserDetailsDTO updatedUser = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Profile updated successfully")
                .code(200)
                .date(Instant.now())
                .success(true)
                .data(updatedUser)
                .build());
    }

    @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateProfilePhoto(@RequestPart("photo") MultipartFile photo) {
        UserDetailsDTO updatedUser = userService.updateProfilePhoto(photo);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Profile photo updated successfully")
                .code(200)
                .date(Instant.now())
                .success(true)
                .data(updatedUser)
                .build());
    }
} 