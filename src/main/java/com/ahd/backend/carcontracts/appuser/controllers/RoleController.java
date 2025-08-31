package com.ahd.backend.carcontracts.appuser.controllers;


import com.ahd.backend.carcontracts.appuser.dto.CreateRoleRequest;
import com.ahd.backend.carcontracts.appuser.dto.RoleDTO;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.services.RoleService;
import com.ahd.backend.carcontracts.appuser.services.UserService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/roles")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ROLE_COMPANY') ")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService svc;
    private final UserService userServ;

    @GetMapping(value = {"/",""})
    public List<Role> list() {
        return svc.findAll();
    }

    @PostMapping
    public Role create(@RequestBody Role r) {
        return svc.create(r);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable Long id, @RequestBody Role r) {
        return svc.update(id, r);
    }


    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<RoleDTO>> getCompanyRoles(@PathVariable Long companyId) {
        return ResponseEntity.ok(svc.getCompanyRoles(companyId));
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }


    @PostMapping("/company/{companyId}")
    public ResponseEntity<RoleDTO> createCompanyRole(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(svc.createCompanyRole(companyId, request));
    }


    @DeleteMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @PathVariable Long userId
    ) {
        userServ.removeRoleFromUser(userId);
        return ResponseEntity.ok(ApiResponse.successMessage("Role removed successfully"));
    }

    @PutMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> replaceUserRoles(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        userServ.replaceUserRoles(userId, roleId);
        return ResponseEntity.ok(ApiResponse.successMessage("Roles replaced successfully"));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> mapUserToRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        userServ.mapUserToRole(userId, roleId);
        return ResponseEntity.ok(ApiResponse.successMessage("Role added successfully"));
    }

}
