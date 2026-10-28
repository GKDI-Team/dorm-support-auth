package com.dormsupport.auth.api;

import com.dormsupport.auth.user.User;
import com.dormsupport.auth.user.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String login,
        String fullName,
        UserRole role,
        String block,
        String roomNumber,
        Instant createdAt) {

    /** Compatibility constructor for old test fixtures. */
    public UserResponse(UUID id, String login, String fullName, UserRole role,
                        UUID blockId, String roomNumber, Instant createdAt) {
        this(id, login, fullName, role,
                blockId == null ? null : blockId.toString(), roomNumber, createdAt);
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLogin(),
                user.getFullName(),
                user.getRole(),
                user.getBlock(),
                user.getRoomNumber(),
                user.getCreatedAt());
    }
}
