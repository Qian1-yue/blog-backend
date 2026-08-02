package com.example.blogbackend.controller;

import com.example.blogbackend.common.Result;
import com.example.blogbackend.dto.CreateUserRequest;
import com.example.blogbackend.dto.UpdateUserRequest;
import com.example.blogbackend.service.UserService;
import com.example.blogbackend.vo.PageResponse;
import com.example.blogbackend.vo.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        UserResponse response = userService.getUserById(id);

        return Result.success(response);
    }

    @PostMapping
    public ResponseEntity<Result<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(201)
                .body(Result.success(response));
    }

    @PutMapping("/{id}")
    public Result<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateUser(id, request);

        return Result.success(response);
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping
    public Result<PageResponse<UserResponse>> listUsers(
            @RequestParam(defaultValue = "1")
            @Min(value = 1,message = "页码不能小于1")
            long current,

            @RequestParam(defaultValue = "10")
            @Min(value = 1,message = "每页数量不能小于1")
            @Max(value = 100,message = "每页数量不能超过100")
            long size,

            @RequestParam(required = false)
            String username,

            @RequestParam(required = false)
            Integer status
    ) {
        PageResponse<UserResponse> response =
                userService.listUsers(current, size, username, status);

        return Result.success(response);
    }
}
