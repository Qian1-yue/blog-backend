package com.example.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "评论不能为空")
        @Size(
                max = 1000,
                message = "评论内容最多1000个字符"
        )
        String content
        ){

}
