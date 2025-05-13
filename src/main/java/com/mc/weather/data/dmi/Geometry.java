package com.mc.weather.data.dmi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Geometry (
    @JsonProperty("coordinates") List<Double> coordinates,
    @JsonProperty("type") String type
){}