package com.image.backend.util.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface PointMixin {
    @JsonProperty("lng")
    double getX();

    @JsonProperty("lat")
    double getY();
}
