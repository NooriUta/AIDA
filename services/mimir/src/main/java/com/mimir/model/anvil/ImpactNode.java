package com.mimir.model.anvil;

/** Mirror of {@code studio.seer.anvil.model.ImpactNode} — kept local to avoid cross-service deps. */
public record ImpactNode(String id, String type, String label, int depth) {}
