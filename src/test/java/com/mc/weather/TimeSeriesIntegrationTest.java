package com.mc.weather;

import com.mc.weather.controller.DmiObservationController;
import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.Properties;
import com.mc.weather.data.dto.TimeSeriesPoint;
import com.mc.weather.dmi.DmiApiService;
import com.mc.weather.redis.WeatherPropertiesService;
import com.mc.weather.redis.WeatherRedisService;
import com.mc.weather.redis.WeatherTimeSeriesService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class TimeSeriesIntegrationTest extends AbstractTestNGSpringContextTests {

    private AutoCloseable mocks;

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WeatherTimeSeriesService queryService;

    @MockBean
    private WeatherPropertiesService propertyService;

    @MockBean
    private WeatherRedisService redisService;

    @MockBean
    private DmiApiService dmiApiService;


    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void shouldReturnTimeSeriesPoints() {

        Flux<TimeSeriesPoint> mockFlux = Flux.just(
                new TimeSeriesPoint("2025-05-01T01:00:00Z", 12.3),
                new TimeSeriesPoint("2025-05-01T02:00:00Z", 13.7)
        );
        Properties properties = new Properties(null, "2025-05-09T02:00:00Z", "humidity", "06186", 22.5);
        Feature feature = Feature.withPropertiesOnly(properties);

         when(propertyService.getLastObserved(anyString()))
                .thenReturn(Mono.just(10));

        when(dmiApiService.getObservations(anyString(), any()))
                .thenReturn(Flux.just(feature));

        when(redisService.saveWeatherData(any()))
                .thenReturn(Mono.empty());

        when(queryService.getTimeSeries(anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(mockFlux);

        webTestClient.get()
                .uri("api/dmi/timeserie/06186/humidity")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(TimeSeriesPoint.class)
                .hasSize(2); // or whatever you expect
    }

 }
