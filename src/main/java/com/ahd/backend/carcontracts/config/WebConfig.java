package com.ahd.backend.carcontracts.config;

import com.ahd.backend.carcontracts.config.middleware.AuditLoggingInterceptor;
import com.ahd.backend.carcontracts.config.middleware.CachingRequestBodyFilter;
import com.ahd.backend.carcontracts.config.middleware.CorrelationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuditLoggingInterceptor auditInterceptor;

    public WebConfig(AuditLoggingInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**");      // scope it as you like
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> cachingFilter() {
        FilterRegistrationBean<OncePerRequestFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(new CachingRequestBodyFilter());
        frb.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return frb;
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> correlationFilter() {
        FilterRegistrationBean<OncePerRequestFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(new CorrelationFilter());
        frb.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return frb;
    }
}
