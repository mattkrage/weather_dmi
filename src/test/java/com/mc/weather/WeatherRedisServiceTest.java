package com.mc.weather;

import com.mc.weather.data.Feature;
import com.mc.weather.data.Properties;
import com.mc.weather.data.WeatherResponse;
import com.mc.weather.redis.RedisKeyBuilder;
import com.mc.weather.redis.WeatherRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherRedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private WeatherRedisService weatherRedisService;

    @Test
    void testSaveWeatherData_savesFeatureAndLatestTimestamp() {
        // Arrange
        Feature feature = new Feature();
        Properties properties = new Properties();
        properties.setStationId("06186");
        properties.setParameterId("humidity");
        properties.setObserved("2025-05-09T02:00:00Z");
        feature.setProperties(properties);

        WeatherResponse response = new WeatherResponse();
        response.setFeatures(List.of(feature));

        String latestKey = "weather:station:06186:latest";
        Long observedTimestamp = Instant.parse(properties.getObserved()).getEpochSecond();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(latestKey)).thenReturn(null); // No previous value

        // Act
        weatherRedisService.saveWeatherData(response);

        // Assert
        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );
        verify(valueOperations).set(eq(latestKey), eq(observedTimestamp));


        properties.setObserved("2025-05-08T02:00:00Z");
        feature.setProperties(properties);
        response.setFeatures(List.of(feature));
        // Act
        weatherRedisService.saveWeatherData(response);


    }

    @Test
    void shouldNotUpdateLatestWhenFeatureHasOlderObservation() {
        // Given
        Feature feature = new Feature();
        Properties properties = new Properties();
        properties.setStationId("06186");
        properties.setParameterId("humidity");
        properties.setObserved("2025-05-09T01:00:00Z");
        feature.setProperties(properties);
        WeatherResponse response = new WeatherResponse();
        response.setFeatures(List.of(feature));

        String latestKey = "weather:station:06186:latest";
        long existingLatest = Instant.parse("2025-05-09T02:00:00Z").getEpochSecond(); // newer
        long observedTimestamp = Instant.parse("2025-05-09T01:00:00Z").getEpochSecond(); // older

        // Redis mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(latestKey)).thenReturn((int) existingLatest); // simulate stored latest

        // Act
        weatherRedisService.saveWeatherData(response);

        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );

        // Verify that the latest is NOT updated
        verify(valueOperations, never()).set(eq(latestKey), anyLong());
    }

}