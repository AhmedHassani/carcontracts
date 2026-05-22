package com.ahd.backend.carcontracts.audit.controller;

import com.ahd.backend.carcontracts.audit.dto.AuditLogResponseDTO;
import com.ahd.backend.carcontracts.audit.service.AuditLogService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/Log")
@RequiredArgsConstructor
public class auditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("")
    public ApiResponse<List<AuditLogResponseDTO>> list(Pageable pageable) {
        Page<AuditLogResponseDTO> page = auditLogService.getAllAuditLogsForCompany(pageable);
        return ApiResponse.success(page);
    }

}
