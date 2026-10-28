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
        UUID blockId,
        String roomNumber,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLogin(),
                user.getFullName(),
                user.getRole(),
                user.getBlockId(),
                user.getRoomNumber(),
                user.getCreatedAt());
    }
}
