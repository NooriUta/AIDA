package com.mimir.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Single Tier-3 demo cache entry — corresponds to one element in
 * {@code services/mimir/src/main/resources/cache/mimir_responses.json}.
 *
 * <p>Loaded at startup by {@link DemoCacheService}. A request matches an entry
 * when at least one of {@link #matchKeywords()} occurs (case-insensitive) in the
 * question text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DemoCacheEntry(
        String questionPattern,
        List<String> matchKeywords,
        String answer,
        List<String> toolCallsUsed,
        List<String> highlightNodeIds,
        double confidence,
        long simulatedDelayMs
) {}
