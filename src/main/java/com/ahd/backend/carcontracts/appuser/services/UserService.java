package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.dto.UpdateProfileRequest;
import com.ahd.backend.carcontracts.appuser.dto.UserDetailsDTO;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserStatsProjection;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.util.Helper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3FileStorageService imageStorageService;
    private final RoleRepository roleRepo;
    private final CompanyUserRepository companyUserRepository;
    private final Helper helper;

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
//    @Transactional(readOnly = true)
//    public UserDetailsDTO getUserByUsername(String username) {
//
//        AppUser user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
//        return UserDetailsDTO.fromAppUser(user);
//    }

    @Transactional(readOnly = true)
    public UserDetailsDTO getUserByUsernameOfCompany(String username) {
        final String requesterUsername =
                SecurityContextHolder.getContext().getAuthentication().getName();

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        AppUser requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Owner not found"));

        Long userCompanyId = companyUserRepository.findCompanyIdByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "User has no company"));

        Long requesterCompanyId = companyUserRepository.findCompanyIdByUserId(requester.getId())
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Requester has no company"));

        if (!Objects.equals(userCompanyId, requesterCompanyId)) {
            throw new ResponseStatusException(FORBIDDEN, "Not in the same company");
        }

        return UserDetailsDTO.fromAppUser(user);
    }
    /**
     * Get all users in the system
     */
//    @Transactional(readOnly = true)
//    public List<UserDetailsDTO> getAllUsers() {
//        return userRepository.findAll().stream()
//                .map(UserDetailsDTO::fromAppUser)
//                .collect(Collectors.toList());
//    }

    /**
     * Update the current user's profile
     */
    @Transactional
    @Auditable(operation = "تحديث بروفايل", captureArgs = true, captureResult = true)
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
    @Transactional
    @Auditable(operation = "تحديث صورة بروفايل", captureArgs = true, captureResult = true)
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
    @Transactional
    @Auditable(operation = "حذف صلاحيات من موظف", captureArgs = true, captureResult = true)
    public void removeRoleFromUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.getRoles().clear();
        userRepository.save(user);
    }
    @Transactional
    @Auditable(operation = "تغير صلاحيات موظف", captureArgs = true, captureResult = true)
    public void replaceUserRoles(Long userId, Long roleId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
    }
//    @Transactional
//    public UserStatsProjection getStats(LocalDate start, LocalDate end) {
//        return userRepository.getUserStats(start, end);
//    }
    @Transactional
    @Auditable(operation = "اضافة صلاحيات لموظف", captureArgs = true, captureResult = true)
    public void mapUserToRole(Long userId, Long roleId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        if (user.getRoles().contains(role)) {
            throw new IllegalStateException("User already has role id=" + roleId);
        }

        user.getRoles().add(role);
        userRepository.save(user);
    }
    public Boolean getCompanyId (Long companyId){
        if(companyId == helper.getCurrentCompanyId() ){
            return true;
        }
        return false ;
    }

} 