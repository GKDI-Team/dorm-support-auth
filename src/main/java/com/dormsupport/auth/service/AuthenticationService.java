package com.dormsupport.auth.service;

import com.dormsupport.auth.api.LoginRequest;
import com.dormsupport.auth.api.LoginResponse;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.login())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String accessToken) {
        return UserResponse.from(userByToken(accessToken));
    }

    private User userByToken(String accessToken) {
        UUID userId = jwtService.getUserId(accessToken);
        return userRepository.findById(userId).orElseThrow(InvalidTokenException::new);
    }
}
