package com.example.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArticleRequest(

        @NotBlank(message = "文章标题不能为空")
        @Size(
                max = 200,
                message = "文章标题最多200个字符"
        )
        String title,
        @NotBlank(message = "文章摘要不能为空")
        @Size(
                max = 500,
                message = "文章摘要最多500个字符"
        )
        String summary,

        @NotBlank(message = "文章正文不能为空")
        String content
) {
}
