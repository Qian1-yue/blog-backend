package com.example.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogbackend.dto.CreateArticleRequest;
import com.example.blogbackend.dto.UpdateArticleRequest;
import com.example.blogbackend.entity.ArticleEntity;
import com.example.blogbackend.entity.CommentEntity;
import com.example.blogbackend.entity.UserEntity;
import com.example.blogbackend.exception.BusinessException;
import com.example.blogbackend.mapper.ArticleMapper;
import com.example.blogbackend.mapper.CommentMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.vo.AirticleDetailResponse;
import com.example.blogbackend.vo.ArticleListItemResponse;
import com.example.blogbackend.vo.ArticleResponse;
import com.example.blogbackend.vo.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    public ArticleService(ArticleMapper articleMapper, UserMapper userMapper, CommentMapper commentMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
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

    public AirticleDetailResponse getAirticleDetail(
            Long id) {
        ArticleEntity article = getPublishedArticleOrThrow(id);

        int affectRow = articleMapper.incrementViewCount(id);

        if (affectRow != 1) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "文章不存在"
            );
        }

        ArticleEntity latestArticle = getPublishedArticleOrThrow(id);

        return convertToDetailResponse(latestArticle);
    }

    @Transactional
    public void deleteArticle(
            Long id,
            Long currentUserId) {
        ArticleEntity article = getActiveArticleOrThrow(id);

        checkArticleOwner(
                article,
                currentUserId
        );

        LambdaUpdateWrapper<ArticleEntity> articleWrapper = new LambdaUpdateWrapper<>();

        articleWrapper.eq(
                ArticleEntity::getAuthorId,
                currentUserId
                )
                .ne(ArticleEntity::getStatus, 2);

        ArticleEntity updateArticle = new ArticleEntity();

        updateArticle.setStatus(2);
        updateArticle.setUpdateTime(LocalDateTime.now());

        int affectedRows = articleMapper.update(updateArticle, articleWrapper);

        if (affectedRows != 1) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "文章状态已经发生变化，请刷新后重试"
            );
        }
        LambdaUpdateWrapper<CommentEntity>
                commentWrapper =
                new LambdaUpdateWrapper<>();

        commentWrapper
                .eq(CommentEntity::getArticleId, id)
                .ne(CommentEntity::getStatus, 2)
                .set(CommentEntity::getStatus, 2);

        commentMapper.update(
                null,
                commentWrapper
        );
    }

    public AirticleDetailResponse updateArticle(
            Long id,
            Long currentUserId,
            UpdateArticleRequest request
    ) {
        ArticleEntity article = getActiveArticleOrThrow(id);

        checkArticleOwner(
                article,
                currentUserId
        );

        ArticleEntity updateArticle = new ArticleEntity();

        updateArticle.setTitle(request.title().trim());
        updateArticle.setContent(request.content());
        updateArticle.setSummary(request.summary().trim());
        updateArticle.setUpdateTime(LocalDateTime.now());

        LambdaUpdateWrapper<ArticleEntity> wrapper = new LambdaUpdateWrapper<>();

        wrapper
                .eq(ArticleEntity::getId, id)
                .eq(
                        ArticleEntity::getAuthorId, currentUserId
                )
                .ne(ArticleEntity::getStatus, 2);

        int affectedRows = articleMapper.update(updateArticle, wrapper);

        if (affectedRows != 1) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "文章状态已经发生变化，请刷新后重试"
            );
        }
        ArticleEntity latesArticle =
                getActiveArticleOrThrow(id);

        return convertToDetailResponse(
                latesArticle
        );
    }

    private ArticleEntity getActiveArticleOrThrow(Long id) {
        ArticleEntity article = articleMapper.selectById(id);

        if (article == null || Integer.valueOf(2).equals(article.getStatus())) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "文章不存在"
            );
        }

        return article;
    }

    private ArticleEntity getPublishedArticleOrThrow(
            Long id) {

        ArticleEntity article =
                articleMapper.selectById(id);

        if (article == null
                || !Integer.valueOf(1)
                .equals(article.getStatus())) {

            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "文章不存在"
            );
        }

        return article;
    }

    private void checkArticleOwner(
            ArticleEntity article,
            Long currentUserId) {
        if(!Objects.equals(article.getAuthorId(), currentUserId)){
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "无权操作这篇文章"
            );
        }
    }

    private AirticleDetailResponse convertToDetailResponse(ArticleEntity article) {
        UserEntity author = userMapper.selectById(article.getAuthorId());

        String nickname = author == null
                ?"未知用户"
                : author.getNickname();

        return new AirticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getContent(),
                article.getAuthorId(),
                nickname,
                article.getViewCount(),
                article.getCreateTime(),
                article.getUpdateTime()
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
