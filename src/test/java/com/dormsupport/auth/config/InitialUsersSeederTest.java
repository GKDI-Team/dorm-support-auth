package com.dormsupport.auth.config;

import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRepository;
import com.dormsupport.auth.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialUsersSeederTest {

    private static final String DEVELOPMENT_PASSWORD = "123456";
    private static final String DEVELOPMENT_BLOCK = "11111111";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private InitialUsersSeeder seeder;

    @BeforeEach
    void setUp() {
    }

    @Test
    void run_whenSeedingDisabled_doesNotCreateUsers() {
        seeder = new InitialUsersSeeder(
                userRepository, passwordEncoder, false);

        seeder.run();

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_whenSeedingEnabled_createsExpectedAccounts() {
        seeder = new InitialUsersSeeder(
                userRepository, passwordEncoder, true);

        when(passwordEncoder.encode(DEVELOPMENT_PASSWORD))
                .thenReturn("encoded-password");

        when(userRepository.findByLogin(anyString()))
                .thenReturn(Optional.empty());

        seeder.run();

        verify(userRepository, times(8)).findByLogin(anyString());
        verify(passwordEncoder, times(8))
                .encode(DEVELOPMENT_PASSWORD);
        verify(userRepository, times(8)).save(any(User.class));
    }

    @Test
    void run_whenAccountsAlreadyExist_doesNotEncodePasswordsOrSaveCompleteResidents() {
        seeder = new InitialUsersSeeder(
                userRepository, passwordEncoder, true);

        when(userRepository.findByLogin(anyString()))
                .thenAnswer(invocation -> {
                    String login = invocation.getArgument(0);

                    return Optional.of(switch (login) {
                        case "german.admin" -> createUser(
                                login, "German", UserRole.ADMIN, null, null);
                        case "kate.admin" -> createUser(
                                login, "Kate", UserRole.ADMIN, null, null);
                        case "java.admin" -> createUser(
                                login, "Java", UserRole.ADMIN, null, null);
                        case "demian.admin" -> createUser(
                                login, "Demian", UserRole.ADMIN, null, null);
                        case "german" -> createUser(
                                login, "German", UserRole.RESIDENT,
                                DEVELOPMENT_BLOCK, "101");
                        case "kate" -> createUser(
                                login, "Kate", UserRole.RESIDENT,
                                DEVELOPMENT_BLOCK, "102");
                        case "java" -> createUser(
                                login, "Java", UserRole.RESIDENT,
                                DEVELOPMENT_BLOCK, "103");
                        case "demian" -> createUser(
                                login, "Demian", UserRole.RESIDENT,
                                DEVELOPMENT_BLOCK, "104");
                        default -> throw new IllegalArgumentException(
                                "Unexpected login: " + login);
                    });
                });

        seeder.run();

        verify(userRepository, times(8)).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_whenExistingResidentHasMissingBlockId_updatesResident() {
        seeder = new InitialUsersSeeder(
                userRepository, passwordEncoder, true);

        User resident = createUser(
                "german", "German", UserRole.RESIDENT, null, "101");

        when(userRepository.findByLogin("german"))
                .thenReturn(Optional.of(resident));

        when(userRepository.findByLogin(argThat(
                login -> !"german".equals(login))))
                .thenReturn(Optional.of(createExistingAdminOrResident()));

        seeder.run();

        assertEquals(DEVELOPMENT_BLOCK, resident.getBlock());
        assertEquals("101", resident.getRoomNumber());
        verify(userRepository).save(resident);
    }

    @Test
    void run_whenExistingResidentHasMissingRoomNumber_updatesResident() {
        seeder = new InitialUsersSeeder(
                userRepository, passwordEncoder, true);

        User resident = createUser(
                "kate", "Kate", UserRole.RESIDENT,
                DEVELOPMENT_BLOCK, null);

        when(userRepository.findByLogin("kate"))
                .thenReturn(Optional.of(resident));

        when(userRepository.findByLogin(argThat(
                login -> !"kate".equals(login))))
                .thenReturn(Optional.of(createExistingAdminOrResident()));

        seeder.run();

        assertEquals(DEVELOPMENT_BLOCK, resident.getBlock());
        assertEquals("102", resident.getRoomNumber());
        verify(userRepository).save(resident);
    }

    private User createUser(
            String login,
            String fullName,
            UserRole role,
            String block,
            String roomNumber) {

        return new User(
                login,
                "encoded-password",
                fullName,
                role,
                block,
                roomNumber);
    }

    private User createExistingAdminOrResident() {
        return new User(
                "existing.user",
                "encoded-password",
                "Existing User",
                UserRole.ADMIN,
                null,
                null);
    }
}