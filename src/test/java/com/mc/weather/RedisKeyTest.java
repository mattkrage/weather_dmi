package com.mc.weather;


import com.mc.weather.redis.RedisKeyBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisKeyTest {

    @Test
    public void testRedisKeyCreation() {
        String key = RedisKeyBuilder.buildKey("1111", "temp", 1746876000L);
        assertEquals(key, "weather:1111:temp:1746876000");
    }
}
