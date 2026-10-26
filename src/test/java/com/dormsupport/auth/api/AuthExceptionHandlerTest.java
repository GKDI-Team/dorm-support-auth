// TODO: Test mapping of authentication and validation exceptions to API errors.
package com.dormsupport.auth.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class AuthExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void loginAlreadyExists_returnsConflict() {
        var response = exceptionHandler.handleLoginAlreadyExists();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LOGIN_ALREADY_EXISTS", response.getBody().code());
        assertEquals(
                "Login is already registered",
                response.getBody().message()
        );
    }

    @Test
    void invalidToken_returnsUnauthorized() {
        var response = exceptionHandler.handleInvalidToken();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNAUTHORIZED", response.getBody().code());
        assertEquals(
                "Bearer token is missing, invalid or expired",
                response.getBody().message()
        );
    }

    @Test
    void userNotFound_returnsNotFound() {
        var response = exceptionHandler.handleUserNotFound();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("USER_NOT_FOUND", response.getBody().code());
        assertEquals("User was not found", response.getBody().message());
    }

    @Test
    void missingHeader_returnsUnauthorized() {
        var response = exceptionHandler.handleMissingHeader();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNAUTHORIZED", response.getBody().code());
        assertEquals(
                "Authorization Bearer token is required",
                response.getBody().message()
        );
    }

    @Test
    void invalidCredentials_returnsUnauthorized() {
        var response = exceptionHandler.handleInvalidCredentials();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_CREDENTIALS", response.getBody().code());
        assertEquals(
                "Invalid login or password",
                response.getBody().message()
        );
    }

    @Test
    void adminAccessRequired_returnsForbidden() {
        var response = exceptionHandler.handleAdminAccessRequired();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FORBIDDEN", response.getBody().code());
        assertEquals(
                "Administrator role is required",
                response.getBody().message()
        );
    }

    @Test
    void invalidUserRole_returnsBadRequest() {
        var response = exceptionHandler.handleInvalidUserRole();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ROLE", response.getBody().code());
        assertEquals(
                "ADMIN accounts cannot be created through this endpoint",
                response.getBody().message()
        );
    }

    @Test
    void validationError_returnsBadRequest() {
        var response = exceptionHandler.handleValidationError();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals(
                "Request contains invalid fields",
                response.getBody().message()
        );
    }

    @Test
    void unreadableRequest_returnsBadRequest() {
        var response = exceptionHandler.handleUnreadableRequest();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_REQUEST", response.getBody().code());
        assertEquals(
                "Request body is invalid",
                response.getBody().message()
        );
    }
}