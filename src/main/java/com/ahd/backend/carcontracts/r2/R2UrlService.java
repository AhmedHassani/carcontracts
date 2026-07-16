package com.ahd.backend.carcontracts.r2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class R2UrlService {

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public String getImageUrl(String key) {
        if (key == null || key.isBlank()) {
            log.warn("Null or empty key provided to getImageUrl");
            return null;
        }
        
        // Clean the key
        String cleanKey = key.startsWith("/") ? key.substring(1) : key;
        
        // Clean the base URL
        String baseUrl = publicUrl.endsWith("/") ? 
                publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
        
        String fullUrl = String.format("%s/%s", baseUrl, cleanKey);
        //log.info("Generated R2 URL: {}", fullUrl);
        
        return fullUrl;
    }
}