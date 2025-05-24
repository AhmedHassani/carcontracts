package com.ahd.backend.carcontracts.appuser.models;
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
    private String httpMethod;
    @Column(nullable = false)
    private String pattern;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}