package com.example.blogbackend.controller;

import com.example.blogbackend.common.Result;
import com.example.blogbackend.dto.UpdateUserRequest;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.security.UserContext;
import com.example.blogbackend.service.UserService;
import com.example.blogbackend.vo.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<UserResponse> getUser(
            @PathVariable Long id
    ) {
        requireCurrentUser(id);
        UserResponse response = userService.getUserById(id);

        return Result.success(response);
    }

    @PutMapping("/{id}")
    public Result<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        requireCurrentUser(id);
        UserResponse response = userService.updateUser(id, request);

        return Result.success(response);
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteUser(
            @PathVariable Long id
    ) {
        requireCurrentUser(id);
        userService.disableUser(id);
        return Result.success();
    }

    private void requireCurrentUser(Long userId) {
        if (!Objects.equals(
                UserContext.getRequiredUserId(),
                userId
        )) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "无权操作其他用户"
            );
        }
    }
}
