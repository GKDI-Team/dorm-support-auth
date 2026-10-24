package com.dormsupport.auth.api;

import com.dormsupport.auth.service.AuthService;
import com.dormsupport.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationService authenticationService;

    public AuthController(AuthService authService, AuthenticationService authenticationService) {
        this.authService = authService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser(
            @RequestHeader("X-Session-Id") UUID sessionId) {
        return ResponseEntity.ok(authenticationService.currentUser(sessionId));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("X-Session-Id") UUID sessionId) {
        authenticationService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/v1/sessions/{sessionId}")
    public ResponseEntity<SessionInfoResponse> validateSession(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(authenticationService.validateSession(sessionId));
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader("X-Session-Id") UUID sessionId,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = authService.createUser(request, sessionId);
        return ResponseEntity
                .created(URI.create("/api/v1/admin/users/" + response.id()))
                .body(response);
    }
}
