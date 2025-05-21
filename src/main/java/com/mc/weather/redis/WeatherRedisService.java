package com.mc.weather.redis;

import com.mc.weather.data.dmi.Feature;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WeatherRedisService {


    private final WeatherPropertiesService weatherPropertiesService;

    public WeatherRedisService(WeatherTimeSeriesService weatherTimeSeriesService, WeatherPropertiesService weatherPropertiesService) {
        this.weatherPropertiesService = weatherPropertiesService;
    }

    public Mono<Void> saveWeatherData(Flux<Feature> features) {
        return weatherPropertiesService.saveProperties(features);
    }









}
