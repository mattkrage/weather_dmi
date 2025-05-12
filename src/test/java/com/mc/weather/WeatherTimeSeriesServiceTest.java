package com.mc.weather;

import com.mc.weather.data.Feature;
import com.mc.weather.data.Properties;
import com.mc.weather.data.WeatherResponse;
import com.mc.weather.redis.WeatherTimeSeriesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeatherTimeSeriesServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOps;

    @InjectMocks
    private WeatherTimeSeriesService weatherTimeSeriesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    void testSaveTimeSeries() {
        // Arrange
        Feature feature = new Feature();
        Properties properties = new Properties();
        properties.setStationId("06186");
        properties.setParameterId("temperature");
        properties.setObserved("2025-05-09T02:40:42.885615Z");
        properties.setValue(22.5);
        feature.setProperties(properties);

        WeatherResponse response = new WeatherResponse();
        response.setFeatures(List.of(feature));

        String expectedKey = "weather:timeseries:06186:temperature";
        double timestamp = Instant.parse("2025-05-09T02:40:42.885615Z").getEpochSecond();

        // Act
        weatherTimeSeriesService.saveTimeSeries(response);

        // Assert
        verify(zSetOps).add(eq(expectedKey), eq(22.5), eq(timestamp));
    }
}
