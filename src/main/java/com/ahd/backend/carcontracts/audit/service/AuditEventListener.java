package com.ahd.backend.carcontracts.audit.service;

import com.ahd.backend.carcontracts.audit.dto.AuditEventPayload;
import com.ahd.backend.carcontracts.audit.model.AuditLog;
import com.ahd.backend.carcontracts.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository repo;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onAudit(AuditEventPayload e) {
        repo.save(AuditLog.builder()
                .eventId(e.eventId())
                .companyId(e.companyId())
                .userId(e.userId())
                .operation(e.operation())
                .method(e.method())
                .params(e.params())
                .result(e.result())
                .success(e.success())
                .errorMsg(e.errorMsg())
                .build());
    }
}
