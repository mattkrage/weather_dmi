package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public record Geometry (
    @JsonProperty("coordinates") List<Double> coordinates,
    @JsonProperty("type") String type
){}