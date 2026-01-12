package com.gonga.tcc.microsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.gonga.tcc.microsservice.domain.User;
import com.gonga.tcc.microsservice.domain.UserRole;
import com.gonga.tcc.microsservice.domain.UserStatus;
import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.dtos.UserResponseDTO;
import com.gonga.tcc.microsservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        userService = new UserService(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(userService, "userNotFound", "User not found");
    }

    @Test
    @DisplayName("Should hash password with BCrypt when creating user")
    void shouldHashPasswordOnCreate() {
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setRole(UserRole.USER);
        requestDTO.setStatus(UserStatus.ACTIVE);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        UserResponseDTO response = userService.createUser(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should hash password with BCrypt when updating user")
    void shouldHashPasswordOnUpdate() {
        UUID userId = UUID.randomUUID();
        
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("$2a$10$existinghash");
        existingUser.setRole(UserRole.USER);
        existingUser.setStatus(UserStatus.ACTIVE);
        existingUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingUser.setUpdatedAt(LocalDateTime.now().minusDays(1));

        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("New Name");
        requestDTO.setEmail("new@example.com");
        requestDTO.setPassword("newpassword123");
        requestDTO.setRole(UserRole.ADMIN);
        requestDTO.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        UserResponseDTO response = userService.updateUser(userId, requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should set default role to USER when not specified")
    void shouldSetDefaultRoleOnCreate() {
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("Jane Doe");
        requestDTO.setEmail("jane@example.com");
        requestDTO.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        UserResponseDTO response = userService.createUser(requestDTO);

        assertThat(response.getRole()).isEqualTo(UserRole.USER);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should include all new fields in response DTO")
    void shouldIncludeAllFieldsInResponse() {
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("Test User");
        requestDTO.setEmail("test@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setRole(UserRole.ADMIN);
        requestDTO.setStatus(UserStatus.INACTIVE);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        UserResponseDTO response = userService.createUser(requestDTO);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(response.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }
}
