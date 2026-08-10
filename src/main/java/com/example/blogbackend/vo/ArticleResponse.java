package com.example.blogbackend.vo;

import java.time.LocalDateTime;

public record ArticleResponse(
        Long id,
        String title,
        String summary,
        String content,
        Long authorId,
        Long viewCount,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
