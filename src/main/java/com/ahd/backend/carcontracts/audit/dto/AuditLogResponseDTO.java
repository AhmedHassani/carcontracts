package com.ahd.backend.carcontracts.audit.dto;

import com.ahd.backend.carcontracts.audit.model.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
        Long id,
        Long companyId,
        String username,
        String operation,
        String method,
//        String params,
//        String result,
        Boolean success,
        String errorMsg
//        LocalDateTime timestamp
) {
    public static AuditLogResponseDTO fromEntity(AuditLog log , String userName) {
        return new AuditLogResponseDTO(
                log.getId(),
                log.getCompanyId(),
                userName,
                log.getOperation(),
                log.getMethod(),
//                log.getParams(),
//                log.getResult(),
                log.getSuccess(),
                log.getErrorMsg()
//                log.getTimestamp()
        );
    }
}
