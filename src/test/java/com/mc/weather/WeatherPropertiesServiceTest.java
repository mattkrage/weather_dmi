package com.mc.weather;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.Properties;
import com.mc.weather.data.dmi.WeatherResponse;
import com.mc.weather.redis.RedisKeyBuilder;
import com.mc.weather.redis.WeatherPropertiesService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import reactor.core.publisher.Flux;

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

    @Mock
    private ZSetOperations<String, Object> zSetOps;

    @InjectMocks
    private WeatherPropertiesService weatherPropertiesService;


    @Test
    public void testSaveWeatherData_savesFeatureAndLatestTimestamp() {
        // Arrange
        Properties properties = new Properties(null, "2025-05-09T02:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);

        String latestKey = "weather:station:06186:latest";
        Long observedTimestamp = Instant.parse(properties.observed()).getEpochSecond();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOperations.get(latestKey)).thenReturn(null); // No previous value

        // Act
        weatherPropertiesService.saveProperties(Flux.just(feature)).block();

        // Assert
        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );
        verify(valueOperations).set(eq(latestKey), eq(observedTimestamp));

        String expectedKey = "weather:timeseries:06186:humidity";
        double timestamp = Instant.parse("2025-05-09T02:00:00Z").getEpochSecond();

        // Check that time serie was saved
        verify(zSetOps).add(eq(expectedKey), eq(22.5), eq(timestamp));

    }

    @Test
    public void shouldNotUpdateLatestWhenFeatureHasOlderObservation() {
        // Given
        Properties properties = new Properties(null, "2025-05-09T01:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);
        WeatherResponse response = WeatherResponse.withFeaturesOnly(List.of(feature));

        String latestKey = "weather:station:06186:latest";
        long existingLatest = Instant.parse("2025-05-09T02:00:00Z").getEpochSecond(); // newer
        long observedTimestamp = Instant.parse("2025-05-09T01:00:00Z").getEpochSecond(); // older

        // Redis mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOperations.get(latestKey)).thenReturn((int) existingLatest); // simulate stored latest

        // Act
        weatherPropertiesService.saveProperties(Flux.just(feature)).block();

        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );

        // Verify that the latest is NOT updated
        verify(valueOperations, never()).set(eq(latestKey), anyLong());
    }


    @Test
    public void shouldUpdateLatestWhenFeatureHasNewerObservation() {
        // Given
        Properties properties = new Properties(null, "2025-05-09T01:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);
        WeatherResponse response = WeatherResponse.withFeaturesOnly(List.of(feature));

        String latestKey = "weather:station:06186:latest";
        long existingLatest = Instant.parse("2025-04-09T02:00:00Z").getEpochSecond(); // newer
        long observedTimestamp = Instant.parse("2025-05-09T01:00:00Z").getEpochSecond(); // older

        // Redis mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOperations.get(latestKey)).thenReturn((int) existingLatest); // simulate stored latest

        // Act
        weatherPropertiesService.saveProperties(Flux.just(feature)).block();

        verify(valueOperations).set(
                eq(RedisKeyBuilder.buildKey("06186", "humidity", observedTimestamp)),
                eq(feature)
        );

        // Verify that the latest is NOT updated
        verify(valueOperations).set(eq(latestKey), anyLong());
    }
}