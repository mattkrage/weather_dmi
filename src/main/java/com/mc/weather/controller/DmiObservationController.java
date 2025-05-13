package com.mc.weather.controller;

import com.mc.weather.data.WeatherResponse;
import com.mc.weather.redis.WeatherPropertiesService;
import com.mc.weather.redis.WeatherRedisService;
import com.mc.weather.dmi.DmiApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dmi")
public class DmiObservationController {

      @Autowired
    private DmiApiService dmiApiService;

    @Autowired
    private WeatherRedisService weatherRedisService;

    @Autowired
    private WeatherPropertiesService weatherPropertiesService;

    @GetMapping("/get-weather")
    public WeatherResponse getWeather(@RequestParam String stationId) {


/*
        Instant lastObservationDate = weatherRedisService.getLastObservationDate(stationId);
*/
        Integer lastObserved = weatherPropertiesService.getLastObserved(stationId);
        System.out.println("LAST OBSERVED:" +lastObserved);
        WeatherResponse observations = dmiApiService.getObservations(stationId, lastObserved);

        weatherRedisService.saveWeatherData(observations);


  /*       Set<String> getUniqueParameterIds = observations.getFeatures().stream().map(feature -> feature.getProperties().getParameterId())
                .collect(Collectors.toSet());



       System.out.println(observations.getFeatures().stream()
                .filter(feature -> "temp_dew".equals(feature.getProperties().getParameterId()))
                .mapToDouble(feature -> feature.getProperties().getValue())
                .min());*/



        return observations;
    }
}