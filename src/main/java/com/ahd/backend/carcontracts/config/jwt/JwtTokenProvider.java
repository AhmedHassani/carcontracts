package com.ahd.backend.carcontracts.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {
    private final SecretKey key;
    private final SecretKey refreshKey;
    private final JwtProperties props;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenProvider(JwtProperties props, TokenBlacklistService tokenBlacklistService) {
        this.props = props;
        this.tokenBlacklistService = tokenBlacklistService;
        byte[] keyBytes = Decoders.BASE64.decode(props.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        byte[] refreshBytes = Decoders.BASE64.decode(props.getRefreshSecret());
        this.refreshKey = Keys.hmacShaKeyFor(refreshBytes);
    }

    public String generateAccessToken(Authentication auth) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpirationMs());
        String jti = UUID.randomUUID().toString();
        
        return Jwts.builder()
                .setId(jti)
                .setSubject(auth.getName())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Authentication auth) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getRefreshExpirationMs());
        String jti = UUID.randomUUID().toString();
        
        return Jwts.builder()
                .setId(jti)
                .setSubject(auth.getName())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(refreshKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException ex) {
            log.warn("Failed to parse access token: {}", ex.getMessage());
            return null;
        }
    }

    public String getUsernameFromRefreshToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException ex) {
            log.warn("Failed to parse refresh token: {}", ex.getMessage());
            return null;
        }
    }

    public boolean validate(String token) {
        try {
            // Check blacklist FIRST
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Access token is blacklisted");
                return false;
            }
            
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Access token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            // Check blacklist FIRST
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Refresh token is blacklisted");
                return false;
            }
            
            Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Refresh token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    // ADD THIS METHOD - Get access token expiration
    public long getAccessTokenExpirationMs() {
        return props.getExpirationMs();
    }
    
    // ADD THIS METHOD - Get refresh token expiration
    public long getRefreshTokenExpirationMs() {
        return props.getRefreshExpirationMs();
    }

    public JwtProperties getProps() {
        return props;
    }
}