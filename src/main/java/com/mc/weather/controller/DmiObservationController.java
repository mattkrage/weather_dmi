package com.mc.weather.controller;

import com.mc.weather.data.WeatherResponse;
import com.mc.weather.redis.WeatherObservation;
import com.mc.weather.redis.WeatherRedisService;
import com.mc.weather.web.DmiApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dmi")
public class DmiObservationController {

      @Autowired
    private DmiApiService dmiApiService;

    @Autowired
    private WeatherRedisService weatherRedisService;


    @GetMapping("/get-weather")
    public Set<String> getWeather(@RequestParam String stationId) {
        WeatherResponse observations = dmiApiService.getObservations(stationId);

        Set<String> getUniqueParameterIds = observations.getFeatures().stream().map(feature -> feature.getProperties().getParameterId())
                .collect(Collectors.toSet());


        System.out.println(observations.getFeatures().stream()
                .filter(feature -> "temp_dew".equals(feature.getProperties().getParameterId()))
                .mapToDouble(feature -> feature.getProperties().getValue())
                .min());

        weatherRedisService.saveWeatherData(observations);

        return getUniqueParameterIds;
    }
}