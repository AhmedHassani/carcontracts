package com.ahd.backend.carcontracts.audit.controller;

import com.ahd.backend.carcontracts.audit.AuditLog;
import com.ahd.backend.carcontracts.audit.dto.AuditLogResponseDTO;
import com.ahd.backend.carcontracts.audit.service.AuditLogService;
import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.service.CarService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
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
