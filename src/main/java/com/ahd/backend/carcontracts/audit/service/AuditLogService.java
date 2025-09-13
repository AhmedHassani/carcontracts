package com.ahd.backend.carcontracts.audit.service;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.audit.AuditLog;
import com.ahd.backend.carcontracts.audit.AuditLogRepository;
import com.ahd.backend.carcontracts.audit.dto.AuditLogResponseDTO;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.person.dto.*;
import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import com.ahd.backend.carcontracts.person.mapper.PersonMapper;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.model.PersonAttachment;
import com.ahd.backend.carcontracts.person.repository.PersonAttachmentRepository;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.person.service.PersonSpecification;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
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
