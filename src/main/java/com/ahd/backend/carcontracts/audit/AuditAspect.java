package com.ahd.backend.carcontracts.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditRepo;
    private final ObjectMapper mapper;

    public AuditAspect(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
        this.mapper    = new ObjectMapper();
    }

    @Around("@annotation(auditable)")
    public Object aroundAuditable(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        long start = System.currentTimeMillis();
        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .companyId(TenantContext.getCompany())
                .userId(TenantContext.getUser())
                .operation(auditable.operation())
                .method(pjp.getSignature().toShortString())
                .success(false);

        // serialize params
        try {
            String paramsJson = mapper.writeValueAsString(pjp.getArgs());
            builder.params(paramsJson);
        } catch(Exception e) {
            builder.params("[unserializable]");
        }

        Object result = null;
        try {
            result = pjp.proceed();
            // serialize result
            try {
                String resJson = mapper.writeValueAsString(result);
                builder.result(resJson);
            } catch(Exception e) {
                builder.result("[unserializable]");
            }
            builder.success(true);
            return result;
        } catch (Throwable ex) {
            builder.errorMsg(ex.getMessage());
            throw ex;
        } finally {
            builder.timestamp(LocalDateTime.now());
            long duration = System.currentTimeMillis() - start;
            auditRepo.save(builder.build());
        }
    }
}
