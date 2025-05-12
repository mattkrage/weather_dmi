package com.mc.weather.redis;

import com.mc.weather.data.Feature;
import com.mc.weather.data.WeatherResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WeatherRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final WeatherTimeSeriesService  weatherTimeSeriesService;

    public WeatherRedisService(RedisTemplate<String, Object> redisTemplate, WeatherTimeSeriesService  weatherTimeSeriesService) {
        this.redisTemplate = redisTemplate;
        this.weatherTimeSeriesService = weatherTimeSeriesService;
    }

    public void saveWeatherData(WeatherResponse weatherResponse) {
        saveProperties(weatherResponse);
        saveLastObservationDate(weatherResponse.getFeatures());
        saveTimeSeries(weatherResponse);
        weatherTimeSeriesService.saveTimeSeries(weatherResponse);
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

    public void saveTimeSeries(WeatherResponse response) {

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        for (Feature feature : response.getFeatures()) {
            var props = feature.getProperties();
            String stationId = props.getStationId();
            String parameterId = props.getParameterId();
            double value = props.getValue(); // Assuming value is numeric
            Instant observedInstant = Instant.parse(props.getObserved());
            long timestamp = observedInstant.getEpochSecond();

            // Build Redis key for the station and parameter
            String key = String.format("weather:timeseries:%s:%s", stationId, parameterId);

            // Add the data to the Redis sorted set
            zSetOps.add(key, value, timestamp);
        }
    }

}
