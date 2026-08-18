package com.example.blogbackend.vo;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long userId,
        String userNickname,
        LocalDateTime createTime
) {
}
