package com.mimir.quota;

import java.time.Instant;

/**
 * Output of a {@link TenantQuotaGuard#check} call. {@link #allowed()} false
 * means the orchestrator must short-circuit with a quota-exceeded answer
 * before invoking any model.
 */
public record QuotaCheckResult(
        boolean allowed,
        String reason,        // "daily_tokens" | "monthly_tokens" | "daily_cost" | "monthly_cost"
        long current,
        double currentValue,
        long limit,
        double limitValue,
        Instant resetAt
) {
    private static final QuotaCheckResult OK = new QuotaCheckResult(
            true, null, 0L, 0.0, 0L, 0.0, null);

    public static QuotaCheckResult ok() { return OK; }

    public static QuotaCheckResult tokensExceeded(String reason, long current, long limit, Instant resetAt) {
        return new QuotaCheckResult(false, reason, current, current, limit, limit, resetAt);
    }

    public static QuotaCheckResult costExceeded(String reason, double current, double limit, Instant resetAt) {
        return new QuotaCheckResult(false, reason, 0L, current, 0L, limit, resetAt);
    }
}
