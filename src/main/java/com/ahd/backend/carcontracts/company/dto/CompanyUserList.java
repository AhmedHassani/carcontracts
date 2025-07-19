package com.ahd.backend.carcontracts.company.dto;


import com.ahd.backend.carcontracts.company.model.CompanyUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserList {

    private Long   id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String role;

    /**
     * Convenience factory; keeps the mapping logic in one place.
     */
    public static CompanyUserList fromCompanyUser(CompanyUser cu) {
        var u = cu.getUser();
        return CompanyUserList.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .phone(u.getPhone())
                .fullName(u.getFullName())
                .role(cu.getRole().name())
                .build();
    }
}
