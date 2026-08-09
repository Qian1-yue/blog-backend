package com.example.blogbackend.vo;

import com.example.blogbackend.dto.LoginRequest;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        LoginUserResponse user
) {
}
