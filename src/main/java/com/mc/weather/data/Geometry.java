package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Geometry {
    @JsonProperty("coordinates")
    private List<Double> coordinates;

    @JsonProperty("type")
    private String type;
}