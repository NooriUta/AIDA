package com.mimir.quota;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Per-tenant LLM consumption limits stored alongside the BYOK config in FRIGG
 * (DaliTenantQuota). Zero / negative limits mean "unlimited".
 *
 * @param dailyTokenLimit       prompt+completion tokens summed per UTC day
 * @param monthlyTokenLimit     same, calendar month UTC
 * @param dailyCostLimitUsd     spend cap based on LlmPriceBook estimate
 * @param monthlyCostLimitUsd   same
 * @param resetTimezone         IANA TZ for day boundary; null/blank → UTC
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TenantQuota(
        long dailyTokenLimit,
        long monthlyTokenLimit,
        double dailyCostLimitUsd,
        double monthlyCostLimitUsd,
        String resetTimezone
) {
    public static TenantQuota unlimited() {
        return new TenantQuota(0L, 0L, 0.0, 0.0, "UTC");
    }
}
