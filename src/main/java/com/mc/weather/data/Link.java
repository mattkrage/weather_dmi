package com.mc.weather.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public record Link (
    @JsonProperty("href") String href,
    @JsonProperty("rel") String rel,
    @JsonProperty("type") String type,
    @JsonProperty("title") String title
) {}
