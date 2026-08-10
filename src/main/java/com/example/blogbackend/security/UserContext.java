package com.example.blogbackend.security;

import com.example.blogbackend.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class UserContext {
    private static final ThreadLocal<LoginSession> SESSION_HOLDER = new ThreadLocal<>();

    public UserContext() {
    }

    public static void set(LoginSession session) {
        SESSION_HOLDER.set(session);
    }

    public static LoginSession getRequiredSession() {
        LoginSession session = SESSION_HOLDER.get();

        if (session == null) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "请先登录"
            );
        }
        return session;
    }

    public static Long getRequiredUserId() {
        return getRequiredSession().userId();
    }

    public static void clear() {
        SESSION_HOLDER.remove();
    }
}
