package com.mc.weather.redis;

import com.mc.weather.data.Feature;
import com.mc.weather.data.WeatherResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WeatherTimeSeriesService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherTimeSeriesService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveTimeSeries(WeatherResponse response) {
        // Get Redis ZSet operations object
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Iterate through features to save time-series data
        for (Feature feature : response.getFeatures()) {
            var props = feature.getProperties();
            String stationId = props.getStationId();
            String parameterId = props.getParameterId();
            double value = props.getValue();
            Instant observedInstant = Instant.parse(props.getObserved());
            long timestamp = observedInstant.getEpochSecond();

            // Build Redis key for the station and parameter
            String key = String.format("weather:timeseries:%s:%s", stationId, parameterId);

            // Add the data to the Redis sorted set (using timestamp as the score)
            zSetOps.add(key, value, timestamp);
        }
    }
}
