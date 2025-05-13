package com.mc.weather.data.dmi;

import com.fasterxml.jackson.annotation.JsonProperty;


public record Feature (
    @JsonProperty("geometry") Geometry geometry,
    @JsonProperty("id") String id,
    @JsonProperty("type") String type,
    @JsonProperty("properties") Properties properties){

    public static Feature withPropertiesOnly(Properties properties) {
        return new Feature(null, null, null, properties);
    }
}
