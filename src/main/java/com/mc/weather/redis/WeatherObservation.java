package com.mc.weather.redis;

public record WeatherObservation(
        String stationId,
        String parameterId,
        String timestamp,
        double value
) {}
