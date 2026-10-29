package com.dormsupport.auth.service;

import com.dormsupport.auth.api.LoginRequest;
import com.dormsupport.auth.api.LoginResponse;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = mock(User.class);
    }

    @Test
    void login_whenCredentialsAreCorrect_returnsTokenAndUser() {
        LoginRequest request = new LoginRequest("student123", "password123");

        when(user.getId()).thenReturn(userId);
        when(user.getLogin()).thenReturn("student123");
        when(user.getFullName()).thenReturn("Ivan Ivanov");
        when(user.getRole()).thenReturn(UserRole.RESIDENT);
        when(user.getBlock()).thenReturn(null);
        when(user.getRoomNumber()).thenReturn("101");
        when(user.getCreatedAt()).thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
        when(user.getPasswordHash()).thenReturn("encoded-password");

        when(userRepository.findByLogin("student123"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);
        when(jwtService.createToken(user))
                .thenReturn("test-jwt-token");

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.accessToken());
        assertNotNull(response.user());
        assertEquals(userId, response.user().id());
        assertEquals("student123", response.user().login());
        assertEquals("Ivan Ivanov", response.user().fullName());
        assertEquals(UserRole.RESIDENT, response.user().role());

        verify(userRepository).findByLogin("student123");
        verify(passwordEncoder).matches("password123", "encoded-password");
        verify(jwtService).createToken(user);
    }

    @Test
    void login_whenUserDoesNotExist_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("unknown", "password123");

        when(userRepository.findByLogin("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authenticationService.login(request)
        );

        verify(userRepository).findByLogin("unknown");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_whenPasswordIsIncorrect_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("student123", "wrong-password");

        when(userRepository.findByLogin("student123"))
                .thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn("encoded-password");
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        assertThrows(
            InvalidCredentialsException.class,
            () -> authenticationService.login(request)
        );

        verify(userRepository).findByLogin("student123");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void currentUser_whenTokenIsValid_returnsUserProfile() {
        String token = "valid-jwt-token";

        when(jwtService.getUserId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);
        when(user.getLogin()).thenReturn("student123");
        when(user.getFullName()).thenReturn("Ivan Ivanov");
        when(user.getRole()).thenReturn(UserRole.RESIDENT);
        when(user.getBlock()).thenReturn(null);
        when(user.getRoomNumber()).thenReturn("101");
        when(user.getCreatedAt()).thenReturn(Instant.parse("2026-01-01T10:00:00Z"));

        UserResponse response = authenticationService.currentUser(token);

        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals("student123", response.login());
        assertEquals("Ivan Ivanov", response.fullName());
        assertEquals(UserRole.RESIDENT, response.role());
        assertEquals("101", response.roomNumber());

        verify(jwtService).getUserId(token);
        verify(userRepository).findById(userId);
    }

    @Test
    void currentUser_whenUserDoesNotExist_throwsInvalidTokenException() {
        String token = "valid-jwt-token";

        when(jwtService.getUserId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> authenticationService.currentUser(token)
        );

        verify(jwtService).getUserId(token);
        verify(userRepository).findById(userId);
    }
}