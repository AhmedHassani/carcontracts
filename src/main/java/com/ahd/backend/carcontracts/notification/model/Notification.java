package com.ahd.backend.carcontracts.notification.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    // The person who performed the action (User ID or Name)
    @Column(name = "action_by")
    private String actionBy;

    // The type of action or point of process (e.g., "ADD_CONTRACT", "LATE_PAYMENT")
    @Column(name = "action_type")
    private String actionType;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    // Optional: Company ID (some notifications are global/owner level)
    @Column(name = "company_id")
    private Long companyId;

    // Optional: Target Role (e.g., "ROLE_OWNER", "ROLE_MANAGER")
    @Column(name = "target_role")
    private String targetRole;

    // Optional: Target User ID (for specific single-user notifications)
    @Column(name = "target_user_id")
    private Long targetUserId;

    // Read status (For simple tracking)
    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}