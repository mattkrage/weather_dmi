package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Properties {
    @JsonProperty("created")
    private String created;

    @JsonProperty("observed")
    private String observed;

    @JsonProperty("parameterId")
    private String parameterId;

    @JsonProperty("stationId")
    private String stationId;

    @JsonProperty("value")
    private double value;

}