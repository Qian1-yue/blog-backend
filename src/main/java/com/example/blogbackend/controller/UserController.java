package com.example.blogbackend.controller;

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

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response =
                userService.createUser(request);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public PageResponse<UserResponse> listUsers(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于1")
            long current,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量不能小于1")
            @Max(value = 100, message = "每页数量不能超过100")
            long size,

            @RequestParam(required = false)
            String username,

            @RequestParam(required = false)
            Integer status) {

        return userService.listUsers(
                current,
                size,
                username,
                status
        );
    }
}
