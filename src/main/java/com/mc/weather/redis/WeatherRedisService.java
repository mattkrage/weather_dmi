package com.mc.weather.redis;

import com.mc.weather.data.dmi.WeatherResponse;
import org.springframework.stereotype.Service;

@Service
public class WeatherRedisService {

    private final WeatherTimeSeriesService  weatherTimeSeriesService;

    private final WeatherPropertiesService weatherPropertiesService;

    public WeatherRedisService(WeatherTimeSeriesService weatherTimeSeriesService, WeatherPropertiesService weatherPropertiesService) {
        this.weatherTimeSeriesService = weatherTimeSeriesService;
        this.weatherPropertiesService = weatherPropertiesService;
    }

    public void saveWeatherData(WeatherResponse weatherResponse) {
        weatherPropertiesService.saveProperties(weatherResponse);
        weatherTimeSeriesService.saveTimeSeries(weatherResponse);
    }









}
