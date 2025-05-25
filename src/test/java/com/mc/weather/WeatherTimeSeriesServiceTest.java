package com.mc.weather;

import com.mc.weather.data.dto.TimeSeriesPoint;
import com.mc.weather.redis.RedisKeyBuilder;
import com.mc.weather.redis.WeatherTimeSeriesService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class WeatherTimeSeriesServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReactiveZSetOperations<String, Object> zSetOps;

    @InjectMocks
    private WeatherTimeSeriesService weatherTimeSeriesService;

    @BeforeMethod
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    public void testGetTimeSeries() {
        // Given
        String stationId = "06186";
        String parameterId = "temperature";
        long startTimestamp = 1747050000L;
        long endTimestamp = 1747150000L;

        String key = RedisKeyBuilder.buildKeyForTimeSeries(stationId, parameterId);

        // Simulate Redis result
        ZSetOperations.TypedTuple<Object> tuple1 = new DefaultTypedTuple<>(22.5d, 1747051800d);
        ZSetOperations.TypedTuple<Object> tuple2 = new DefaultTypedTuple<>(23.1d, 1747060000d);
        Flux<ZSetOperations.TypedTuple<Object>> redisData  = Flux.just(tuple1, tuple2);

        // Create Range and Limit objects
        Range<Double> scoreRange = Range.closed((double)startTimestamp, (double)endTimestamp);
        Limit limit = Limit.unlimited();  // or specify your limit if needed


        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.rangeByScoreWithScores(key, scoreRange, limit)).thenReturn(redisData);

        // When
        List<TimeSeriesPoint> result = weatherTimeSeriesService.getTimeSeries(stationId, parameterId, startTimestamp, endTimestamp).collectList()
                .block();;

        // Then
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Europe/Copenhagen")); // Danish local time

        assertEquals(2, result.size());
        assertEquals(new TimeSeriesPoint(formatter.format(Instant.ofEpochSecond(1747051800L)), 22.5), result.get(0));
        assertEquals(new TimeSeriesPoint(formatter.format(Instant.ofEpochSecond(1747060000L)), 23.1), result.get(1));
    }
}
