package com.dormsupport.auth.service;

import com.dormsupport.auth.api.CreateUserRequest;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.session.Session;
import com.dormsupport.auth.session.SessionRepository;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID sessionId) {
        Session session = sessionRepository
                .findByIdAndRevokedAtIsNullAndExpiresAtAfter(sessionId, Instant.now())
                .orElseThrow(SessionNotFoundException::new);

        if (session.getUser().getRole() != UserRole.ADMIN) {
            throw new AdminAccessRequiredException();
        }

        if (request.role() == UserRole.ADMIN) {
            throw new InvalidUserRoleException();
        }

        if (userRepository.existsByLogin(request.login())) {
            throw new LoginAlreadyExistsException();
        }

        User user = new User(
                request.login(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                request.role(),
                request.blockId(),
                request.roomNumber());

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            // Protect against a concurrent registration with the same login.
            throw new LoginAlreadyExistsException();
        }
    }
}
