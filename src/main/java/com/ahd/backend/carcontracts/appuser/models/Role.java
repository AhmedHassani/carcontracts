package com.ahd.backend.carcontracts.appuser.models;

import com.ahd.backend.carcontracts.company.model.Company;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.HashSet;
import java.util.Set;



@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., ROLE_COMPANY_123_MANAGER

    @Column(name = "display_name")
    private String displayName; // User-friendly name

    @Column(name = "display_name_ar")
    private String displayNameAr; // Arabic display name

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType; // SYSTEM or COMPANY_SPECIFIC

    // Link to company - NULL for system roles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<AppUser> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default  // This is important!
    private Set<Permission> permissions = new HashSet<>();
}
