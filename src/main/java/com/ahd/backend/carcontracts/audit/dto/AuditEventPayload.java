package com.ahd.backend.carcontracts.audit.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventPayload {
    private String method;
    private String operation;
    private String params;
    private String result;
    private Boolean success;
    private String errorMsg;
    private Long userId;
    private Long companyId;
}