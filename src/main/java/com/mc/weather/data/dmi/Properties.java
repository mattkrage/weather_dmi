package com.mc.weather.data.dmi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Properties (
    @JsonProperty("created") String created,
    @JsonProperty("observed") String observed,
    @JsonProperty("parameterId") String parameterId,
    @JsonProperty("stationId") String stationId,
    @JsonProperty("value") double value
) {}