package com.gonga.tcc.microsservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gonga.tcc.microsservice.domain.User;
import com.gonga.tcc.microsservice.dtos.LoginRequestDTO;
import com.gonga.tcc.microsservice.dtos.TokenResponseDTO;
import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.exceptions.ResourceNotFoundException;
import com.gonga.tcc.microsservice.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public TokenResponseDTO login(LoginRequestDTO loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found for email: {}", loginRequest.getEmail());
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for email: {}", loginRequest.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        logger.info("Login successful for user: {}", user.getId());
        return TokenResponseDTO.of(accessToken, refreshToken, jwtService.getAccessTokenExpiration());
    }

    public TokenResponseDTO register(UserRequestDTO registerRequest) {
        logger.info("Registration attempt for email: {}", registerRequest.getEmail());

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setStatus(registerRequest.getStatus());

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        logger.info("Registration successful for user: {}", savedUser.getId());
        return TokenResponseDTO.of(accessToken, refreshToken, jwtService.getAccessTokenExpiration());
    }

    public TokenResponseDTO refreshToken(String refreshToken) {
        logger.info("Token refresh attempt");

        if (!jwtService.validateToken(refreshToken)) {
            logger.warn("Token refresh failed: Invalid token");
            throw new InvalidTokenException("Invalid refresh token");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            logger.warn("Token refresh failed: Not a refresh token");
            throw new InvalidTokenException("Invalid refresh token");
        }

        var userId = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Token refresh failed: User not found");
                    return new ResourceNotFoundException("User not found");
                });

        String newAccessToken = jwtService.generateAccessToken(user);

        logger.info("Token refresh successful for user: {}", user.getId());
        return TokenResponseDTO.ofAccessToken(newAccessToken, jwtService.getAccessTokenExpiration());
    }

    // Custom exceptions
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
