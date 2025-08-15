package com.ahd.backend.carcontracts.authorization.controller;


import com.ahd.backend.carcontracts.authorization.dto.AuthorizationResponse;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpsertRequest;
import com.ahd.backend.carcontracts.authorization.mapper.AuthorizationMapper;
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
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/authorizations")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService service;

    @GetMapping
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
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Authorization Deleted Successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }


}
