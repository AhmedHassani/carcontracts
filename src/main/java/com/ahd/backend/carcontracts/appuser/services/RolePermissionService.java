package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.appuser.dto.AllPermissionDto;
import com.ahd.backend.carcontracts.appuser.dto.RolePermissionResponseDTO;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Permission;
import com.ahd.backend.carcontracts.appuser.dto.PermissionDTO;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.dto.RolePermissionDTO;
import com.ahd.backend.carcontracts.appuser.models.RoleType;
import com.ahd.backend.carcontracts.appuser.repository.PermissionRepository;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



@Service
@Transactional
@RequiredArgsConstructor
public class RolePermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CompanyUserRepository companyUserRepository;
    private final Helper helper;


    public RolePermissionDTO getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        List<Permission> allPermissions = permissionRepository.findAll();
        Set<Long> rolePermissionIds = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        List<PermissionDTO> permissionDTOs = allPermissions.stream()
                .map(permission -> PermissionDTO.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .displayNameAr(permission.getDisplayNameAr())
                        .granted(rolePermissionIds.contains(permission.getId()))
                        .build())
                .collect(Collectors.toList());
        boolean allEnabled = rolePermissionIds.size() == allPermissions.size() && !allPermissions.isEmpty();
        return RolePermissionDTO.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .permissions(permissionDTOs)
                .allPermissionsEnabled(allEnabled)
                .build();
    }


    public List<AllPermissionDto> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findNonSystemPermissions();
        return permissions.stream()
                .map(this::convertToAllPermission)
                .collect(Collectors.toList());
    }

    public RolePermissionDTO togglePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));
        if (role.getPermissions().contains(permission)) {
            role.getPermissions().remove(permission);
        } else {
            role.getPermissions().add(permission);
        }
        roleRepository.save(role);
        return getRolePermissions(roleId);
    }

    // Toggle all permissions (when toggle switch is clicked)
    public RolePermissionDTO toggleAllPermissions(Long roleId, boolean enableAll) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        if (enableAll) {
            List<Permission> allPermissions = permissionRepository.findAll();
            role.getPermissions().clear();
            role.getPermissions().addAll(allPermissions);
        } else {
            role.getPermissions().clear();
        }
        roleRepository.save(role);
        return getRolePermissions(roleId);
    }

    // Link single permission to role
    public RolePermissionResponseDTO linkPermissionToRole(Long permissionId, Long roleId) {
        AppUser currentUser = helper.getCurrentUser();
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        validatePermissionLinkAccess(currentUser, role, permission);
        if (role.getPermissions().contains(permission)) {
            return buildRolePermissionResponse(role, "Permission already linked");
        }
        role.getPermissions().add(permission);
        role = roleRepository.save(role);
        return buildRolePermissionResponse(role, "Permission linked successfully");
    }

    // Link multiple permissions to role
    public RolePermissionResponseDTO linkPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Permission IDs list cannot be empty");
        }
        AppUser currentUser = helper.getCurrentUser();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        List<Permission> permissions = permissionRepository.findByIdIn(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BadRequestException("Some permission IDs are invalid");
        }
        for (Permission permission : permissions) {
            validatePermissionLinkAccess(currentUser, role, permission);
        }
        int addedCount = 0;
        for (Permission permission : permissions) {
            if (!role.getPermissions().contains(permission)) {
                role.getPermissions().add(permission);
                addedCount++;
            }
        }
        role = roleRepository.save(role);
        return buildRolePermissionResponse(role,
                String.format("Linked %d permissions successfully", addedCount));
    }

    // Unlink single permission from role
    public RolePermissionResponseDTO unlinkPermissionFromRole(Long permissionId, Long roleId) {
        AppUser currentUser = helper.getCurrentUser();
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        if (!role.getPermissions().contains(permission)) {
            return buildRolePermissionResponse(role, "Permission was not linked");
        }
        role.getPermissions().remove(permission);
        role = roleRepository.save(role);
        return buildRolePermissionResponse(role, "Permission unlinked successfully");
    }

    // Unlink multiple permissions from role
    public RolePermissionResponseDTO unlinkPermissionsFromRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Permission IDs list cannot be empty");
        }
        AppUser currentUser = helper.getCurrentUser();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        List<Permission> permissions = permissionRepository.findByIdIn(permissionIds);
        int removedCount = 0;
        for (Permission permission : permissions) {
            if (role.getPermissions().remove(permission)) {
                removedCount++;
            }
        }
        role = roleRepository.save(role);
        return buildRolePermissionResponse(role,
                String.format("Unlinked %d permissions successfully", removedCount));
    }

    // Replace all permissions for a role
    public RolePermissionResponseDTO setRolePermissions(Long roleId, List<Long> permissionIds) {
        AppUser currentUser = helper.getCurrentUser();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        role.getPermissions().clear();
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> permissions = permissionRepository.findByIdIn(permissionIds);
            for (Permission permission : permissions) {
                validatePermissionLinkAccess(currentUser, role, permission);
            }
            role.getPermissions().addAll(permissions);
        }
        role = roleRepository.save(role);
        return buildRolePermissionResponse(role,
                String.format("Set %d permissions for role", role.getPermissions().size()));
    }

    // Toggle permission for role
    public RolePermissionResponseDTO togglePermissionForRole(Long permissionId, Long roleId) {
        AppUser currentUser = helper.getCurrentUser();
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if(!getCompanyId(role.getCompany().getId())){
            throw new ResourceNotFoundException("Company authorization not found: " + role.getCompany().getId());
        }
        validatePermissionLinkAccess(currentUser, role, permission);
        boolean added;
        if (role.getPermissions().contains(permission)) {
            role.getPermissions().remove(permission);
            added = false;
        } else {
            role.getPermissions().add(permission);
            added = true;
        }
        role = roleRepository.save(role);
        String action = added ? "Added" : "Removed";
        return buildRolePermissionResponse(role,
                String.format("%s permission '%s'", action, permission.getName()));
    }
    // Get available permissions for company (non-system)
    public List<PermissionDTO> getAvailablePermissionsForCompany() {
        return permissionRepository.findAll().stream()
                .filter(p -> !p.isSystemOnly())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // Check if role has specific permission
    public boolean roleHasPermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        return role.getPermissions().contains(permission);
    }
    // Helper methods
    private boolean isSuperAdmin(AppUser user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"));
    }
//    private void validateRoleAccess(AppUser user, Role role) {
//        if (isSuperAdmin(user)) {
//            return;
//        }
//        if (role.getCompany() != null) {
//            boolean hasAccess = companyUserRepository
//                    .findByCompanyIdAndUserId(role.getCompany().getId(), user.getId())
//                    .isPresent();
//            if (!hasAccess) {
//                throw new BadRequestException("You don't have access to this role");
//            }
//        }
//    }

    private void validatePermissionLinkAccess(AppUser user, Role role, Permission permission) {
        if (permission.isSystemOnly() && !isSuperAdmin(user)) {
            throw new BadRequestException("This is a system-only permission");
        }
        if (role.getRoleType() == RoleType.COMPANY_SPECIFIC && permission.isSystemOnly()) {
            throw new BadRequestException("Company roles cannot have system-only permissions");
        }
    }

    private PermissionDTO convertToDTO(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .displayNameAr(permission.getDisplayNameAr())
                .description(permission.getDescription())
                .build();
    }

    private AllPermissionDto convertToAllPermission(Permission permission) {
        return AllPermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .displayNameAr(permission.getDisplayNameAr())
                .description(permission.getDescription())
                .build();
    }


    private RolePermissionResponseDTO buildRolePermissionResponse(Role role, String message) {
        List<PermissionDTO> permissions = role.getPermissions().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return RolePermissionResponseDTO.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .roleDisplayName(role.getDisplayName())
                .permissions(permissions)
                .totalPermissions(permissions.size())
                .message(message)
                .build();
    }

    public Boolean getCompanyId (Long companyId){
        if(companyId == helper.getCurrentCompanyId() ){
            return true;
        }
        return false ;
    }


}
