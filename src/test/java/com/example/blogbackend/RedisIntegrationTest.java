package com.example.blogbackend;

import com.example.blogbackend.service.RedisJsonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class RedisIntegrationTest {

    @Autowired
    private RedisJsonService redisJsonService;

    @Test
    void shouldSaveAndReadJson() {
        String key = "test:article:1";
        DemoArticle article = new DemoArticle(1L, "Redis测试");

        redisJsonService.set(key, article, Duration.ofMinutes(1));

        DemoArticle result = redisJsonService
                .get(key, DemoArticle.class)
                .orElseThrow();

        assertEquals(1L, result.id());
        assertEquals("Redis测试", result.title());

        redisJsonService.delete(key);
        assertTrue(redisJsonService.get(key, DemoArticle.class).isEmpty());
    }

    record DemoArticle(Long id, String title) {
    }
}
