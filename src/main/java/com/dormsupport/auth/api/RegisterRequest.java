package com.dormsupport.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRequest(
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
        UUID blockId,

        @NotBlank
        @Size(max = 20)
        String roomNumber) {
}
