package com.ahd.backend.carcontracts.appuser.controllers;


import com.ahd.backend.carcontracts.appuser.dto.CreateRoleRequest;
import com.ahd.backend.carcontracts.appuser.dto.RoleDTO;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ROLE_COMPANY') ")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService svc;

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
}
