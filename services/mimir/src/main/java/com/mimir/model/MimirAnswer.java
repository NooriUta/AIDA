package com.mimir.model;

import java.time.Instant;
import java.util.List;

/**
 * MIMIR end-user response envelope. Token / cost / quota fields are
 * populated by the orchestrator on TIER2 paths; legacy CORE callers can rely
 * on the canonical 5-field constructor still resolving (overload below).
 */
public record MimirAnswer(
        String answer,
        List<String> toolCallsUsed,
        List<String> highlightNodeIds,
        double confidence,
        long durationMs,
        // ── TIER2 MT-07 ────────────────────────────────────────────────
        String provider,
        String model,
        long promptTokens,
        long completionTokens,
        // ── TIER2 MT-07 quota / MT-08 hil ─────────────────────────────
        QuotaInfo quota,
        ApprovalInfo awaitingApproval
) {
    public MimirAnswer(String answer, List<String> toolCallsUsed, List<String> highlightNodeIds,
                      double confidence, long durationMs) {
        this(answer, toolCallsUsed, highlightNodeIds, confidence, durationMs,
                null, null, 0L, 0L, null, null);
    }

    public static MimirAnswer unavailable() {
        return new MimirAnswer(
                "MIMIR is temporarily unavailable. Please try again later.",
                List.of(), List.of(), 0.0, 0L);
    }

    public static MimirAnswer quotaExceeded(QuotaInfo info) {
        String msg = "MIMIR quota exceeded: " + (info == null ? "unknown" : info.reason())
                + ". Please retry after the next reset window.";
        return new MimirAnswer(msg, List.of(), List.of(), 0.0, 0L,
                null, null, 0L, 0L, info, null);
    }

    public static MimirAnswer awaitingApproval(ApprovalInfo info) {
        String msg = "MIMIR request paused — operator approval required (" + info.reason() + ").";
        return new MimirAnswer(msg, List.of(), List.of(), 0.0, 0L,
                null, null, 0L, 0L, null, info);
    }

    public static MimirAnswer rejected(String comment) {
        String msg = "MIMIR request rejected by operator"
                + (comment == null || comment.isBlank() ? "." : ": " + comment);
        return new MimirAnswer(msg, List.of(), List.of(), 0.0, 0L,
                null, null, 0L, 0L, null, null);
    }

    public record QuotaInfo(
            String reason,
            long currentTokens,
            long limitTokens,
            double currentCost,
            double limitCost,
            Instant resetAt
    ) {}

    public record ApprovalInfo(
            String approvalId,
            String reason,
            Instant requestedAt,
            Instant expiresAt,
            double estimatedCostUsd
    ) {}
}
