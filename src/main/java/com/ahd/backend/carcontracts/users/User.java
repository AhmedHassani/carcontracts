package com.ahd.backend.carcontracts.users;


import com.ahd.backend.carcontracts.company.Company;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users_info")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    private String email;

    private String phone;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private LocalDateTime createdAt;
}
