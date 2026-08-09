package com.example.blogbackend.controller;

import com.example.blogbackend.common.Result;
import com.example.blogbackend.dto.LoginRequest;
import com.example.blogbackend.dto.RegisterRequest;
import com.example.blogbackend.service.AuthService;
import com.example.blogbackend.vo.LoginResponse;
import com.example.blogbackend.vo.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Result<UserResponse>> register(
            @Valid
            @RequestBody
            RegisterRequest request) {

        UserResponse response =
                authService.register(request);

        return ResponseEntity
                .status(201)
                .body(Result.success(response));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        LoginResponse response =
                authService.login(request);

        return Result.success(response);
    }
}
