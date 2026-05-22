package com.ahd.backend.carcontracts.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String title;
    private String message;
    private String actionBy;
    private String actionType;
    @Builder.Default
    private LocalDateTime actionDate = LocalDateTime.now();
    private Long companyId;
    private List<Long> targetUserIds;
    private List<String> targetRoles;
    private Map<String, String> additionalData;  // ✅ أضف هذا الحقل
    
    public boolean hasSpecificUsers() {
        return targetUserIds != null && !targetUserIds.isEmpty();
    }
    
    public boolean hasTargetRoles() {
        return targetRoles != null && !targetRoles.isEmpty();
    }
}