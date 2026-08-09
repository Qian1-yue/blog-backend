package com.example.blogbackend.vo;

public record LoginUserResponse(
        Long id,
        String username,
        String nickname
) {
}
