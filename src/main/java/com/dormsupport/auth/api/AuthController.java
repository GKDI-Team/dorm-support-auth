package com.dormsupport.auth.api;

import com.dormsupport.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
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
