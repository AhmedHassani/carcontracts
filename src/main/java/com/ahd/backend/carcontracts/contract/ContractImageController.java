package com.ahd.backend.carcontracts.contract;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carcontracts/v1/ContractImages")
@RequiredArgsConstructor
public class ContractImageController {

    private final ContractImageService contractImageService;

    @PutMapping("/{id}")
    public ResponseEntity<ContractImage> updateImage(@PathVariable Long id,
                                                     @RequestBody ContractImageDTO dto) {
        ContractImage updated = contractImageService.updateContractImage(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteImage(@PathVariable Long id) {
        contractImageService.deleteContractImage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<List<ContractImage>> getImagesByContractId(@PathVariable Long contractId) {
        List<ContractImage> images = contractImageService.getImagesByContractId(contractId);
        return ResponseEntity.ok(images);
    }

}
