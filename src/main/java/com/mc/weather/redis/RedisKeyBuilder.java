package com.mc.weather.redis;

import java.time.Instant;

public class RedisKeyBuilder {
    public static String buildKey(String stationId, String parameterId, Instant timestamp) {
        return String.format("weather:%s:%s:%s", stationId, parameterId, timestamp.toString());
    }
}