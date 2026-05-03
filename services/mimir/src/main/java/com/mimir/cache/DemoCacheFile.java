package com.mimir.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DemoCacheFile(
        String version,
        String generatedAt,
        String model,
        List<DemoCacheEntry> responses
) {}
