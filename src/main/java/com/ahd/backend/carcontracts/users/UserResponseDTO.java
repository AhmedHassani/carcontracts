package com.ahd.backend.carcontracts.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private RoleEnum role;
    private Long companyId;
}