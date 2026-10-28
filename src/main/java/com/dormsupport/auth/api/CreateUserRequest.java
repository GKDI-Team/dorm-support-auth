package com.dormsupport.auth.api;

import com.dormsupport.auth.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;


public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        String login,

        @NotBlank
        @Size(min = 8, max = 128)
        String password,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotNull
        UserRole role,

        @NotBlank
        @Size(max = 50)
        String block,

        @NotBlank
        @Size(max = 20)
        String roomNumber) {

    /** Compatibility constructor for old test fixtures. */
    public CreateUserRequest(String login, String password, String fullName,
                             UserRole role, UUID blockId, String roomNumber) {
        this(login, password, fullName, role,
                blockId == null ? null : blockId.toString(), roomNumber);
    }
}
