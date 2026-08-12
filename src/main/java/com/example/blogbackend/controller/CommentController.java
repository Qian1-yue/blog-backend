package com.example.blogbackend.controller;

import com.example.blogbackend.common.Result;
import com.example.blogbackend.dto.CreateCommentRequest;
import com.example.blogbackend.security.LoginRequired;
import com.example.blogbackend.security.UserContext;
import com.example.blogbackend.service.CommentService;
import com.example.blogbackend.vo.CommentResponse;
import com.example.blogbackend.vo.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article/{articleId}/comments")
@Validated
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @LoginRequired
    @PostMapping
    public ResponseEntity<Result<CommentResponse>> createComment(
            @PathVariable Long articleId,

            @Valid
            @RequestBody
            CreateCommentRequest request) {
        Long currentUserId =
                UserContext.getRequiredUserId();

        CommentResponse response =
                commentService.createComment(articleId, currentUserId, request);

        return ResponseEntity
                .status(201)
                .body(Result.success(response));
    }

    @GetMapping
    public Result<PageResponse<CommentResponse>> listComments(
            @PathVariable Long articleId,

            @RequestParam(
                    name = "page",
                    defaultValue = "1"
            )
            @Min(value = 1,message = "页码不能小于1")
            long page,

            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            @Min(value = 1,message = "每页数量不能小于1")
            @Max(value = 100,message = "每页数量不能超过100")
            long size) {

        PageResponse<CommentResponse> response =
                commentService.listComments(articleId, page, size);

        return Result.success(response);
    }
}
