package com.ahd.backend.carcontracts.company.model;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CompanyUser.CompanyUserId.class)
public class CompanyUser {
    @Id
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "role", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CompanyUserRole role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyUserId implements java.io.Serializable {
        private Long company;
        private Long user;
    }
} 