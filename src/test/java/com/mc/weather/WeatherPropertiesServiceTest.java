package com.mc.weather;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.Properties;
import com.mc.weather.redis.RedisKeyBuilder;
import com.mc.weather.redis.WeatherPropertiesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherPropertiesServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> valueOperations;

    @Mock
    private ReactiveZSetOperations<String, Object> zSetOps;

    @InjectMocks
    private WeatherPropertiesService weatherPropertiesService;

    @BeforeEach
    void setup() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    public void testSaveWeatherData_savesFeatureAndLatestTimestamp() {
        // Arrange
        Properties properties = new Properties(null, "2025-05-09T02:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);

        String latestKey = "weather:station:06186:latest";
        Long observedTimestamp = Instant.parse(properties.observed()).getEpochSecond();

        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOperations.get(latestKey)).thenReturn(Mono.empty()); // No previous value
        when(valueOperations.set(anyString(), any())).thenReturn(Mono.just(true));
        when(zSetOps.add(anyString(), any(), anyDouble())).thenReturn(Mono.just(true));

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
    void shouldSaveFeatureAndUpdateLatestIfNewer() {
        // Arrange
        Properties props = new Properties(null, "2025-05-09T02:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(props);

        long observed = Instant.parse(props.observed()).getEpochSecond();
        String featureKey = RedisKeyBuilder.buildKey("06186", "humidity", observed);
        String timeSeriesKey = RedisKeyBuilder.buildKeyForTimeSeries("06186", "humidity");
        String latestKey = RedisKeyBuilder.buildKeyForLatestTimestamp("06186");

        when(valueOperations.get(latestKey)).thenReturn(Mono.just(Instant.parse("2025-05-08T02:00:00Z").getEpochSecond()));
        when(valueOperations.set(eq(featureKey), eq(feature))).thenReturn(Mono.just(true));
        when(valueOperations.set(eq(latestKey), eq(observed))).thenReturn(Mono.just(true));
        when(zSetOps.add(eq(timeSeriesKey), eq(22.5), eq((double) observed))).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(weatherPropertiesService.saveProperties(Flux.just(feature)))
                .verifyComplete();

        verify(valueOperations).set(featureKey, feature);
        verify(valueOperations).set(latestKey, observed);
        verify(zSetOps).add(timeSeriesKey, 22.5, (double) observed);
    }


    @Test
    public void shouldUpdateLatestWhenFeatureHasNewerObservation() {
        // Given
        Properties properties = new Properties(null, "2025-05-09T01:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);

        String latestKey = "weather:station:06186:latest";
        long existingLatest = Instant.parse("2025-04-09T02:00:00Z").getEpochSecond(); // newer
        long observedTimestamp = Instant.parse("2025-05-09T01:00:00Z").getEpochSecond(); // older

        // Redis mocks
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOperations.get(latestKey)).thenReturn(Mono.just(existingLatest)); // simulate stored latest
        when(valueOperations.set(anyString(), any())).thenReturn(Mono.just(true));
        when(zSetOps.add(anyString(), any(), anyDouble())).thenReturn(Mono.just(true));
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