package com.mimir.hil;

/**
 * Outcome of a {@link HilGate#check} call.
 *
 * <p>Allowed → orchestrator continues to model dispatch.
 * <p>Not allowed → orchestrator pauses the session, emits HIL_PAUSE_REQUESTED,
 * and returns {@link com.mimir.model.MimirAnswer#awaitingApproval} to the caller.
 */
public record HilDecision(
        boolean allowed,
        String reason,           // "high_cost" | "destructive_op" | "tenant_policy" | null
        double estimatedCostUsd
) {
    public static HilDecision allow() { return new HilDecision(true, null, 0.0); }

    public static HilDecision require(String reason, double cost) {
        return new HilDecision(false, reason, cost);
    }
}
