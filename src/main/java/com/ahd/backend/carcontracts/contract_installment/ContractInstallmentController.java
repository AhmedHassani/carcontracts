package com.ahd.backend.carcontracts.contract_installment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carcontracts/v1/installments")
@RequiredArgsConstructor
public class ContractInstallmentController {

    private final ContractInstallmentService service;

    @PostMapping
    public ResponseEntity<ContractInstallment> create(@RequestBody ContractInstallment installment) {
        return ResponseEntity.ok(service.create(installment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractInstallment> update(@PathVariable Long id, @RequestBody ContractInstallment updated) {
        return ResponseEntity.ok(service.update(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping
//    public ResponseEntity<List<ContractInstallment>> getAll() {
//        return ResponseEntity.ok(service.getAll());
//    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<ContractInstallmentResponseDTO>> getByContractId(@PathVariable Long contractId) {
        List<ContractInstallment> installments = service.getByContractId(contractId);

        List<ContractInstallmentResponseDTO> dtos = installments.stream()
                .map(i -> {
                    return ContractInstallmentResponseDTO.builder()
                            .id(i.getId())
                            .installmentNo(i.getInstallmentNo())
                            .dueDate(i.getDueDate())
                            .amountDue(i.getAmountDue())
                            .amountPaid(i.getAmountPaid())
                            .status(i.getStatus())
                            .paidAt(i.getPaidAt())
                            .createdAt(i.getCreatedAt())
                            .deleted(i.isDeleted())
                            .contractId(i.getContract().getId())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ContractInstallment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
