package com.example.blogbackend.security;

import com.example.blogbackend.common.Result;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final JsonMapper jsonMapper;

    public TokenAuthenticationFilter(
            TokenService tokenService,
            UserMapper userMapper,
            JsonMapper jsonMapper) {
        this.tokenService = tokenService;
        this.userMapper = userMapper;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        UserContext.clear();
        SecurityContextHolder.clearContext();

        try {
            String token = extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));

            if (StringUtils.hasText(token)) {
                try {
                    authenticate(token);
                } catch (RuntimeException exception) {
                    log.error("认证服务暂时不可用", exception);
                    writeServiceUnavailable(response);
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void authenticate(String token) {
        tokenService.findSession(token).ifPresent(session -> {
            UserEntity user = userMapper.selectById(session.userId());

            if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                tokenService.removeToken(token);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            session,
                            token,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserContext.set(session);
        });
    }

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization)
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }

        String token = authorization.substring(7).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    private void writeServiceUnavailable(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer();
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                jsonMapper.writeValueAsString(
                        Result.failure(503, "认证服务暂时不可用，请稍后重试")
                )
        );
        response.flushBuffer();
    }
}
