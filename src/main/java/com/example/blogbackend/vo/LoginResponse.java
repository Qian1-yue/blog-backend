package com.example.blogbackend.vo;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        LoginUserResponse user
) {
}
