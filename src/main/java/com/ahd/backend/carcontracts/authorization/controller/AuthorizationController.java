package com.ahd.backend.carcontracts.authorization.controller;

import com.ahd.backend.carcontracts.authorization.dto.AuthorizationResponse;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpsertRequest;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpdateRequest;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationHistoryResponse;
import com.ahd.backend.carcontracts.authorization.mapper.AuthorizationMapper;
import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import com.ahd.backend.carcontracts.authorization.service.AuthorizationService;
import com.ahd.backend.carcontracts.authorization.service.AuthorizationSpecification;
import com.ahd.backend.carcontracts.person.dto.PersonResponseDTO;
import com.ahd.backend.carcontracts.person.dto.PersonSearchCriteria;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/authorizations")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService service;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_COMPANY') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthorizationResponse>>> getAllAuthorizations(
            @ModelAttribute AuthorizationSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AuthorizationResponse> personResponse = service.list(pageable,criteria);
        return ResponseEntity.ok(ApiResponse.success(personResponse));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_COMPANY') or hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody AuthorizationUpsertRequest request) {
        service.create(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Authorization Created Successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_COMPANY') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long id) {
        AuthorizationResponse result = service.get(id);
        return ResponseEntity.ok(ApiResponse.<AuthorizationResponse>builder()
                .success(true)
                .message("Ok")
                .data(result)
                .code(200)
                .date(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_COMPANY') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Authorization Deleted Successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }

    

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ROLE_COMPANY') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthorizationHistoryResponse>>> getChangeHistory(@PathVariable Long id) {
        List<AuthorizationHistoryResponse> history = service.getChangeHistory(id);
        return ResponseEntity.ok(ApiResponse.<List<AuthorizationHistoryResponse>>builder()
                .success(true)
                .message("Change history retrieved successfully")
                .data(history)
                .code(200)
                .date(Instant.now())
                .build());
    }

  @PutMapping("/{id}/change-buyer/{newBuyerId}")
@PreAuthorize("hasRole('ROLE_COMPANY') or hasRole('SUPER_ADMIN')")
public ResponseEntity<ApiResponse<AuthorizationResponse>> changeBuyerSimple(  
        @PathVariable Long id,
        @PathVariable Long newBuyerId) {
    AuthorizationUpdateRequest request = AuthorizationUpdateRequest.builder()
            .authorizationId(id)
            .newBuyerId(newBuyerId)
            .build();
    AuthorizationResponse result = service.updateIsChange(request);  
    return ResponseEntity.ok(ApiResponse.<AuthorizationResponse>builder()  
            .success(true)
            .message("Buyer changed successfully and is_change flag set to true")
            .data(result)
            .code(200)
            .date(Instant.now())
            .build());
}
}