// TODO: Unit tests for JWT creation and validation.
// TODO: Check subject, role, expiration, modified signature and malformed tokens.
package com.dormsupport.auth.service;

import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-key-for-jwt-unit-tests";

    private JwtService jwtService;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, Duration.ofHours(1));

        userId = UUID.randomUUID();
        user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(UserRole.RESIDENT);
    }

    @Test
    void createToken_whenTokenIsValid_returnsUserId() {
        String token = jwtService.createToken(user);

        UUID actualUserId = jwtService.getUserId(token);

        assertEquals(userId, actualUserId);
    }

    @Test
    void createToken_containsCorrectSubjectAndRole() {
        String token = jwtService.createToken(user);

        String[] parts = token.split("\\.");
        String payload = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
        );

        assertTrue(payload.contains("\"sub\":\"" + userId + "\""));
        assertTrue(payload.contains("\"role\":\"RESIDENT\""));
    }

    @Test
    void createToken_containsExpirationInTheFuture() {
        String token = jwtService.createToken(user);

        String[] parts = token.split("\\.");
        String payload = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
        );

        long expiration = extractExpiration(payload);

        assertTrue(expiration > Instant.now().getEpochSecond());
    }

    @Test
    void getUserId_whenSignatureIsModified_throwsInvalidTokenException() {
        String token = jwtService.createToken(user);
        String[] parts = token.split("\\.");

        String modifiedSignature = parts[2].substring(0, parts[2].length() - 1)
                + (parts[2].endsWith("A") ? "B" : "A");

        String modifiedToken = parts[0] + "." + parts[1] + "." + modifiedSignature;

        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.getUserId(modifiedToken)
        );
    }

    @Test
    void getUserId_whenTokenIsMalformed_throwsInvalidTokenException() {
        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.getUserId("not-a-valid-jwt")
        );
    }

    @Test
    void getUserId_whenTokenIsExpired_throwsInvalidTokenException() {
        JwtService expiredJwtService =
                new JwtService(SECRET, Duration.ofSeconds(-1));

        String expiredToken = expiredJwtService.createToken(user);

        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.getUserId(expiredToken)
        );
    }

    @Test
    void getUserId_whenTokenWasSignedWithAnotherSecret_throwsInvalidTokenException() {
        JwtService anotherJwtService =
                new JwtService("another-test-secret", Duration.ofHours(1));

        String token = anotherJwtService.createToken(user);

        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.getUserId(token)
        );
    }

    private long extractExpiration(String payload) {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("\"exp\"\\s*:\\s*(\\d+)")
                        .matcher(payload);

        assertTrue(matcher.find(), "JWT payload must contain exp");
        return Long.parseLong(matcher.group(1));
    }
}