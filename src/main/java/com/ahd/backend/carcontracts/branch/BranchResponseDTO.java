package com.ahd.backend.carcontracts.branch;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BranchResponseDTO {
    private Long id;
    private String name;
    private String address;
    private Long companyId;
}