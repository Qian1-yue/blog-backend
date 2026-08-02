package com.example.blogbackend.dto;

import jakarta.validation.constraints.*;

public record UpdateUserRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50,message = "昵称最多50个字符")
        String nickname,

        @NotNull(message = "用户状态不能为空")
        @Min(value = 0,message = "用户状态只能为0或1")
        @Max(value = 1,message = "用户状态只能为0或1")
        Integer status
) {

}
