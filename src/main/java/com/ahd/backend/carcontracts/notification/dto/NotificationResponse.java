package com.ahd.backend.carcontracts.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String actionBy;
    private String actionType;
    private LocalDateTime actionDate;
    @JsonIgnore
    private Long companyId;
    private boolean isRead;
    private LocalDateTime createdAt;
    @JsonIgnore
    private String targetRole;
    @JsonIgnore
    private Long targetUserId;
}
