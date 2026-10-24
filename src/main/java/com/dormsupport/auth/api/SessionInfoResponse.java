package com.dormsupport.auth.api;

import java.time.Instant;
import java.util.UUID;

public record SessionInfoResponse(
        UUID sessionId,
        Instant expiresAt,
        UserResponse user) {
}
