package com.ahd.backend.carcontracts.audit.event;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.audit.dto.AuditEventPayload;
import com.ahd.backend.carcontracts.util.Helper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Aspect
@Component("auditAspect")
@RequiredArgsConstructor
public class AuditAspect {

    private final ApplicationEventPublisher publisher;
    private final Helper helper;
    private final ObjectMapper om = new ObjectMapper();

    @PostConstruct
    public void init() {
        // Register JavaTimeModule to support LocalDate, LocalDateTime, etc.
        om.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps (use ISO format instead)
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        boolean success = false;
        String error = null;
        String params = null;
        String result = null;

        Long userId    = safeUserId();
        Long companyId = safeCompanyId();

        if (auditable.captureArgs()) {
            params = safeArgs(pjp.getArgs());
        }

        Object value = null;
        try {
            value = pjp.proceed();
            success = true;
            if (auditable.captureResult()) {
                result = safeJson(value);
            }
            return value;
        } catch (Throwable t) {
            success = false;
            error = (t.getMessage() != null ? t.getMessage() : t.toString());
            throw t;
        } finally {
            String methodName = auditable.method().isBlank()
                    ? pjp.getSignature().toShortString()
                    : auditable.method();

            // Create payload without builder
            AuditEventPayload payload = new AuditEventPayload();
            payload.setCompanyId(companyId);
            payload.setUserId(userId);
            payload.setOperation(auditable.operation());
            payload.setMethod(methodName);
            payload.setParams(params);
            payload.setResult(result);
            payload.setSuccess(success);
            payload.setErrorMsg(error);
            
            publisher.publishEvent(payload);
        }
    }

    private Long safeUserId()    { try { return helper.getCurrentUserId();    } catch (Exception e) { return null; } }
    private Long safeCompanyId() { try { return helper.getCurrentCompanyId(); } catch (Exception e) { return null; } }

    private String safeArgs(Object[] args) {
        try {
            // mapper copy so we don't affect global configuration
            ObjectMapper m = om.copy();

            SimpleModule mod = new SimpleModule();
            mod.addSerializer(MultipartFile.class, new JsonSerializer<MultipartFile>() {
                @Override
                public void serialize(MultipartFile value, JsonGenerator gen, SerializerProvider serializers)
                        throws java.io.IOException {
                    gen.writeString("[file]");
                }
            });
            m.registerModule(mod);

            var list = java.util.Arrays.stream(args)
                    .map(a -> (a instanceof MultipartFile) ? "[file]" : a)
                    .toList();

            return m.writeValueAsString(list);
        } catch (Exception e) {
            return "[unserializable1] : " + e;
        }
    }

    private String safeJson(Object o) {
        try {
            ObjectMapper m = om.copy();
            m.registerModule(new SimpleModule().addSerializer(
                    org.springframework.web.multipart.MultipartFile.class,
                    new com.fasterxml.jackson.databind.JsonSerializer<org.springframework.web.multipart.MultipartFile>() {
                        @Override
                        public void serialize(org.springframework.web.multipart.MultipartFile v,
                                              com.fasterxml.jackson.core.JsonGenerator g,
                                              com.fasterxml.jackson.databind.SerializerProvider p)
                                throws java.io.IOException {
                            g.writeString("[file]");
                        }
                    }
            ));
            return m.writeValueAsString(o);
        } catch (Exception e) {
            return "[unserializable] : " + e;
        }
    }
}