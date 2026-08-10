package com.example.blogbackend.vo;

import java.time.LocalDateTime;

public record ArticleListItemResponse(
        Long id,
        String title,
        String summary,
        Long authorId,
        Long viewCount,
        LocalDateTime createTime
) {
}
