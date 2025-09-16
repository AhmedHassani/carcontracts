package com.ahd.backend.carcontracts.audit.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.audit.dto.AuditLogResponseDTO;
import com.ahd.backend.carcontracts.audit.model.AuditLog;
import com.ahd.backend.carcontracts.audit.repository.AuditLogRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository appUserRepository;
    private final Helper helper;

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAllAuditLogsForCompany(Pageable pageable) {
        Long companyId = getCompanyId();

        Page<AuditLog> logs = auditLogRepository.findAllByCompanyId(companyId, pageable);

        return logs.map(log -> {
            String username = null;
            if (log.getUserId() != null) {
                username = appUserRepository.findById(log.getUserId())
                        .map(AppUser::getUsername)
                        .orElse(null);
            }
            return AuditLogResponseDTO.fromEntity(log, username);
        });
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
}
