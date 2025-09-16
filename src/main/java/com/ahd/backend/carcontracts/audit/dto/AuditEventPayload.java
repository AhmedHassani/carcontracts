package com.ahd.backend.carcontracts.audit.dto;

import lombok.Builder;

@Builder
public record AuditEventPayload(
        java.util.UUID eventId,
        Long companyId,
        Long userId,
        String operation,
        String method,
        String params,
        String result,
        boolean success,
        String errorMsg
) {}
