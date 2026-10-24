package com.dormsupport.auth.service;

import com.dormsupport.auth.api.LoginRequest;
import com.dormsupport.auth.api.LoginResponse;
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
        Session session = sessionRepository
                .findByIdAndRevokedAtIsNullAndExpiresAtAfter(sessionId, Instant.now())
                .orElseThrow(SessionNotFoundException::new);

        return UserResponse.from(session.getUser());
    }
}
