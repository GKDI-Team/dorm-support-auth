package com.dormsupport.auth.service;

import com.dormsupport.auth.api.LoginRequest;
import com.dormsupport.auth.api.LoginResponse;
import com.dormsupport.auth.api.SessionInfoResponse;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.session.Session;
import com.dormsupport.auth.session.SessionRepository;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final Duration sessionTtl;

    public AuthenticationService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.session.ttl:24h}") Duration sessionTtl) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTtl = sessionTtl;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.login())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        Session session = new Session(user, Instant.now().plus(sessionTtl));
        Session savedSession = sessionRepository.saveAndFlush(session);
        return new LoginResponse(savedSession.getId(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID sessionId) {
        return UserResponse.from(activeSession(sessionId).getUser());
    }

    @Transactional(readOnly = true)
    public SessionInfoResponse validateSession(UUID sessionId) {
        Session session = activeSession(sessionId);
        return new SessionInfoResponse(
                session.getId(),
                session.getExpiresAt(),
                UserResponse.from(session.getUser()));
    }

    @Transactional
    public void logout(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.revoke();
            }
        });
    }

    private Session activeSession(UUID sessionId) {
        return sessionRepository
                .findByIdAndRevokedAtIsNullAndExpiresAtAfter(sessionId, Instant.now())
                .orElseThrow(SessionNotFoundException::new);
    }
}
