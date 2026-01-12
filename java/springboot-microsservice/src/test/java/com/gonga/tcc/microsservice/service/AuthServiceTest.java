package com.gonga.tcc.microsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gonga.tcc.microsservice.config.JwtConfig;
import com.gonga.tcc.microsservice.domain.User;
import com.gonga.tcc.microsservice.domain.UserRole;
import com.gonga.tcc.microsservice.domain.UserStatus;
import com.gonga.tcc.microsservice.dtos.LoginRequestDTO;
import com.gonga.tcc.microsservice.dtos.TokenResponseDTO;
import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.repository.UserRepository;
import com.gonga.tcc.microsservice.service.AuthService.EmailAlreadyExistsException;
import com.gonga.tcc.microsservice.service.AuthService.InvalidCredentialsException;
import com.gonga.tcc.microsservice.service.AuthService.InvalidTokenException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        
        JwtConfig jwtConfig = new JwtConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(jwtConfig, "secret", "tcc-microsservices-jwt-secret-2026-test-key-minimum-256-bits");
        org.springframework.test.util.ReflectionTestUtils.setField(jwtConfig, "accessTokenExpiration", 900L);
        org.springframework.test.util.ReflectionTestUtils.setField(jwtConfig, "refreshTokenExpiration", 604800L);
        
        jwtService = new JwtService(jwtConfig);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(UserRole.USER);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should login successfully and return tokens")
    void shouldLoginSuccessfully() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("john@example.com", "password123");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        TokenResponseDTO response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900);
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials - wrong password")
    void shouldThrowExceptionForInvalidPassword() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("john@example.com", "wrongpassword");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials - user not found")
    void shouldThrowExceptionForUserNotFound() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent@example.com", "password123");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Should register successfully and return tokens")
    void shouldRegisterSuccessfully() {
        UserRequestDTO registerRequest = new UserRequestDTO();
        registerRequest.setName("Jane Doe");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.USER);
        registerRequest.setStatus(UserStatus.ACTIVE);

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        TokenResponseDTO response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void shouldThrowExceptionForExistingEmail() {
        UserRequestDTO registerRequest = new UserRequestDTO();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        TokenResponseDTO response = authService.refreshToken(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNull(); // Only returns new access token
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Should throw exception for invalid refresh token")
    void shouldThrowExceptionForInvalidRefreshToken() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid refresh token");
    }
}
