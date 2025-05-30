package com.mc.weather;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.Properties;
import com.mc.weather.dmi.DmiApiService;
import com.mc.weather.dmi.DmiRetrieveDataJob;
import com.mc.weather.redis.WeatherPropertiesService;
import com.mc.weather.redis.WeatherRedisService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DmiRetrieveDataJonTest {

    @Mock
    WeatherPropertiesService weatherPropertiesService;
    @Mock
    DmiApiService dmiApiService;
    @Mock
    WeatherRedisService weatherRedisService;

    @InjectMocks
    DmiRetrieveDataJob dmiJob; // Replace with your actual service name

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes @Mock and @InjectMocks
    }

    @Test
    void testSyncWeatherForStation() {
        // Arrange
        String stationId = "06186";
        int lastObserved = 42;

        Properties properties = new Properties(null, "2025-05-09T02:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);

        Flux<Feature> features = Flux.just(feature);

        when(weatherPropertiesService.getLastObserved(stationId)).thenReturn(Mono.just(lastObserved));
        when(dmiApiService.getObservations(stationId, lastObserved)).thenReturn(features);
        when(weatherRedisService.saveWeatherData(features)).thenReturn(Mono.empty());

        StepVerifier.create(dmiJob.syncWeatherForStation(stationId))
                .verifyComplete();

        verify(weatherPropertiesService).getLastObserved(stationId);
        verify(dmiApiService).getObservations(stationId, lastObserved);
        verify(weatherRedisService).saveWeatherData(features);
    }
}
