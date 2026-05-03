package com.mimir.quota;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Daily aggregated usage row — one per (tenantAlias, date). Stored as a
 * vertex in FRIGG type {@code DaliMimirUsage} with a unique compound key
 * {@code tenantAlias|date} (date in {@code yyyy-MM-dd} ISO format).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DailyUsage(
        String tenantAlias,
        String date,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        double costEstimateUsd,
        int requestCount
) {
    public static DailyUsage zero(String tenantAlias, String date) {
        return new DailyUsage(tenantAlias, date, 0L, 0L, 0L, 0.0, 0);
    }

    public DailyUsage add(long prompt, long completion, double cost) {
        return new DailyUsage(
                tenantAlias,
                date,
                promptTokens + prompt,
                completionTokens + completion,
                totalTokens + prompt + completion,
                costEstimateUsd + cost,
                requestCount + 1
        );
    }
}
