package com.gonga.tcc.microsservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gonga.tcc.microsservice.config.JwtConfig;
import com.gonga.tcc.microsservice.domain.User;
import com.gonga.tcc.microsservice.domain.UserRole;
import com.gonga.tcc.microsservice.domain.UserStatus;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        // Use reflection to set values since @Value won't work in unit tests
        org.springframework.test.util.ReflectionTestUtils.setField(jwtConfig, "secret", "tcc-microsservices-jwt-secret-2026-test-key-minimum-256-bits");
        org.springframework.test.util.ReflectionTestUtils.setField(jwtConfig, "accessTokenExpiration", 900L);
        org.springframework.test.util.ReflectionTestUtils.setField(jwtConfig, "refreshTokenExpiration", 604800L);
        
        jwtService = new JwtService(jwtConfig);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole(UserRole.USER);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should generate access token with correct claims")
    void shouldGenerateAccessTokenWithCorrectClaims() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts

        UUID userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        assertThat(userId).isEqualTo(testUser.getId());
        assertThat(email).isEqualTo("john@example.com");
        assertThat(role).isEqualTo("USER");
    }

    @Test
    @DisplayName("Should generate refresh token with correct claims")
    void shouldGenerateRefreshTokenWithCorrectClaims() {
        String token = jwtService.generateRefreshToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);

        UUID userId = jwtService.extractUserId(token);
        boolean isRefresh = jwtService.isRefreshToken(token);

        assertThat(userId).isEqualTo(testUser.getId());
        assertThat(isRefresh).isTrue();
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        String token = jwtService.generateAccessToken(testUser);

        boolean isValid = jwtService.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should detect token is not expired for fresh token")
    void shouldDetectTokenNotExpired() {
        String token = jwtService.generateAccessToken(testUser);

        boolean isExpired = jwtService.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void shouldRejectTokenWithWrongSignature() {
        String token = jwtService.generateAccessToken(testUser);
        // Tamper with the token by changing a character in the signature
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        boolean isValid = jwtService.validateToken(tamperedToken);

        assertThat(isValid).isFalse();
    }
}
