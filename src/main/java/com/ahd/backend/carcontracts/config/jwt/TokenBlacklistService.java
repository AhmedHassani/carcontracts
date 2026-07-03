package com.ahd.backend.carcontracts.config.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {
    
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    public void blacklistToken(String token, long expirationMs) {
        if (token == null || token.isEmpty()) {
            return;
        }
        blacklistedTokens.put(token, System.currentTimeMillis() + expirationMs);
        log.debug("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 30)) + "...");
    }
    
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        Long expiryTime = blacklistedTokens.get(token);
        if (expiryTime == null) {
            return false;
        }
        
        // Auto-clean expired entries
        if (System.currentTimeMillis() > expiryTime) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }
    
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
    
    // Clean expired tokens periodically
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}