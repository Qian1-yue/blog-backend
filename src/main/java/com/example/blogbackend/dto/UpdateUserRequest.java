package com.example.blogbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50,message = "昵称最多50个字符")
        String nickname,

        @NotBlank(message = "用户状态不能为空")
        @Min(value = 0,message = "用户状态只能为0或1")
        @Max(value = 1,message = "用户状态只能为0或1")
        Integer status
) {

}
