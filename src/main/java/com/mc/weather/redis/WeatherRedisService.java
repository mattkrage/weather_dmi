package com.mc.weather.redis;

import com.mc.weather.data.dmi.Feature;
import com.mc.weather.data.dmi.WeatherResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class WeatherRedisService {

    private final WeatherTimeSeriesService  weatherTimeSeriesService;

    private final WeatherPropertiesService weatherPropertiesService;

    public WeatherRedisService(WeatherTimeSeriesService weatherTimeSeriesService, WeatherPropertiesService weatherPropertiesService) {
        this.weatherTimeSeriesService = weatherTimeSeriesService;
        this.weatherPropertiesService = weatherPropertiesService;
    }

    public void saveWeatherData(Flux<Feature> features) {
        weatherPropertiesService.saveProperties(features).subscribe();
        weatherTimeSeriesService.saveTimeSeries(features).subscribe();
    }









}
