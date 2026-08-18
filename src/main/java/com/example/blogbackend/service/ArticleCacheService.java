package com.example.blogbackend.service;

import com.example.blogbackend.vo.ArticleDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class ArticleCacheService {
    private static final Logger log = LoggerFactory.getLogger(ArticleCacheService.class);
    private static final String DETAIL_PREFIX = "article:detail:";
    private static final Duration DETAIL_TTL = Duration.ofMinutes(10);

    private final RedisJsonService redisJsonService;

    public ArticleCacheService(RedisJsonService redisJsonService) {
        this.redisJsonService = redisJsonService;
    }

    public Optional<ArticleDetailResponse> get(Long articleId) {
        try {
            return redisJsonService.get(
                    buildKey(articleId),
                    ArticleDetailResponse.class
            );
        } catch (RuntimeException exception) {
            log.warn("读取文章缓存失败，articleId={}", articleId, exception);
            return Optional.empty();
        }
    }

    public void put(Long articleId, ArticleDetailResponse response) {
        try {
            redisJsonService.set(
                    buildKey(articleId),
                    response,
                    DETAIL_TTL
            );
        } catch (RuntimeException exception) {
            log.warn("写入文章缓存失败，articleId={}", articleId, exception);
        }
    }

    public void evict(Long articleId) {
        try {
            redisJsonService.delete(buildKey(articleId));
        } catch (RuntimeException exception) {
            log.warn("删除文章缓存失败，articleId={}", articleId, exception);
        }
    }
    private String buildKey(Long articleId) {
        return DETAIL_PREFIX + articleId;
    }
}
