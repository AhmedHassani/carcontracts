package com.ahd.backend.carcontracts.authorization.model;

import com.ahd.backend.carcontracts.person.model.Person;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "authorization_history")
public class AuthorizationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id")
    private Long companyId;

    @NotNull
    @Column(name = "authorization_id", nullable = false)
    private Long authorizationId;

    @NotNull
    @Column(name = "update_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_buyer_id")
    private Person newBuyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_buyer_id")
    private Person oldBuyer;

    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "change_number", nullable = false)
    private Integer changeNumber;
    
}