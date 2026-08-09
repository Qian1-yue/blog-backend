package com.example.blogbackend.security;

public record GeneratedToken(
        String token,
        long expiresInSeconds
) {
}
