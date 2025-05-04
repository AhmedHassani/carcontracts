package com.ahd.backend.carcontracts.branch;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    public ResponseEntity<BranchResponseDTO> create(@RequestBody BranchRequestDTO dto) {
        return ResponseEntity.ok(branchService.createBranch(dto));
    }

    @GetMapping
    public ResponseEntity<List<BranchResponseDTO>> getAll() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }
}
