package com.ahd.backend.carcontracts.company.controller;

import com.ahd.backend.carcontracts.company.model.*;
import com.ahd.backend.carcontracts.company.service.CompanyService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CompanyResponse> createCompany(@Valid  @RequestBody CompanyRequest dto) {
        return ResponseEntity.ok(companyService.createCompany(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> getAllCompanies(
            CompanySearchCriteria searchCriteria,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(companyService.getAllCompanies(searchCriteria, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompany(authentication.principal.username, #id)")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompanyWithRole(authentication.principal.username, #id, 'OWNER')")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable Long id, @RequestBody UpdateCompanyRequest request) {
        CompanyResponse updated = companyService.updateCompany(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        ApiResponse<Void> response = companyService.deleteCompany(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{companyId}/users/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompanyWithRole(authentication.principal.username, #companyId, 'OWNER')")
    public ResponseEntity<ApiResponse<Void>> addUserToCompany(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @RequestParam CompanyUserRole role) {
        companyService.addUserToCompany(companyId, userId, role);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User added to company successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }


    @DeleteMapping("/{companyId}/users/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompanyWithRole(authentication.principal.username, #companyId, 'OWNER')")
    public ResponseEntity<ApiResponse<Void>> removeUserFromCompany(
            @PathVariable Long companyId,
            @PathVariable Long userId) {
        companyService.removeUserFromCompany(companyId, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User removed from company successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }

    @PutMapping("/{companyId}/users/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompanyWithRole(authentication.principal.username, #companyId, 'OWNER')")
    public ResponseEntity<ApiResponse<Void>> updateUserCompanyRole(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @RequestParam CompanyUserRole newRole) {
        companyService.updateUserCompanyRole(companyId, userId, newRole);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User role updated successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }

    @GetMapping("/{companyId}/users")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @companyService.isUserInCompany(authentication.principal.username, #companyId)")
    public ResponseEntity<List<CompanyUser>> getCompanyUsers(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyUsers(companyId));
    }
}
