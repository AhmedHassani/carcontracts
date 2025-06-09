package com.ahd.backend.carcontracts.appuser.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.*;


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
}
