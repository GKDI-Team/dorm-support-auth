package com.dormsupport.auth.integration;

import com.dormsupport.auth.api.LoginRequest;
import com.dormsupport.auth.api.LoginResponse;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "auth.seed.enabled=true",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:db/init.sql"
        }
)
@AutoConfigureTestRestTemplate
class AuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void seededUserCanLoginAndAccessCurrentUserEndpoint() {
        // 1. Проверяем, что сидер создал пользователя.
        var seededUser = userRepository.findByLogin("german");

        assertThat(seededUser)
                .as("Пользователь german должен быть создан сидером")
                .isPresent();

        // 2. Выполняем вход через HTTP API.
        LoginRequest loginRequest = new LoginRequest("german", "123456");

        ResponseEntity<LoginResponse> loginResponse =
                restTemplate.postForEntity(
                        "/login",
                        loginRequest,
                        LoginResponse.class
                );

        assertThat(loginResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(loginResponse.getBody()).isNotNull();

        LoginResponse loginBody = loginResponse.getBody();

        assertThat(loginBody.accessToken()).isNotBlank();
        assertThat(loginBody.user().login()).isEqualTo("german");
        assertThat(loginBody.user().id()).isEqualTo(seededUser.get().getId());

        // 3. Используем JWT для запроса текущего пользователя.
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(loginBody.accessToken());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<UserResponse> currentUserResponse =
                restTemplate.exchange(
                        "/me",
                        HttpMethod.GET,
                        requestEntity,
                        UserResponse.class
                );

        assertThat(currentUserResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(currentUserResponse.getBody()).isNotNull();

        UserResponse currentUser = currentUserResponse.getBody();

        // 4. Проверяем, что /me вернул того же пользователя.
        assertThat(currentUser.id()).isEqualTo(loginBody.user().id());
        assertThat(currentUser.login()).isEqualTo(loginBody.user().login());
        assertThat(currentUser.fullName()).isEqualTo(loginBody.user().fullName());
        assertThat(currentUser.role()).isEqualTo(loginBody.user().role());
    }
}