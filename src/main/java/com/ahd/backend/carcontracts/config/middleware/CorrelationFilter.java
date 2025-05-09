package com.ahd.backend.carcontracts.config.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.logging.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class CorrelationFilter extends OncePerRequestFilter {
    private static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String corrId = req.getHeader("X-Request-ID");
        if (corrId == null) corrId = UUID.randomUUID().toString();
        MDC.put(MDC_CORRELATION_ID, corrId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
        }
    }
}
