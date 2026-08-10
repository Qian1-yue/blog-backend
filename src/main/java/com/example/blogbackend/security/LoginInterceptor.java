package com.example.blogbackend.security;

import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.http.HttpHeaders;


@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;
    public LoginInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        UserContext.clear();

        if(!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean loginRequired =
                handlerMethod.hasMethodAnnotation(
                        LoginRequired.class
                )
                || handlerMethod
                        .getBeanType()
                        .isAnnotationPresent(
                                LoginRequired.class
                        );

        if(!loginRequired) {
            return true;
        }

        String authorization = request.getHeader(
                HttpHeaders.AUTHORIZATION
        );

        String token = extractBearerToken(authorization);

        if(!StringUtils.hasText(token)) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "请先登录"
            );
        }

        LoginSession session = tokenService
                .findSession(token)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED,
                        "登录状态已失效，请重新登录"
                ));

        UserContext.set(session);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        UserContext.clear();
    }
    private String extractBearerToken(
            String authorization) {

        if (!StringUtils.hasText(authorization)) {
            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization
                .substring(7)
                .trim();
    }
}
