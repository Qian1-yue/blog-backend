package com.example.blogbackend.vo;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

}
