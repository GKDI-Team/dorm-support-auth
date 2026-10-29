package com.dormsupport.auth.support;

import com.dormsupport.auth.api.CreateUserRequest;
import com.dormsupport.auth.api.LoginRequest;
import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRole;

import java.util.UUID;

public final class UserTestFixtures {

    private static final UUID DEFAULT_BLOCK_ID =
            UUID.fromString("11111111");

    private UserTestFixtures() {
        // Utility class.
    }

    public static User createUser() {
        return createUser(
                "testuser",
                "test-password-hash",
                "Test User",
                UserRole.RESIDENT,
                DEFAULT_BLOCK_ID,
                "101"
        );
    }

    public static User createUser(
            String login,
            String passwordHash,
            String fullName,
            UserRole role,
            UUID blockId,
            String roomNumber
    ) {
        return new User(
                login,
                passwordHash,
                fullName,
                role,
                blockId,
                roomNumber
        );
    }

    public static LoginRequest createLoginRequest() {
        return createLoginRequest("testuser", "test-password");
    }

    public static LoginRequest createLoginRequest(
            String login,
            String password
    ) {
        return new LoginRequest(login, password);
    }

    public static CreateUserRequest createUserRequest() {
        return createUserRequest(
                "newtestuser",
                "test-password",
                "New Test User",
                UserRole.RESIDENT,
                DEFAULT_BLOCK_ID,
                "102"
        );
    }

    public static CreateUserRequest createUserRequest(
            String login,
            String password,
            String fullName,
            UserRole role,
            UUID blockId,
            String roomNumber
    ) {
        return new CreateUserRequest(
                login,
                password,
                fullName,
                role,
                blockId,
                roomNumber
        );
    }

    public static UUID defaultBlockId() {
        return DEFAULT_BLOCK_ID;
    }
}