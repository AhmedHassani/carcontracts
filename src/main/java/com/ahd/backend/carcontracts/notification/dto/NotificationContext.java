package com.ahd.backend.carcontracts.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class NotificationContext {
    private String operation; // CREATE, UPDATE, DELETE
    private String title;
    private String message;
    private String actionType;
    private Object entity; // The car entity or any entity
    private Map<String, Object> additionalData;
    private Long entityId;
    private String entityName;
}