package com.mc.weather.redis;

import com.mc.weather.data.Feature;
import com.mc.weather.data.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
