package com.gonga.tcc.microsservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.gonga.tcc.microsservice.repository.UserRepository;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setName("John Doe");
        validUser.setEmail("john.doe@example.com");
        validUser.setPassword("password123");
        validUser.setRole(UserRole.USER);
        validUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should create user with all fields and auto-generate UUID")
    void shouldCreateUserWithAllFields() {
        User savedUser = entityManager.persistAndFlush(validUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should auto-populate createdAt and updatedAt timestamps")
    void shouldAutoPopulateTimestamps() {
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);
        
        User savedUser = entityManager.persistAndFlush(validUser);
        
        LocalDateTime afterSave = LocalDateTime.now().plusSeconds(1);

        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isAfter(beforeSave);
        assertThat(savedUser.getCreatedAt()).isBefore(afterSave);
    }

    @Test
    @DisplayName("Should set default role to USER when not specified")
    void shouldSetDefaultRoleToUser() {
        User newUser = new User();
        newUser.setName("Jane Doe");
        newUser.setEmail("jane.doe@example.com");
        newUser.setPassword("password123");

        User savedUser = entityManager.persistAndFlush(newUser);

        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should set default status to ACTIVE when not specified")
    void shouldSetDefaultStatusToActive() {
        User newUser = new User();
        newUser.setName("Jane Doe");
        newUser.setEmail("jane.doe2@example.com");
        newUser.setPassword("password123");

        User savedUser = entityManager.persistAndFlush(newUser);

        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find user by email using repository method")
    void shouldExistsByEmailReturnTrue() {
        entityManager.persistAndFlush(validUser);

        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldExistsByEmailReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }
}
