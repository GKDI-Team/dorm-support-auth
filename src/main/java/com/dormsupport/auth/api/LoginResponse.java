package com.dormsupport.auth.api;

import java.util.UUID;

public record LoginResponse(UUID sessionId, UserResponse user) {
}
