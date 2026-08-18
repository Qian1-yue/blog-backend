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
import com.example.blogbackend.vo.ArticleDetailResponse;
import com.example.blogbackend.vo.ArticleListItemResponse;
import com.example.blogbackend.vo.ArticleResponse;
import com.example.blogbackend.vo.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private static final Logger log =
            LoggerFactory.getLogger(
                    ArticleService.class
            );
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final ArticleCacheService articleCacheService;
    private final HotArticleService hotArticleService;

    public ArticleService(ArticleMapper articleMapper, UserMapper userMapper, CommentMapper commentMapper, ArticleCacheService articleCacheService, HotArticleService hotArticleService) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.articleCacheService = articleCacheService;
        this.hotArticleService = hotArticleService;
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
        log.info(
                "用户发布文章成功，userId={}, articleId={}",
                authorId,
                article.getId()
        );
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

    public PageResponse<ArticleListItemResponse> listUserArticles(
            Long authorId,
            long current,
            long size) {
        Page<ArticleEntity> page = new Page<>(current, size);

        LambdaQueryWrapper<ArticleEntity> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(ArticleEntity::getAuthorId, authorId)
                .ne(ArticleEntity::getStatus, 2)
                .orderByDesc(ArticleEntity::getUpdateTime);

        Page<ArticleEntity> resultPage =
                articleMapper.selectPage(page, wrapper);

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

    public ArticleDetailResponse getArticleDetail(
            Long id) {
        int affectedRows = articleMapper.incrementViewCount(id);

        if (affectedRows == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND,"文章不存在");
        }

        hotArticleService.recordView(id);

        Optional<ArticleDetailResponse> cached = articleCacheService.get(id);

        if (cached.isPresent()) {
            Long currentViewCount = articleMapper.selectPublishedViewCount(id);
            return withViewCount(cached.get(), currentViewCount);
        }

        ArticleEntity article = getPublishedArticleOrThrow(id);

        ArticleDetailResponse response = convertToDetailResponse(article);
        articleCacheService.put(id, response);
        return response;
    }

    private ArticleDetailResponse withViewCount(
            ArticleDetailResponse response,
            Long viewCount) {
        return new ArticleDetailResponse(
                response.id(),
                response.title(),
                response.summary(),
                response.content(),
                response.authorId(),
                response.authorNickname(),
                viewCount == null ? response.viewCount() : viewCount,
                response.createTime(),
                response.updateTime()
        );
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
                        ArticleEntity::getId,
                        id
                )
                .eq(ArticleEntity::getAuthorId, currentUserId)
                .ne(ArticleEntity::getStatus, 2);

        ArticleEntity updateArticle = new ArticleEntity();

        updateArticle.setStatus(2);
        updateArticle.setUpdateTime(LocalDateTime.now());
        log.info(
                "用户逻辑删除文章，userId={}, articleId={}",
                currentUserId,
                id
        );
        int affectedRows = articleMapper.update(updateArticle, articleWrapper);

        if (affectedRows != 1) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "文章状态已经发生变化，请刷新后重试"
            );
        }
        articleCacheService.evict(id);
        hotArticleService.remove(id);
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

    public ArticleDetailResponse updateArticle(
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
        articleCacheService.evict(id);
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

    private ArticleDetailResponse convertToDetailResponse(ArticleEntity article) {
        UserEntity author = userMapper.selectById(article.getAuthorId());

        String nickname = author == null
                ?"未知用户"
                : author.getNickname();

        return new ArticleDetailResponse(
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

    public List<ArticleListItemResponse> listHotArticles(int limit) {
        if (limit < 1 || limit >50) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,"limit必须在1到50之间");
        }

        List<Long> hotIds = hotArticleService.getHotArticleIds(limit);

        if (hotIds.isEmpty()) {
            return List.of();
        }

        List<ArticleEntity> articles = articleMapper.selectList(
                new LambdaQueryWrapper<ArticleEntity>()
                .in(ArticleEntity::getId, hotIds)
                        .eq(ArticleEntity::getStatus, 1)
        );

        Map<Long,ArticleEntity> articleEntityMap =
                articles.stream()
                        .collect(Collectors.toMap(
                                ArticleEntity::getId,
                                article -> article
                        ));

        return hotIds.stream()
                .map(articleEntityMap::get)
                .filter(Objects::nonNull)
                .map(this::convertToListItem)
                .toList();
    }
}
