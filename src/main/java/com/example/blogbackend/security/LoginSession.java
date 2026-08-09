package com.example.blogbackend.security;

import java.time.Instant;

public record LoginSession(
        Long userId,
        String username,
        Instant expiresAt
) {
}
