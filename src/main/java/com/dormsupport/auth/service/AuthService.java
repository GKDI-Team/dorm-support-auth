package com.dormsupport.auth.service;

import com.dormsupport.auth.api.CreateUserRequest;
import com.dormsupport.auth.api.UserResponse;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String accessToken) {
        User creator = userRepository.findById(jwtService.getUserId(accessToken))
                .orElseThrow(InvalidTokenException::new);

        if (creator.getRole() != UserRole.ADMIN) {
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
                request.block(),
                request.roomNumber());

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            // Protect against a concurrent registration with the same login.
            throw new LoginAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
