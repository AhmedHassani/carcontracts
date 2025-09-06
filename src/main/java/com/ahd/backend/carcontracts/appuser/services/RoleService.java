package com.ahd.backend.carcontracts.appuser.services;



import com.ahd.backend.carcontracts.appuser.dto.CreateRoleRequest;
import com.ahd.backend.carcontracts.appuser.dto.PermissionDTO;
import com.ahd.backend.carcontracts.appuser.dto.RoleDTO;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Permission;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.models.RoleType;
import com.ahd.backend.carcontracts.appuser.repository.PermissionRepository;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.company.enums.CompanyUserRole;
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.util.Helper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final PermissionRepository permissionRepository;

    private final Helper helper;

    @Transactional
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role create(Role r) {
//        if(getCompanyId(r.getCompany().getId())){
//            throw new ResourceNotFoundException("Company authorization not found: " + r.getCompany().getId());
//        }
        if (roleRepository.findByName(r.getName()).isPresent()) {
            throw new IllegalArgumentException("Role already exists");
        }
        return roleRepository.save(r);
    }


    public List<RoleDTO> getCompanyRoles(Long companyId) {

        if(getCompanyId(companyId)){
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        List<Role> roles = roleRepository.findByCompanyId(companyId);
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public RoleDTO createCompanyRole(Long companyId, CreateRoleRequest request) {
        if(getCompanyId(companyId)){
            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
        }
        AppUser currentUser = helper.getCurrentUser();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        validateCompanyRoleCreationPermission(currentUser, company);
        String roleName = generateCompanyRoleName(company.getId(), request.getDisplayName());
        if (roleRepository.existsByName(roleName)) {
            throw new BadRequestException("Role with this name already exists in your company");
        }
        Role role = Role.builder()
                .name(roleName)
                .displayName(request.getDisplayName())
                .displayNameAr(request.getDisplayNameAr())
                .description(request.getDescription())
                .roleType(RoleType.COMPANY_SPECIFIC)
                .company(company)
                .permissions(new HashSet<>())  // Initialize explicitly
                .users(new HashSet<>())        // Initialize explicitly
                .build();
        // Add permissions if provided
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> requestedPermissions = permissionRepository.findByIdIn(request.getPermissionIds());
            // Filter out system-only permissions
            requestedPermissions = requestedPermissions.stream()
                    .filter(p -> !p.isSystemOnly())
                    .collect(Collectors.toList());

            role.getPermissions().addAll(requestedPermissions);
        }
        role = roleRepository.save(role);
        return convertToDTO(role);
    }

    private String generateCompanyRoleName(Long companyId, String displayName) {
//        if(getCompanyId(companyId)){
//            throw new ResourceNotFoundException("Company authorization not found: " + companyId);
//        }
        String baseName = displayName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");

        return "ROLE_COMPANY_" + companyId + "_" + baseName;
    }


    private void validateCompanyRoleCreationPermission(AppUser user, Company company) {
//        if(getCompanyId(company.getId())){
//            throw new ResourceNotFoundException("Company authorization not found: " + company.getId());
//        }
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return; // Super admin can create roles for any company
        }
        CompanyUser companyUser = companyUserRepository
                .findByCompanyIdAndUserId(company.getId(), user.getId())
                .orElseThrow(() -> new BadRequestException("You are not associated with this company"));
        if (companyUser.getRole() != CompanyUserRole.OWNER) {
            throw new BadRequestException("Only company owner can create roles");
        }
    }

    public Role update(Long id, Role r) {
        if(getCompanyId(r.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + r.getCompany().getId());
        }
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        existing.setName(r.getName());
        existing.setPermissions(r.getPermissions());
        return roleRepository.save(existing);
    }

    public void delete(Long id) {
        Role role = roleRepository.getById(id);

        if(getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        roleRepository.deleteById(id); }


    private RoleDTO convertToDTO(Role role) {
        List<PermissionDTO> permissions = role.getPermissions().stream()
                .map(p -> PermissionDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .displayNameAr(p.getDisplayNameAr())
                        .granted(true)
                        .build())
                .collect(Collectors.toList());

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .displayNameAr(role.getDisplayNameAr())
                .description(role.getDescription())
                .roleType(role.getRoleType())
                .companyId(role.getCompany() != null ? role.getCompany().getId() : null)
                .companyName(role.getCompany() != null ? role.getCompany().getCompanyName() : null)
                .permissions(permissions)
                .userCount(role.getUsers().size())
                .build();
    }
    public Boolean getCompanyId (Long companyId){
        if(companyId == helper.getCurrentCompanyId() ){
            return true;
        }
        return false ;
    }
}
