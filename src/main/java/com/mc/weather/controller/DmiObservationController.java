package com.mc.weather.controller;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import com.mc.weather.data.dto.TimeSeriesPoint;
import com.mc.weather.redis.WeatherPropertiesService;
import com.mc.weather.redis.WeatherRedisService;
import com.mc.weather.dmi.DmiApiService;
import com.mc.weather.redis.WeatherTimeSeriesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/dmi")
public class DmiObservationController {

    @Autowired
    private DmiApiService dmiApiService;

    @Autowired
    private WeatherRedisService weatherRedisService;

    @Autowired
    private WeatherPropertiesService weatherPropertiesService;

    @Autowired
    private WeatherTimeSeriesService weatherTimeSeriesService;

    @GetMapping("/timeserie/{station}/{serie}")
    public Flux<TimeSeriesPoint> getTimeSerie(@PathVariable String station,
                                              @PathVariable String serie,
                                              @RequestParam(required = false) Long from,
                                              @RequestParam(required = false) Long to) {

        long oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond();;
        from = Objects.requireNonNullElse(from, oneDayAgo);
        to = Objects.requireNonNullElse(to, (long) Double.POSITIVE_INFINITY);

        return retrieveData(station)
                .thenMany(weatherTimeSeriesService.getTimeSeries(station, serie, from, to));
    }

    private Mono<Void> retrieveData(String stationId) {
        Integer lastObserved = weatherPropertiesService.getLastObserved(stationId);
        log.info("Last observed:{}", lastObserved);

        Flux<Feature> observations = dmiApiService.getObservations(stationId, lastObserved);
        return weatherRedisService.saveWeatherData(observations);


        //log.info("Number of new observations: {}", observations.features().size());
    }
}