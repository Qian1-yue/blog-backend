package com.example.blogbackend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class HotArticleService {
    private static final Logger log = LoggerFactory.getLogger(HotArticleService.class);
    private static final String HOT_KEY = "article:hot";

    private final StringRedisTemplate stringRedisTemplate;

    public HotArticleService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void recordView(Long articleId) {
        try {
            stringRedisTemplate.opsForZSet()
                    .incrementScore(HOT_KEY, articleId.toString(), 1);
        } catch (RuntimeException exception) {
            log.warn("记录文章热度失败，articleId={}", articleId, exception);
        }
    }

    public List<Long> getHotArticleIds(int limit) {
        Set<String> members;

        try {
            members = stringRedisTemplate.opsForZSet()
                    .reverseRange(HOT_KEY, 0, limit - 1);
        } catch (RuntimeException exception) {
            log.warn("读取文章热度排行失败", exception);
            return Collections.emptyList();
        }

        if(members == null || members.isEmpty()) {
            return Collections.emptyList();
        }

        return members.stream()
                .map(Long::valueOf)
                .toList();
    }

    public void remove(Long articleId) {
        try {
            stringRedisTemplate.opsForZSet().remove(HOT_KEY, articleId.toString());
        } catch (RuntimeException exception) {
            log.warn("删除文章热度失败，articleId={}", articleId, exception);
        }
    }
}
