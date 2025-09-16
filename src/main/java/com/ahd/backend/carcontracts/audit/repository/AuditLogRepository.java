package com.ahd.backend.carcontracts.audit.repository;

import com.ahd.backend.carcontracts.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByCompanyId(Long companyId, Pageable pageable);

}
