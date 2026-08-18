package com.example.blogbackend.controller;


import com.example.blogbackend.common.Result;
import com.example.blogbackend.dto.CreateArticleRequest;
import com.example.blogbackend.dto.UpdateArticleRequest;
import com.example.blogbackend.security.UserContext;
import com.example.blogbackend.service.ArticleService;
import com.example.blogbackend.vo.ArticleDetailResponse;
import com.example.blogbackend.vo.ArticleListItemResponse;
import com.example.blogbackend.vo.ArticleResponse;
import com.example.blogbackend.vo.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@Validated
public class ArticleController {
    private final ArticleService articleService;
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<Result<ArticleResponse>> createArticle(
            @Valid
            @RequestBody
            CreateArticleRequest request) {
        Long currentUserId =
                UserContext.getRequiredUserId();

        ArticleResponse response = articleService.createArticle(
                request,
                currentUserId
        );

        return ResponseEntity
                .status(201)
                .body(Result.success(response));
    }

    @GetMapping
    public Result<PageResponse<ArticleListItemResponse>> listArticles(
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

        PageResponse<ArticleListItemResponse> response =
                articleService.listPublishedArticles(page, size);

        return Result.success(response);
    }

    @GetMapping("/mine")
    public Result<PageResponse<ArticleListItemResponse>> listMyArticles(
            @RequestParam(
                    name = "page",
                    defaultValue = "1"
            )
            @Min(value = 1, message = "页码不能小于1")
            long page,

            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            @Min(value = 1, message = "每页数量不能小于1")
            @Max(value = 100, message = "每页数量不能超过100")
            long size) {

        return Result.success(
                articleService.listUserArticles(
                        UserContext.getRequiredUserId(),
                        page,
                        size
                )
        );
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailResponse> getArticleDetail(
            @PathVariable Long id) {
        ArticleDetailResponse response =
                articleService.getArticleDetail(id);

        return Result.success(response);
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteArticle(
            @PathVariable Long id) {
        Long currentUserId =
                UserContext.getRequiredUserId();

        articleService.deleteArticle(
                id,
                currentUserId
        );
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<ArticleDetailResponse> updateArticle(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateArticleRequest request) {
        Long currentUserId =
                UserContext.getRequiredUserId();

        ArticleDetailResponse response =
                articleService.updateArticle(
                        id,
                        currentUserId,
                        request
        );

        return Result.success(response);
    }

    @GetMapping("/hot")
    public Result<List<ArticleListItemResponse>> hotArticles(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return Result.success(
                articleService.listHotArticles(limit)
        );
    }
}
