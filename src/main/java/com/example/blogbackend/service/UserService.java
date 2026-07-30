package com.example.blogbackend.service;

import com.example.blogbackend.dto.CreateUserRequest;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.vo.UserResponse;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final Map<Long, UserEntity> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserResponse createUser(CreateUserRequest request) {
        Long id = idGenerator.incrementAndGet();

        UserEntity user = new UserEntity(
                id,
                request.username(),
                request.nickname()
        );

        users.put(id, user);
        return convertToResponse(user);
    }

    public Optional<UserResponse> findById(Long id) {
        UserEntity user = users.get(id);

        if (user == null) {
            return Optional.empty();
        }

        return Optional.of(convertToResponse(user));
    }

    private UserResponse convertToResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname()
        );
    }
}
