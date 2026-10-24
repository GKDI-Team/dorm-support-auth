package com.dormsupport.auth.api;

import com.dormsupport.auth.service.AuthService;
import com.dormsupport.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(authenticationService.currentUser(bearerToken(authorization)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = authService.createUser(request, bearerToken(authorization));
        return ResponseEntity
                .created(URI.create("/api/v1/admin/users/" + response.id()))
                .body(response);
    }

    private static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new com.dormsupport.auth.service.InvalidTokenException();
        }
        return authorization.substring("Bearer ".length()).trim();
    }
}
