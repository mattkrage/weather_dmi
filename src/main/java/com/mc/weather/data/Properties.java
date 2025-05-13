package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public record Properties (
    @JsonProperty("created") String created,
    @JsonProperty("observed") String observed,
    @JsonProperty("parameterId") String parameterId,
    @JsonProperty("stationId") String stationId,
    @JsonProperty("value") double value
) {}