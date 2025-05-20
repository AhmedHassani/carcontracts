package com.ahd.backend.carcontracts.contract;


import com.ahd.backend.carcontracts.branch.BranchRequestDTO;
import com.ahd.backend.carcontracts.branch.BranchResponseDTO;
import com.ahd.backend.carcontracts.branch.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carcontracts/v1/Contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractResponseDTO> createContract(@RequestBody ContractRequestDTO dto) {
        return ResponseEntity.ok(contractService.createContract(dto));
    }

    @GetMapping
    public ResponseEntity<List<ContractResponseDTO>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractResponseDTO> updateContract(@PathVariable Long id, @RequestBody ContractRequestDTO dto) {
        return ResponseEntity.ok(contractService.updateContract(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteContract(@PathVariable Long id) {
        contractService.softDeleteContract(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDTO> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

}
