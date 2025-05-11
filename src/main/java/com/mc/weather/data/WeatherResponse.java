package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WeatherResponse {
    @JsonProperty("type")
    private String type;

    @JsonProperty("features")
    private List<Feature> features;

    @JsonProperty("timeStamp")
    private String timeStamp;

    @JsonProperty("numberReturned")
    private int numberReturned;

    @JsonProperty("links")
    private List<Link> links;
}
