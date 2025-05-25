package com.mc.weather;


import com.mc.weather.redis.RedisKeyBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RedisKeyTest {

    @Test
    public void testRedisKeyCreation() {
        String key = RedisKeyBuilder.buildKey("1111", "temp", 1746876000L);
        assertEquals(key, "weather:1111:temp:1746876000");
    }

    @Test
    public void testRedisKeyTimestampCreation() {
        String key = RedisKeyBuilder.buildKeyForLatestTimestamp("1111");
        assertEquals(key, "weather:station:1111:latest");
    }

    @Test
    public void testRedisKeyTimeSeriesCreation() {
        String key = RedisKeyBuilder.buildKeyForTimeSeries("1111", "temp");
        assertEquals(key, "weather:timeseries:1111:temp");
    }
}
