package com.example.blogbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisJsonService {
    private static final Logger log = LoggerFactory.getLogger(RedisJsonService.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;

    public RedisJsonService(StringRedisTemplate stringRedisTemplate, JsonMapper jsonMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void set(String key, Object value, Duration ttl) {
        String json;

        try {
            json = jsonMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Redis JSON序列化失败",exception);
        }

        stringRedisTemplate.opsForValue()
                .set(key,json,ttl);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        String json = stringRedisTemplate.opsForValue().get(key);

        if (json == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonMapper.readValue(json, type));
        } catch (Exception exception) {
            log.warn("Redis缓存内容无法反序列化，删除异常缓存，key={}",key);
            stringRedisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }
}
