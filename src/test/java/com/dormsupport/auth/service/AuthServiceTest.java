package com.dormsupport.auth.service;

import com.dormsupport.auth.api.CreateUserRequest;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    private UUID adminId;
    private UUID blockId;
    private User admin;

    private final String token = "admin-jwt-token";

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService
        );

        adminId = UUID.randomUUID();
        blockId = UUID.randomUUID();

        admin = mock(User.class);

        when(jwtService.getUserId(token)).thenReturn(adminId);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(admin.getRole()).thenReturn(UserRole.ADMIN);
    }

    @Test
    void createUser_whenCreatorIsAdmin_createsUser() {
        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        when(userRepository.existsByLogin("student123")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        when(userRepository.saveAndFlush(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.createUser(request, token);

        assertNotNull(response);
        verify(userRepository).saveAndFlush(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void createUser_whenCreatorIsNotAdmin_throwsAdminAccessRequiredException() {
        when(admin.getRole()).thenReturn(UserRole.RESIDENT);

        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        assertThrows(
                AdminAccessRequiredException.class,
                () -> authService.createUser(request, token)
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void createUser_whenLoginAlreadyExists_throwsLoginAlreadyExistsException() {
        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        when(userRepository.existsByLogin("student123")).thenReturn(true);

        assertThrows(
                LoginAlreadyExistsException.class,
                () -> authService.createUser(request, token)
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void createUser_whenRequestedRoleIsAdmin_throwsInvalidUserRoleException() {
        CreateUserRequest request = new CreateUserRequest(
                "newadmin",
                "password123",
                "New Admin",
                UserRole.ADMIN,
                blockId,
                "101"
        );

        assertThrows(
                InvalidUserRoleException.class,
                () -> authService.createUser(request, token)
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void createUser_whenSaveFailsDueToDuplicateLogin_throwsLoginAlreadyExistsException() {
        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        when(userRepository.existsByLogin("student123")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate login"));

        assertThrows(
                LoginAlreadyExistsException.class,
                () -> authService.createUser(request, token)
        );
    }

    @Test
    void createUser_encodesPasswordBeforeSaving() {
        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        when(userRepository.existsByLogin("student123")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.createUser(request, token);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).saveAndFlush(argThat(user ->
                "encoded-password".equals(user.getPasswordHash())
        ));
    }

    @Test
    void createUser_whenTokenDoesNotIdentifyUser_throwsInvalidTokenException() {
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        assertThrows(
                InvalidTokenException.class,
                () -> authService.createUser(request, token)
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void createUser_withRealBcryptEncoder_doesNotStorePlainPassword() {
        PasswordEncoder realEncoder = new BCryptPasswordEncoder();

        AuthService serviceWithRealEncoder = new AuthService(
                userRepository,
                realEncoder,
                jwtService
        );

        CreateUserRequest request = new CreateUserRequest(
                "student123",
                "password123",
                "Ivan Ivanov",
                UserRole.RESIDENT,
                blockId,
                "101"
        );

        when(userRepository.existsByLogin("student123")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        serviceWithRealEncoder.createUser(request, token);

        verify(userRepository).saveAndFlush(argThat(user ->
                !user.getPasswordHash().equals("password123")
                        && realEncoder.matches("password123", user.getPasswordHash())
        ));
    }
}