package com.ahd.backend.carcontracts.contract.controller;


import com.ahd.backend.carcontracts.contract.dto.ContractPaymentsSearchCriteria;
import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.service.ContractService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
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

import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADD_CONTRSCT') or hasRole('SUPER_ADMIN')")
    public ContractResponse addContract(@Valid @RequestBody ContractRequest request) {
        return contractService.addContract(request);
    }
    @GetMapping
    @PreAuthorize("hasAuthority('GET_CONTRACT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getAllContract(
            @ModelAttribute ContractSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ContractResponse> contractResponses = contractService.getAllContract(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(contractResponses));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAuthority('GET_CONTRACT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllContract2(
            @ModelAttribute ContractPaymentsSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        var contractResponses = contractService.getAllContractPayments(criteria, pageable);
        return ResponseEntity.ok(contractResponses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_CONTRACT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> softDeleteContract(@PathVariable Long id) {
        contractService.softDeleteContract(id);
        return ResponseEntity.noContent().build();
    }
}
