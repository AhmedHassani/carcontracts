package com.ahd.backend.carcontracts.company.model;


import com.ahd.backend.carcontracts.company.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE company SET deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE company_id = ?")
@Where(clause = "deleted = 0")
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @Column(name = "subscription_date", nullable = false)
    private LocalDate subscriptionDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "owner_name", length = 150, nullable = false)
    private String ownerName;

    @Column(name = "owner_contact", length = 50, nullable = false)
    private String ownerContact;

    @Column(name = "company_name", length = 200, nullable = false)
    private String companyName;

    @Column(name = "user_count", nullable = false)
    private Integer userCount;

    @Column(name = "company_location", length = 250)
    private String companyLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CompanyStatus status;


    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
