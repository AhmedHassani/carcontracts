package com.ahd.backend.carcontracts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")               // apply to all endpoints
                .allowedOriginPatterns("*")      // allow any origin
                .allowedMethods("*")             // allow GET, POST, PUT, DELETE, OPTIONS…
                .allowedHeaders("*")             // allow any header
                .allowCredentials(true);         // keep if you need cookies/auth
    }
}

