package com.mc.weather.redis;

import com.mc.weather.data.Feature;
import com.mc.weather.data.WeatherResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WeatherPropertiesService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherPropertiesService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveProperties(WeatherResponse weatherResponse) {

        for (Feature feature : weatherResponse.features()) {
            Instant instant = Instant.parse(feature.properties().observed());
            Long observed = instant.getEpochSecond();
            String stationId = feature.properties().stationId();
            String key = RedisKeyBuilder.buildKey(
                    stationId,
                    feature.properties().parameterId(),
                    observed
            );

            saveLatestTimestamp(stationId, observed);
            redisTemplate.opsForValue().set(key, feature);

        }

    }

    private void saveLatestTimestamp(String stationId, Long observed) {
        Integer lastObserved = getLastObserved(stationId);
        if (lastObserved == null || Integer.compare(observed.intValue(), lastObserved) > 0) {
            String lastUpdateKey = RedisKeyBuilder.buildKeyForLatestTimestamp(stationId);
            redisTemplate.opsForValue().set(lastUpdateKey, observed);
        }
    }

    public Integer getLastObserved(String stationId) {
        String key = RedisKeyBuilder.buildKeyForLatestTimestamp(stationId);
        return (Integer) redisTemplate.opsForValue().get(key);
    }
}
