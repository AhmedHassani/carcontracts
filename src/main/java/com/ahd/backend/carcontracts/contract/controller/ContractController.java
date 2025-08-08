package com.ahd.backend.carcontracts.contract.controller;


import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.dto.ContractSearchCriteria;
import com.ahd.backend.carcontracts.contract.service.ContractService;
import com.ahd.backend.carcontracts.contract.service.ContractSpecification;
import com.ahd.backend.carcontracts.person.dto.PersonResponseDTO;
import com.ahd.backend.carcontracts.person.dto.PersonSearchCriteria;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse addContract(@Valid @RequestBody ContractRequest request) {
        return contractService.addContract(request);
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getAllPersons(
            @ModelAttribute ContractSearchCriteria criteria,              // filters
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ContractResponse> page = contractService.getAllContract(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteContract(@PathVariable Long id) {
        contractService.softDeleteContract(id);
        return ResponseEntity.noContent().build();
    }
}
