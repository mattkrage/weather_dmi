package com.mc.weather.redis;

import com.mc.weather.data.dto.TimeSeriesPoint;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Service
public class WeatherTimeSeriesService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public WeatherTimeSeriesService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Flux<TimeSeriesPoint> getTimeSeries(String stationId, String parameterId, long startTimestamp, long endTimestamp) {

        String key = RedisKeyBuilder.buildKeyForTimeSeries(stationId, parameterId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Europe/Copenhagen")); // Danish local time

        Range<Double> scoreRange = Range.closed((double)startTimestamp, (double)endTimestamp);
        Limit limit = Limit.unlimited();

        return reactiveRedisTemplate.opsForZSet()
                .rangeByScoreWithScores(key, scoreRange, limit)
                .map(tuple -> new TimeSeriesPoint(
                        formatter.format(Instant.ofEpochSecond(tuple.getScore().longValue())),
                        (Double) tuple.getValue()
                ));
    }
}
