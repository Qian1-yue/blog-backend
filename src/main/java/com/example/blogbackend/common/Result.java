package com.example.blogbackend.common;

import java.util.Map;

public record Result<T> (
        int code,
        String msg,
        T data
){
    public static <T> Result<T> success (T data) {
        return new Result<T>(200,"success",data);
    }

    public static Result<Map<String,Object>> success () {
        return new Result<>(
                200,
                "success",
                Map.of()
        );
    }

    public static Result<Map<String,Object>> failure (
            int code,
            String message
    ){
        return new Result<>(
                code,
                message,
                Map.of()
        );
    }
}
