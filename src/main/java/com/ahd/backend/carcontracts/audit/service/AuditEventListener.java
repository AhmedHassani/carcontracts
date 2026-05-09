package com.ahd.backend.carcontracts.audit.service;

import com.ahd.backend.carcontracts.audit.dto.AuditEventPayload;
import com.ahd.backend.carcontracts.audit.model.AuditLog;
import com.ahd.backend.carcontracts.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository repo;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onAudit(AuditEventPayload e) {
        //log.info("=== AUDIT EVENT RECEIVED ===");
        // log.info("Operation: {}, User: {}, Company: {}", 
        //     e.getOperation(), e.getUserId(), e.getCompanyId());
        
        try {
            AuditLog auditLog = AuditLog.builder()
                    .companyId(e.getCompanyId())
                    .userId(e.getUserId())
                    .operation(e.getOperation())
                    .method(e.getMethod())
                    .params(e.getParams())
                    .result(e.getResult())
                    .success(e.getSuccess())
                    .errorMsg(e.getErrorMsg())
                    .build();
            
            AuditLog saved = repo.save(auditLog);
            //log.info("Audit log saved with ID: {}", saved.getId());
        } catch (Exception ex) {
            //log.error("Failed to save audit log", ex);
        }
    }
}