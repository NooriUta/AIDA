package com.mimir.service;

import com.mimir.model.MimirAnswer;

/**
 * Common type for all MIMIR model implementations.
 * Used by ModelRouter to dispatch without knowing the concrete service.
 *
 * ADR-MIMIR-001: Multi-model routing — Tier 1.
 */
public interface MimirAiPort {

    MimirAnswer ask(String sessionId, String question, String dbName, String tenantAlias);
}
