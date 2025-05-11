package com.mc.weather.redis;

import com.mc.weather.data.WeatherResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class WeatherRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveWeatherData(WeatherResponse weatherResponse) {
        weatherResponse.getFeatures().forEach(feature -> {
            String key = RedisKeyBuilder.buildKey(
                    feature.getProperties().getStationId(),
                    feature.getProperties().getParameterId(),
                    Instant.parse(feature.getProperties().getObserved())
            );
            redisTemplate.opsForValue().set(key, feature);
        });
    }
}
