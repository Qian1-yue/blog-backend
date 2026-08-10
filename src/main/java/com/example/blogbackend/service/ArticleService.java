package com.example.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogbackend.dto.CreateArticleRequest;
import com.example.blogbackend.entity.ArticleEntity;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.mapper.ArticleMapper;
import com.example.blogbackend.vo.ArticleListItemResponse;
import com.example.blogbackend.vo.ArticleResponse;
import com.example.blogbackend.vo.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    public ArticleService(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public ArticleResponse createArticle(
            CreateArticleRequest request,
            Long authorId
    ) {
        LocalDateTime now = LocalDateTime.now();

        ArticleEntity article = new ArticleEntity();
        article.setAuthorId(authorId);
        article.setCreateTime(now);
        article.setUpdateTime(now);
        article.setTitle(request.title().trim());
        article.setContent(request.content());
        article.setSummary(request.summary().trim());
        article.setViewCount(0L);
        article.setStatus(1);

        int affectRow = articleMapper.insert(article);

        if (affectRow != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "文章发布失败"
            );
        }
        return convertToArticleResponse(article);
    }

    public PageResponse<ArticleListItemResponse> listPublishedArticles(
            long current,
            long size) {
        Page<ArticleEntity> page = new Page<>(current, size);

        LambdaQueryWrapper<ArticleEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ArticleEntity::getStatus, 1)
                .orderByDesc(ArticleEntity::getCreateTime);

        Page<ArticleEntity> resultPage = articleMapper.selectPage(page, wrapper);

        List<ArticleListItemResponse> records = resultPage
                .getRecords()
                .stream()
                .map(this::convertToListItem)
                .toList();

        return new PageResponse<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                resultPage.getPages(),
                records
        );
    }

    private ArticleResponse convertToArticleResponse(
            ArticleEntity article) {

        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getContent(),
                article.getAuthorId(),
                article.getViewCount(),
                article.getStatus(),
                article.getCreateTime(),
                article.getUpdateTime()
        );
    }

    private ArticleListItemResponse convertToListItem(
            ArticleEntity article) {

        return new ArticleListItemResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getAuthorId(),
                article.getViewCount(),
                article.getCreateTime()
        );
    }
}
