package com.example.blogbackend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisPracticeService {
    private final StringRedisTemplate stringRedisTemplate;

    public RedisPracticeService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void setString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void setStringWithExpire(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10));
    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }
}
