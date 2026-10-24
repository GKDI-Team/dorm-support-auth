package com.dormsupport.auth.api;

public record LoginResponse(String accessToken, UserResponse user) {
}
