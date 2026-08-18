package com.example.blogbackend.vo;

import java.time.LocalDateTime;

public record ArticleDetailResponse(
        Long id,
        String title,
        String summary,
        String content,
        Long authorId,
        String authorNickname,
        Long viewCount,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
