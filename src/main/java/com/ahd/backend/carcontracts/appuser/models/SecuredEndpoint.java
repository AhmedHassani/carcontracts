package com.ahd.backend.carcontracts.appuser.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "secured_endpoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecuredEndpoint {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String httpMethod; // GET, POST, …
    @Column(nullable = false)
    private String pattern;
    // Ant‐style, e.g. /api/users/**
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}