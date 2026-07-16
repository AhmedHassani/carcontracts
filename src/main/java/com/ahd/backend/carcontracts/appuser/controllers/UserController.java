package com.ahd.backend.carcontracts.appuser.controllers;

import com.ahd.backend.carcontracts.r2.R2FileStorageService;
import com.ahd.backend.carcontracts.appuser.dto.UpdateProfileRequest;
import com.ahd.backend.carcontracts.appuser.dto.UserDetailsDTO;
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

@RestController
@RequestMapping("${application.api.base-path}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final R2FileStorageService imageStorageService;

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserDetails());
    }

//    @GetMapping("/{username}")
//    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompany(authentication.principal.username, #companyId)")
//    public ResponseEntity<UserDetailsDTO> getUserByUsername(@PathVariable String username) {
//        return ResponseEntity.ok(userService.getUserByUsername(username));
//    }
    @GetMapping("CompanyUserRol/{username}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('GET_USER_INFO')")
    public ResponseEntity<UserDetailsDTO> getUserByUsernameOfCompany(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsernameOfCompany(username));
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