package com.mc.weather.redis;

import java.time.Instant;

public class RedisKeyBuilder {
    public static String buildKey(String stationId, String parameterId, Long timestamp) {
        return String.format("weather:%s:%s:%d", stationId, parameterId, timestamp);
    }
}