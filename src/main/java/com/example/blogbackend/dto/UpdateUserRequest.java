package com.example.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50,message = "昵称最多50个字符")
        String nickname
) {

}
