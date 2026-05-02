package com.mimir.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request body for POST /api/ask.
 *
 * ADR-MIMIR-001:
 *   model  — Tier 1: client-selectable model ("deepseek" | "anthropic" | "ollama" | null)
 *   agents — Tier 2: multi-agent reasoning (stub, MIMIR Foundation sprint)
 */
public record AskRequest(
    @NotBlank String question,
    @NotBlank String sessionId,
    String dbName,

    /** Optional: "deepseek" | "anthropic" | "ollama". Null → server default (mimir.default-model). */
    String model,

    /** Tier 2 (stub): list of agents for multi-model reasoning. Ignored until MIMIR Foundation. */
    List<String> agents,

    @Min(1) @Max(10) int maxToolCalls
) {
    public AskRequest {
        if (maxToolCalls <= 0) maxToolCalls = 5;
    }

    /** Convenience constructor without agents (backward-compat). */
    public AskRequest(String question, String sessionId, String dbName, int maxToolCalls) {
        this(question, sessionId, dbName, null, null, maxToolCalls);
    }
}
