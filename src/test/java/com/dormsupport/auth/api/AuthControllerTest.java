package com.dormsupport.auth.api;

import com.dormsupport.auth.service.AuthService;
import com.dormsupport.auth.service.AuthenticationService;
import com.dormsupport.auth.service.InvalidTokenException;
import com.dormsupport.auth.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private UUID userId;
    private UUID blockId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        blockId = UUID.randomUUID();

        userResponse = new UserResponse(
                userId,
                "student123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101",
                Instant.parse("2026-01-01T10:00:00Z")
        );
    }

    @Test
    void login_whenCredentialsAreCorrect_returnsTokenAndUser() {
        LoginRequest request = new LoginRequest(
                "student123",
                "password123"
        );

        LoginResponse expectedResponse = new LoginResponse(
                "test-jwt-token",
                userResponse
        );

        when(authenticationService.login(request))
                .thenReturn(expectedResponse);

        ResponseEntity<LoginResponse> response =
                authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().accessToken());
        assertNotNull(response.getBody().user());
        assertEquals(userId, response.getBody().user().id());
        assertEquals("student123", response.getBody().user().login());
        assertEquals("Ivan Ivanov", response.getBody().user().fullName());

        verify(authenticationService).login(request);
    }

    @Test
    void login_whenServiceThrowsException_propagatesException() {
        LoginRequest request = new LoginRequest(
                "student123",
                "wrong-password"
        );

        when(authenticationService.login(request))
                .thenThrow(new com.dormsupport.auth.service.InvalidCredentialsException());

        assertThrows(
                com.dormsupport.auth.service.InvalidCredentialsException.class,
                () -> authController.login(request)
        );

        verify(authenticationService).login(request);
    }

    @Test
    void currentUser_whenAuthorizationHeaderIsValid_returnsUserProfile() {
        String authorization = "Bearer valid-jwt-token";

        when(authenticationService.currentUser("valid-jwt-token"))
                .thenReturn(userResponse);

        ResponseEntity<UserResponse> response =
                authController.currentUser(authorization);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());
        assertEquals("student123", response.getBody().login());
        assertEquals("Ivan Ivanov", response.getBody().fullName());
        assertEquals(UserRole.RESIDENT, response.getBody().role());

        verify(authenticationService).currentUser("valid-jwt-token");
    }

    @Test
    void currentUser_whenAuthorizationHeaderIsInvalid_throwsInvalidTokenException() {
        String authorization = "Basic invalid-token";

        assertThrows(
                InvalidTokenException.class,
                () -> authController.currentUser(authorization)
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    void currentUser_whenAuthorizationHeaderHasNoToken_passesEmptyTokenToService() {
        String authorization = "Bearer ";

        when(authenticationService.currentUser("")).thenReturn(userResponse);

        ResponseEntity<UserResponse> response =
                authController.currentUser(authorization);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());

        verify(authenticationService).currentUser("");
    }

    @Test
    void logout_returnsNoContent() {
        ResponseEntity<Void> response = authController.logout();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(authService, authenticationService);
    }

    @Test
    void createUser_whenRequestIsValid_returnsCreatedAndUserProfile() {
        CreateUserRequest request = new CreateUserRequest(
                "newstudent",
                "password123",
                "New Student",
                UserRole.RESIDENT,
                blockId,
                "205"
        );

        when(authService.createUser(request, "admin-token"))
                .thenReturn(userResponse);

        ResponseEntity<UserResponse> response =
                authController.createUser(
                        "Bearer admin-token",
                        request
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());
        assertEquals("student123", response.getBody().login());

        assertNotNull(response.getHeaders().getLocation());
        assertEquals(
                "/api/v1/admin/users/" + userId,
                response.getHeaders().getLocation().toString()
        );

        verify(authService).createUser(request, "admin-token");
    }

    @Test
    void createUser_whenAuthorizationHeaderIsInvalid_throwsInvalidTokenException() {
        CreateUserRequest request = new CreateUserRequest(
                "newstudent",
                "password123",
                "New Student",
                UserRole.RESIDENT,
                blockId,
                "205"
        );

        assertThrows(
                InvalidTokenException.class,
                () -> authController.createUser(
                        "Basic invalid-token",
                        request
                )
        );

        verifyNoInteractions(authService);
    }

    @Test
    void getUserProfile_whenUserExists_returnsUserProfile() {
        when(authService.getUserProfile(userId))
                .thenReturn(userResponse);

        ResponseEntity<UserResponse> response =
                authController.getUserProfile(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());
        assertEquals("student123", response.getBody().login());
        assertEquals("Ivan Ivanov", response.getBody().fullName());

        verify(authService).getUserProfile(userId);
    }

    @Test
    void getUserProfile_whenServiceThrowsException_propagatesException() {
        when(authService.getUserProfile(userId))
                .thenThrow(new com.dormsupport.auth.service.UserNotFoundException());

        assertThrows(
                com.dormsupport.auth.service.UserNotFoundException.class,
                () -> authController.getUserProfile(userId)
        );

        verify(authService).getUserProfile(userId);
    }
}