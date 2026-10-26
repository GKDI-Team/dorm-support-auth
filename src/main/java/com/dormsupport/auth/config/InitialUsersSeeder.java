package com.dormsupport.auth.config;

import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class InitialUsersSeeder implements CommandLineRunner {
    private static final String DEVELOPMENT_PASSWORD = "123456";
    private static final UUID DEVELOPMENT_BLOCK_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

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
        User user = userRepository.findByLogin(login).orElseGet(() -> new User(
                login,
                passwordEncoder.encode(DEVELOPMENT_PASSWORD),
                fullName,
                role,
                role == UserRole.RESIDENT ? DEVELOPMENT_BLOCK_ID : null,
                roomNumber));

        if (role == UserRole.RESIDENT
                && (user.getBlockId() == null || user.getRoomNumber() == null)) {
            user.setBlockId(DEVELOPMENT_BLOCK_ID);
            user.setRoomNumber(roomNumber);
            userRepository.save(user);
        }
    }
}
