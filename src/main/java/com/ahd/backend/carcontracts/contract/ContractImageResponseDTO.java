package com.ahd.backend.carcontracts.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor   // ← adds the required constructor
@NoArgsConstructor
public class ContractImageResponseDTO {
    private Long id;
    private String imageUrl;
    private boolean deleted;
}

