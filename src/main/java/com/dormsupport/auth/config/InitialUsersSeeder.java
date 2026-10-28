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
    private static final String DEVELOPMENT_BLOCK = "11111111";

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

        seed("german.admin", "German", UserRole.ADMIN, null);
        seed("kate.admin", "Kate", UserRole.ADMIN, null);
        seed("java.admin", "Java", UserRole.ADMIN, null);
        seed("demian.admin", "Demian", UserRole.ADMIN, null);
        seed("german", "German", UserRole.RESIDENT, "101");
        seed("kate", "Kate", UserRole.RESIDENT, "102");
        seed("java", "Java", UserRole.RESIDENT, "103");
        seed("demian", "Demian", UserRole.RESIDENT, "104");
    }

    private void seed(String login, String fullName, UserRole role, String roomNumber) {
        var existingUser = userRepository.findByLogin(login);

        if (existingUser.isEmpty()) {
            User user = new User(
                    login,
                    passwordEncoder.encode(DEVELOPMENT_PASSWORD),
                    fullName,
                    role,
                    role == UserRole.RESIDENT ? DEVELOPMENT_BLOCK : null,
                    roomNumber
            );

            userRepository.save(user);
            return;
        }

        User user = existingUser.get();

        if (role == UserRole.RESIDENT
                && (user.getBlock() == null || user.getRoomNumber() == null)) {
            user.setBlock(DEVELOPMENT_BLOCK);
            user.setRoomNumber(roomNumber);
            userRepository.save(user);
        }
    }
}
