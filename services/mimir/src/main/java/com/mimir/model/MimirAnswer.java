package com.mimir.model;

import java.util.List;

public record MimirAnswer(
    String answer,
    List<String> toolCallsUsed,
    List<String> highlightNodeIds,
    double confidence,
    long durationMs
) {
    public static MimirAnswer unavailable() {
        return new MimirAnswer(
            "MIMIR is temporarily unavailable. Please try again later.",
            List.of(), List.of(), 0.0, 0L
        );
    }
}
