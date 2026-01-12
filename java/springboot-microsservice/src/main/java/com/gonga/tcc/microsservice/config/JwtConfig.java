package com.gonga.tcc.microsservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret:tcc-microsservices-jwt-secret-2026}")
    private String secret;

    @Value("${jwt.access-token-expiration:900}")
    private long accessTokenExpiration; // 15 minutes in seconds

    @Value("${jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration; // 7 days in seconds

    public String getSecret() {
        return secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpiration * 1000;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration * 1000;
    }
}
