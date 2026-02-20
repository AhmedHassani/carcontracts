package com.ahd.backend.carcontracts.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String title;
    private String message;
    // Who did it?
    private String actionBy;
    // What kind of action? e.g. "LATE_PAYMENT", "NEW_CONTRACT"
    private String actionType;
    @Builder.Default
    private LocalDateTime actionDate = LocalDateTime.now();
    // Context
    private Long companyId;
    // Recipients
    // If you want to notify specific users:
    private List<Long> targetUserIds;
    // If you want to notify by Role (e.g. "ROLE_MANAGER", "ROLE_OWNER"):
    private List<String> targetRoles;
    // Helper to determine if it is for a specific user
    public boolean hasSpecificUsers() {
        return targetUserIds != null && !targetUserIds.isEmpty();
    }
    // Helper to determine if it is for roles
    public boolean hasTargetRoles() {
        return targetRoles != null && !targetRoles.isEmpty();
    }
}
