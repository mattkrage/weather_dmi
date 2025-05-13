package com.mc.weather.data.dmi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Link (
    @JsonProperty("href") String href,
    @JsonProperty("rel") String rel,
    @JsonProperty("type") String type,
    @JsonProperty("title") String title
) {}
