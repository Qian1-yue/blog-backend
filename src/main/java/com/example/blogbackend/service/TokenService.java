package com.example.blogbackend.service;

import com.example.blogbackend.security.GeneratedToken;
import com.example.blogbackend.security.LoginSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class TokenService {
    private static final String TOKEN_PREFIX = "login:token:";
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisJsonService redisJsonService;

    public TokenService(RedisJsonService redisJsonService) {
        this.redisJsonService = redisJsonService;
    }

    public GeneratedToken createToken(Long userId, String username) {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        LoginSession session = new LoginSession(
                userId,
                username,
                expiresAt
        );

        redisJsonService.set(
                buildKey(token),
                session,
                TOKEN_TTL
        );

        return new GeneratedToken(token, TOKEN_TTL.toSeconds());
    }

    public Optional<LoginSession> findSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<LoginSession> session = redisJsonService.get(
                buildKey(token),
                LoginSession.class
        );

        if (session.isPresent() && session.get().expiresAt().isBefore(Instant.now())) {
            removeToken(token);
            return Optional.empty();
        }

        return session;
    }

    public void removeToken(String token) {
        if (token != null && !token.isBlank()) {
            redisJsonService.delete(buildKey(token));
        }
    }

    private String buildKey(String token) {
        return TOKEN_PREFIX + token;
    }
}
