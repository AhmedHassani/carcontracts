package com.ahd.backend.carcontracts.appuser.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    private String displayNameAr;
    @Column(name = "is_system_only", nullable = false, columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private Boolean isSystemOnly = false;
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

    // Helper method to safely check if system only
    public boolean isSystemOnly() {
        return Boolean.TRUE.equals(isSystemOnly);
    }

    // Setter to ensure non-null value
    public void setIsSystemOnly(Boolean isSystemOnly) {
        this.isSystemOnly = isSystemOnly != null ? isSystemOnly : false;
    }
}
