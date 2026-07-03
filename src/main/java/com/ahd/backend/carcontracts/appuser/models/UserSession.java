package com.ahd.backend.carcontracts.appuser.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 500)
    private String deviceId;
    
    @Column(nullable = false, length = 1000)
    private String refreshToken;
    
    @Column(nullable = false)
    private LocalDateTime loginTime;

     @Column(length = 1000)
    private String accessToken;
    
    @Column(nullable = false)
    private Boolean isActive = true;
}