package com.ahd.backend.carcontracts.person;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String fourth_name;

    @Column(nullable = false)
    private String grandfather_name;

    @Column(nullable = false)
    private String father_name;

    @Column(nullable = false)
    private String first_name;
    @Column(name = "national_id",nullable = false , unique = true)
    private String nationalId;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String housing_card_number;
    @Column(nullable = false)
    private String house_number;
    @Column(nullable = false)
    private String alley;
    @Column(nullable = false)
    private String neighborhood;
    @Column(nullable = false)
    private String info_office;
    @Column(nullable = false)
    private String issuing_authority;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
