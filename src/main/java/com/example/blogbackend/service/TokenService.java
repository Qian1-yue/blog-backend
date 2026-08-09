package com.example.blogbackend.service;

import com.example.blogbackend.security.GeneratedToken;
import com.example.blogbackend.security.LoginSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    public GeneratedToken createToken(
            Long userId,
            String username
    ) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        LoginSession session = new LoginSession(userId, username, expiresAt);

        sessions.put(token, session);

        return new GeneratedToken(
                token,
                TOKEN_TTL.toSeconds()
        );
    }

    public Optional<LoginSession> findSession(String token) {

        LoginSession session = sessions.get(token);

        if (session == null) {
            return Optional.empty();
        }

        if(!session.expiresAt().isAfter(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }

        return Optional.of(session);
    }
    public void removeSession(String token) {
        sessions.remove(token);
    }
}
