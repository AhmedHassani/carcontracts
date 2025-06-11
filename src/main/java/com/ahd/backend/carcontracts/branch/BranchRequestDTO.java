package com.ahd.backend.carcontracts.branch;

import lombok.Data;

@Data
public class BranchRequestDTO {
    private String name;
    private String address;
    private Long companyId;
}