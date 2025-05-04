package com.ahd.backend.carcontracts.users;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private RoleEnum role;
    private Long companyId;
}
