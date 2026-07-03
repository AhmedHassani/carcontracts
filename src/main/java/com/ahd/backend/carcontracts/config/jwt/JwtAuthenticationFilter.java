package com.ahd.backend.carcontracts.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService uds;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, 
                                   UserDetailsService uds,
                                   TokenBlacklistService tokenBlacklistService) {
        this.tokenProvider = tokenProvider;
        this.uds = uds;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, 
                                   HttpServletResponse res, 
                                   FilterChain chain)
            throws ServletException, IOException {
        
        String header = req.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            // CRITICAL: Check if token is blacklisted FIRST
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Blacklisted token used - rejecting request");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Session expired. Please login again.\"}");
                return;
            }
            
            // Validate token
            if (tokenProvider.validate(token)) {
                String username = tokenProvider.getUsernameFromAccessToken(token);
                if (username != null) {
                    UserDetails userDetails = uds.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("User authenticated: {}", username);
                }
            }
        }
        
        chain.doFilter(req, res);
    }
}