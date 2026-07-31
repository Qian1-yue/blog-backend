package com.example.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest (
            @NotBlank(message = "用户名不能为空")
            @Size(max = 50,message = "用户名最多50个字符")
            String username,

            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 72,
                    message = "密码长度必须在6到72个字符之间")
            String password,

            @NotBlank(message = "昵称不能为空")
            @Size(max = 50, message = "昵称最多50个字符")
            String nickname
    ) {

    }

