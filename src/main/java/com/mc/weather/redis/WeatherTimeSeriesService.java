package com.mc.weather.redis;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import com.mc.weather.data.dto.TimeSeriesPoint;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;


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

        // Create Range and Limit objects
        Range<Double> scoreRange = Range.closed((double)startTimestamp, (double)endTimestamp);
        Limit limit = Limit.unlimited();  // or specify your limit if needed

        return reactiveRedisTemplate.opsForZSet()
                .rangeByScoreWithScores(key, scoreRange, limit)
                .map(tuple -> new TimeSeriesPoint(
                        formatter.format(Instant.ofEpochSecond(tuple.getScore().longValue())),
                        (Double) tuple.getValue()
                ));
/*
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> rawResults = zSetOps.rangeByScoreWithScores(key, startTimestamp, endTimestamp);
*/



/*
        List<TimeSeriesPoint> points = rawResults.stream()
                .map(tuple -> new TimeSeriesPoint(
                        formatter.format(Instant.ofEpochSecond(tuple.getScore().longValue())),
                        (Double) tuple.getValue()))
                .toList();

        return points;
*/
    }
}
