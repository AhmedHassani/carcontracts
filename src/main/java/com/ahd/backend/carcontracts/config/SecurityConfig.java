package com.ahd.backend.carcontracts.config;

import com.ahd.backend.carcontracts.appuser.services.AppUserDetailsService;
import com.ahd.backend.carcontracts.appuser.repository.SecuredEndpointRepository;
import com.ahd.backend.carcontracts.config.jwt.JwtAuthenticationFilter;
import com.ahd.backend.carcontracts.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService uds;
    private final JwtTokenProvider jwtProvider;
    private final SecuredEndpointRepository endpointRepo;
    @Value("${application.api.base-path}")
    private String apiBasePath;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(apiBasePath + "/auth/**").permitAll();
                    endpointRepo.findAll().forEach(ep ->
                            authorize.requestMatchers(
                                    HttpMethod.valueOf(ep.getHttpMethod()),
                                    ep.getPattern()
                            ).hasRole(ep.getRole().getName().substring(5))
                    );
                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, uds),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}


