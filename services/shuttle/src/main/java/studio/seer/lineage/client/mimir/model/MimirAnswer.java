package studio.seer.lineage.client.mimir.model;

import java.util.List;

public record MimirAnswer(
        String answer,
        List<String> toolCallsUsed,
        List<String> highlightNodeIds,
        String confidence,
        long durationMs
) {}
