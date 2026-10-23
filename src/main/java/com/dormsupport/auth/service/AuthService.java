package com.dormsupport.auth.service;

import com.dormsupport.auth.api.RegisterRequest;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByLogin(request.login())) {
            throw new LoginAlreadyExistsException();
        }

        User user = new User(
                request.login(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                UserRole.RESIDENT,
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
