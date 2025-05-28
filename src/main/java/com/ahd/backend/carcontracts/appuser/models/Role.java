package com.ahd.backend.carcontracts.appuser.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
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
    @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String name; // e.g. "ROLE_ADMIN"
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}