package com.mc.weather;

import com.mc.weather.data.Feature;
import com.mc.weather.data.Properties;
import com.mc.weather.data.WeatherResponse;
import com.mc.weather.redis.RedisKeyBuilder;
import com.mc.weather.redis.WeatherPropertiesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherPropertiesServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private WeatherPropertiesService weatherPropertiesService;


    @Test
    void testSaveWeatherData_savesFeatureAndLatestTimestamp() {
        // Arrange
        Properties properties = new Properties(null, "2025-05-09T02:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);
        WeatherResponse response = WeatherResponse.withFeaturesOnly(List.of(feature));


        String latestKey = "weather:station:06186:latest";
        Long observedTimestamp = Instant.parse(properties.observed()).getEpochSecond();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(latestKey)).thenReturn(null); // No previous value

        // Act
        weatherPropertiesService.saveProperties(response);

        // Assert
        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );
        verify(valueOperations).set(eq(latestKey), eq(observedTimestamp));
    }

    @Test
    void shouldNotUpdateLatestWhenFeatureHasOlderObservation() {
        // Given
        Properties properties = new Properties(null, "2025-05-09T01:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);
        WeatherResponse response = WeatherResponse.withFeaturesOnly(List.of(feature));

        String latestKey = "weather:station:06186:latest";
        long existingLatest = Instant.parse("2025-05-09T02:00:00Z").getEpochSecond(); // newer
        long observedTimestamp = Instant.parse("2025-05-09T01:00:00Z").getEpochSecond(); // older

        // Redis mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(latestKey)).thenReturn((int) existingLatest); // simulate stored latest

        // Act
        weatherPropertiesService.saveProperties(response);

        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );

        // Verify that the latest is NOT updated
        verify(valueOperations, never()).set(eq(latestKey), anyLong());
    }


    @Test
    void shouldUpdateLatestWhenFeatureHasNewerObservation() {
        // Given
        Properties properties = new Properties(null, "2025-05-09T01:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);
        WeatherResponse response = WeatherResponse.withFeaturesOnly(List.of(feature));

        String latestKey = "weather:station:06186:latest";
        long existingLatest = Instant.parse("2025-04-09T02:00:00Z").getEpochSecond(); // newer
        long observedTimestamp = Instant.parse("2025-05-09T01:00:00Z").getEpochSecond(); // older

        // Redis mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(latestKey)).thenReturn((int) existingLatest); // simulate stored latest

        // Act
        weatherPropertiesService.saveProperties(response);

        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );

        // Verify that the latest is NOT updated
        verify(valueOperations).set(eq(latestKey), anyLong());
    }
}