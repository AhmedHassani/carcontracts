package com.ahd.backend.carcontracts.config.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Optional;

@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuditLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res,
                                Object handler, Exception ex) {

        long start = (Long) req.getAttribute("startTime");
        long durationMs = System.currentTimeMillis() - start;

        // HTTP info
        String method   = req.getMethod();
        String uri      = req.getRequestURI();
        int    status   = res.getStatus();

        // Username (if using Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "anonymous";
        // Bodies
        String reqBody = Optional.ofNullable(req)
                .filter(r -> r instanceof ContentCachingRequestWrapper)
                .map(r -> new String(((ContentCachingRequestWrapper)r).getContentAsByteArray()))
                .orElse("");
        String resBody = Optional.ofNullable(res)
                .filter(r -> r instanceof ContentCachingResponseWrapper)
                .map(r -> {
                    try {
                        return new String(((ContentCachingResponseWrapper)r).getContentAsByteArray());
                    } catch (Exception e) { return "[error reading response]"; }
                })
                .orElse("");
        log.info(
                "AUDIT ▶ {} {} | user={} | status={} | duration={}ms | req={} | res={}",
                method, uri, user, status, durationMs,
                abbreviate(reqBody, 1024),
                abbreviate(resBody, 1024)
        );
    }

    private String abbreviate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }
}
