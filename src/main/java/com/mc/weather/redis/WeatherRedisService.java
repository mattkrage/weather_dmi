package com.mc.weather.redis;

import com.mc.weather.data.Feature;
import com.mc.weather.data.WeatherResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WeatherRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveWeatherData(WeatherResponse weatherResponse) {
        saveProperties(weatherResponse);
        saveLastObservationDate(weatherResponse.getFeatures());
    }

    private void saveLastObservationDate(List<Feature> features) {
        for (Feature feature : features) {
            String stationId = feature.getProperties().getStationId();
            Instant instant = Instant.parse(feature.getProperties().getObserved());
            Long observed = instant.getEpochSecond();

            Integer lastObserved = getLastObserved(stationId);
            String key = "weather:station:" + stationId + ":latest";

            if (lastObserved == null || Long.valueOf(lastObserved.longValue()).compareTo(observed) < 0) {
                redisTemplate.opsForValue().set(key, observed);
            }
        }
    }

    public Integer getLastObserved(String stationId) {
        String key = "weather:station:" + stationId + ":latest";
        return (Integer) redisTemplate.opsForValue().get(key);
    }


    private void saveProperties(WeatherResponse weatherResponse) {
        weatherResponse.getFeatures().forEach(feature -> {

            Instant instant = Instant.parse(feature.getProperties().getObserved());
            long lastObserved = instant.getEpochSecond();
            String key = RedisKeyBuilder.buildKey(
                    feature.getProperties().getStationId(),
                    feature.getProperties().getParameterId(),
                    lastObserved
            );
            redisTemplate.opsForValue().set(key, feature);
        });
    }

}
