package com.wen.oj.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void testRedisConnection() {
        // 测试 Redis 是否连接成功
        redisTemplate.opsForValue().set("testKey", "Hello Redis");

        String value = redisTemplate.opsForValue().get("testKey");

        // 验证返回的值
        assertEquals("Hello Redis", value);
    }
}
