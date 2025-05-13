package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public record WeatherResponse (
    @JsonProperty("type") String type,
    @JsonProperty("features") List<Feature> features,
    @JsonProperty("timeStamp") String timeStamp,
    @JsonProperty("numberReturned") int numberReturned,
    @JsonProperty("links") List<Link> links
) {
    public static  WeatherResponse withFeaturesOnly(List<Feature> features) {
        return new WeatherResponse(null, features, null, 1, null);
    }
}
