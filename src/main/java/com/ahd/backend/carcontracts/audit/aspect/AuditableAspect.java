package com.ahd.backend.carcontracts.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ahd.backend.carcontracts.audit.annotation.Auditable;
import com.ahd.backend.carcontracts.audit.dto.AuditEventPayload;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Aspect
@Component
@Slf4j
public class AuditableAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final Helper helper;
    private ObjectMapper objectMapper;
    
    public AuditableAspect(ApplicationEventPublisher eventPublisher, Helper helper) {
        this.eventPublisher = eventPublisher;
        this.helper = helper;
    }
    
    @PostConstruct
    public void init() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //log.info("=== AUDIT ASPECT INITIALIZED ===");
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        //log.info("=== AUDIT ASPECT TRIGGERED for: {} ===", joinPoint.getSignature().getName());

        String paramsJson = null;
        String resultJson = null;
        boolean success = true;
        String errorMsg = null;
        Object result = null;
        
        try {
            paramsJson = serializeToJson(joinPoint.getArgs());
            result = joinPoint.proceed();
            resultJson = serializeToJson(result);
            return result;
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                // Create payload WITHOUT builder
                AuditEventPayload payload = new AuditEventPayload();
                payload.setMethod(getMethodSignature(joinPoint));
                payload.setOperation(auditable.operation());
                payload.setParams(paramsJson);
                payload.setResult(resultJson);
                payload.setSuccess(success);
                payload.setErrorMsg(errorMsg);
                payload.setUserId(getCurrentUserId());
                payload.setCompanyId(getCurrentCompanyId());
                
               //log.info("Publishing audit event for operation: {}", auditable.operation());
                eventPublisher.publishEvent(payload);
                
            } catch (Exception e) {
               //log.error("Failed to publish audit event for method: {}", 
                    //joinPoint.getSignature().toShortString(), e);
            }
        }
    }
    
    private String serializeToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            String json = objectMapper.writeValueAsString(obj);
           //log.debug("Serialized to JSON: {}", json.substring(0, Math.min(json.length(), 100)));
            return json;
        } catch (Exception e) {
           //log.error("Failed to serialize object of type {} to JSON for auditing", 
                //obj.getClass().getName(), e);
            return "{\"error\": \"Serialization failed: " + e.getMessage() + "\"}";
        }
    }
    
    private String getMethodSignature(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().toShortString();
    }
    
    private Long getCurrentUserId() {
        try {
            return helper.getCurrentUserId();
        } catch (Exception e) {
           //log.warn("Could not get current user ID for audit", e);
            return null;
        }
    }
    
    private Long getCurrentCompanyId() {
        try {
            return helper.getCurrentCompanyId();
        } catch (Exception e) {
           //log.warn("Could not get current company ID for audit", e);
            return null;
        }
    }
}