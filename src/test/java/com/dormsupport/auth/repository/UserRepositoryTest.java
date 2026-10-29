package com.dormsupport.auth.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "auth.seed.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never"
        }
)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByLogin() {
        User user = new User(
                "testuser",
                "password-hash",
                "Test User",
                UserRole.RESIDENT,
                null,
                null
        );

        userRepository.saveAndFlush(user);

        var result = userRepository.findByLogin("testuser");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getLogin()).isEqualTo("testuser");
    }

    @Test
    void shouldReturnTrueWhenLoginExists() {
        User user = new User(
                "existinguser",
                "password-hash",
                "Existing User",
                UserRole.RESIDENT,
                null,
                null
        );

        userRepository.saveAndFlush(user);

        assertThat(userRepository.existsByLogin("existinguser")).isTrue();
        assertThat(userRepository.existsByLogin("unknownuser")).isFalse();
    }

    @Test
    void shouldRejectDuplicateLogin() {
        User firstUser = new User(
                "duplicate",
                "password-hash-1",
                "First User",
                UserRole.RESIDENT,
                null,
                null
        );

        User secondUser = new User(
                "duplicate",
                "password-hash-2",
                "Second User",
                UserRole.RESIDENT,
                null,
                null
        );

        userRepository.saveAndFlush(firstUser);

        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}