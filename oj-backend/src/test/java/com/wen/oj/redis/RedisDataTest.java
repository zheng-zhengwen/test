package com.wen.oj.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisDataTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void viewRedisData() {
        // 假设要查看的题目 ID 和编程语言
        String questionId = "1908506063495335937";
        String language = "java";

        // 构建 AI 缓存键
        String cacheKey = "question:ai:" + questionId + language;
        // 获取键的 TTL
        Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);

        if (ttl != null) {
            if (ttl == 86400) {
                System.out.println("键 " + cacheKey + " 的 TTL 为一天。");
            } else {
                System.out.println("键 " + cacheKey + " 的 TTL 不是一天，剩余时间为 " + ttl + " 秒。");
            }
        } else {
            System.out.println("键 " + cacheKey + " 没有设置 TTL 或者键不存在。");
        }
    }
}