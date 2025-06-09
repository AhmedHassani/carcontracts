package com.ahd.backend.carcontracts.config.jwt;



import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long expirationMs;
    private String refreshSecret;
    private long refreshExpirationMs;
}

