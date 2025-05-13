package com.mc.weather.redis;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import com.mc.weather.data.dto.TimeSeriesPoint;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class WeatherTimeSeriesService {

    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherTimeSeriesService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveTimeSeries(WeatherResponse response) {

        for (Feature feature : response.features()) {
            var props = feature.properties();
            String stationId = props.stationId();
            String parameterId = props.parameterId();
            double value = props.value();
            Instant observedInstant = Instant.parse(props.observed());
            long timestamp = observedInstant.getEpochSecond();

            String key = RedisKeyBuilder.buildKeyForTimeSeries(stationId, parameterId);

            redisTemplate.opsForZSet().add(key, value, timestamp);
        }
    }

    public List<TimeSeriesPoint> getTimeSeries(String stationId, String parameterId, long startTimestamp, long endTimestamp) {

        String key = RedisKeyBuilder.buildKeyForTimeSeries(stationId, parameterId);

        // Fetch observations within the specified timestamp range
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> rawResults = zSetOps.rangeByScoreWithScores(key, startTimestamp, endTimestamp);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Europe/Copenhagen")); // Danish local time

        List<TimeSeriesPoint> points = rawResults.stream()
                .map(tuple -> new TimeSeriesPoint(
                        formatter.format(Instant.ofEpochSecond(tuple.getScore().longValue())),
                        (Double) tuple.getValue()))
                .toList();

        return points;
    }
}
