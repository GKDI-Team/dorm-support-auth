package com.dormsupport.auth.api;

import com.dormsupport.auth.service.LoginAlreadyExistsException;
import com.dormsupport.auth.service.AdminAccessRequiredException;
import com.dormsupport.auth.service.InvalidUserRoleException;
import com.dormsupport.auth.service.InvalidCredentialsException;
import com.dormsupport.auth.service.InvalidTokenException;
import com.dormsupport.auth.service.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleLoginAlreadyExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("LOGIN_ALREADY_EXISTS", "Login is already registered"));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidToken() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("UNAUTHORIZED", "Bearer token is missing, invalid or expired"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("USER_NOT_FOUND", "User was not found"));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeader() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("UNAUTHORIZED", "Authorization Bearer token is required"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("INVALID_CREDENTIALS", "Invalid login or password"));
    }

    @ExceptionHandler(AdminAccessRequiredException.class)
    public ResponseEntity<ApiError> handleAdminAccessRequired() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("FORBIDDEN", "Administrator role is required"));
    }

    @ExceptionHandler(InvalidUserRoleException.class)
    public ResponseEntity<ApiError> handleInvalidUserRole() {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_ROLE", "ADMIN accounts cannot be created through this endpoint"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationError() {
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_ERROR", "Request contains invalid fields"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest() {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_REQUEST", "Request body is invalid"));
    }
}
