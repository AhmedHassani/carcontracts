package com.ahd.backend.carcontracts.appuser.controllers;


import com.ahd.backend.carcontracts.appuser.dto.*;
import com.ahd.backend.carcontracts.appuser.services.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;



@RestController
@RequestMapping("${application.api.base-path}/permissions")
@RequiredArgsConstructor
@CrossOrigin
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;


    @GetMapping("/{roleId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY')")
    public ResponseEntity<RolePermissionDTO> getRolePermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(rolePermissionService.getRolePermissions(roleId));
    }



    @GetMapping
    public ResponseEntity<List<AllPermissionDto>> getAllPermissions() {
        return ResponseEntity.ok(rolePermissionService.getAllPermissions());
    }

    @PostMapping("/{roleId}/permissions/{permissionId}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY')")
    public ResponseEntity<RolePermissionDTO> togglePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(rolePermissionService.togglePermission(roleId, permissionId));
    }

    @PostMapping("/{roleId}/toggle-all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('COMPANY')")
    public ResponseEntity<RolePermissionDTO> toggleAllPermissions(
            @PathVariable Long roleId,
            @RequestParam boolean enableAll) {
        return ResponseEntity.ok(rolePermissionService.toggleAllPermissions(roleId, enableAll));
    }


    // Link multiple permissions to role
    @PostMapping("/link-to-role/{roleId}")
    public ResponseEntity<RolePermissionResponseDTO> linkPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody LinkPermissionsRequest request) {
        return ResponseEntity.ok(rolePermissionService.linkPermissionsToRole(roleId, request.getPermissionIds()));
    }

    // Unlink single permission from role
    @DeleteMapping("/{permissionId}/unlink-from-role/{roleId}")
    public ResponseEntity<RolePermissionResponseDTO> unlinkPermissionFromRole(
            @PathVariable Long permissionId,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(rolePermissionService.unlinkPermissionFromRole(permissionId, roleId));
    }

    // Unlink multiple permissions from role
    @DeleteMapping("/unlink-from-role/{roleId}")
    public ResponseEntity<RolePermissionResponseDTO> unlinkPermissionsFromRole(
            @PathVariable Long roleId,
            @RequestBody LinkPermissionsRequest request) {
        return ResponseEntity.ok(rolePermissionService.unlinkPermissionsFromRole(roleId, request.getPermissionIds()));
    }

    // Replace all permissions for a role (set permissions)
    @PutMapping("/role/{roleId}")
    public ResponseEntity<RolePermissionResponseDTO> setRolePermissions(
            @PathVariable Long roleId,
            @RequestBody LinkPermissionsRequest request) {
        return ResponseEntity.ok(rolePermissionService.setRolePermissions(roleId, request.getPermissionIds()));
    }

    // Toggle permission for role (if exists, remove; if not exists, add)
    @PostMapping("/{permissionId}/toggle-for-role/{roleId}")
    public ResponseEntity<RolePermissionResponseDTO> togglePermissionForRole(
            @PathVariable Long permissionId,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(rolePermissionService.togglePermissionForRole(permissionId, roleId));
    }

    // Get available permissions for company (non-system permissions)
    @GetMapping("/available-for-company")
    public ResponseEntity<List<PermissionDTO>> getAvailablePermissionsForCompany() {
        return ResponseEntity.ok(rolePermissionService.getAvailablePermissionsForCompany());
    }

    // Check if role has specific permission
    @GetMapping("/role/{roleId}/has-permission/{permissionId}")
    public ResponseEntity<Boolean> roleHasPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(rolePermissionService.roleHasPermission(roleId, permissionId));
    }
}
