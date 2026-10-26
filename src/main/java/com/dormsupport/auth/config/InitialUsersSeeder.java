package com.dormsupport.auth.config;

import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialUsersSeeder implements CommandLineRunner {
    private static final String DEVELOPMENT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public InitialUsersSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.seed.enabled:false}") boolean enabled) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        seed("german.admin", "German", UserRole.ADMIN);
        seed("kate.admin", "Kate", UserRole.ADMIN);
        seed("java.admin", "Java", UserRole.ADMIN);
        seed("demian.admin", "Demian", UserRole.ADMIN);
        seed("german", "German", UserRole.RESIDENT);
        seed("kate", "Kate", UserRole.RESIDENT);
        seed("java", "Java", UserRole.RESIDENT);
        seed("demian", "Demian", UserRole.RESIDENT);
    }

    private void seed(String login, String fullName, UserRole role) {
        if (!userRepository.existsByLogin(login)) {
            userRepository.save(new User(
                    login,
                    passwordEncoder.encode(DEVELOPMENT_PASSWORD),
                    fullName,
                    role,
                    null,
                    null));
        }
    }
}
