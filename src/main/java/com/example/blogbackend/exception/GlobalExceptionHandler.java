package com.example.blogbackend.exception;


import com.example.blogbackend.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Map<String,Object>>> handleBusinessException(BusinessException exception) {
        Result<Map<String,Object>> result =
        Result.failure(
                exception.getCode(),
                exception.getMessage()
        );
        return ResponseEntity.status(exception.getStatus()).body(result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String,Object>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数不正确");

        return ResponseEntity
                .badRequest()
                .body(Result.failure(400, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Map<String,Object>>> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception
                .getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation ->
                        violation.getMessage())
                .orElse("请求参数不正确");

        return ResponseEntity
                .badRequest()
                .body(Result.failure(400, message));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Map<String,Object>>> handleDuplicateKeyException(DuplicateKeyException exception) {
        log.warn("数据库唯一约束冲突", exception);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.failure(
                        409,
                        "数据已存在，请勿重复提交"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Map<String, Object>>>
    handleUnknownException(Exception exception) {

        log.error("系统发生未处理异常", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(
                        500,
                        "服务器内部错误"
                ));
    }
}
