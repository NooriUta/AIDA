package com.mimir.quota;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

/**
 * Pre-call quota gate. {@link #check} returns {@link QuotaCheckResult#ok()}
 * when the tenant has either no quota row or current usage is below all
 * configured limits. Call this before the model dispatch in
 * {@link com.mimir.orchestration.MimirOrchestrator}.
 *
 * <p>Cache TTL is short (default 30s) so that limit changes by an admin or
 * usage updates by {@link TenantUsageTracker} surface quickly.
 */
@ApplicationScoped
public class TenantQuotaGuard {

    private static final Logger LOG = Logger.getLogger(TenantQuotaGuard.class);

    @Inject TenantQuotaStore store;

    @ConfigProperty(name = "mimir.quota.cache.ttl-seconds", defaultValue = "30")
    int ttlSeconds;

    @ConfigProperty(name = "mimir.quota.cache.max-size", defaultValue = "1000")
    int maxSize;

    private Cache<String, QuotaCheckResult> cache;

    @PostConstruct
    void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .maximumSize(maxSize)
                .build();
        LOG.infof("Quota guard cache initialised (TTL=%ds, max=%d)", ttlSeconds, maxSize);
    }

    public QuotaCheckResult check(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return QuotaCheckResult.ok();
        return cache.get(tenantAlias, this::computeFresh);
    }

    public void invalidate(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return;
        cache.invalidate(tenantAlias);
    }

    private QuotaCheckResult computeFresh(String tenantAlias) {
        TenantQuota q = store.findQuota(tenantAlias).orElse(null);
        if (q == null) return QuotaCheckResult.ok();
        ZoneId zone = resolveZone(q.resetTimezone());
        LocalDate today = LocalDate.now(zone);

        DailyUsage daily = store.findUsage(tenantAlias, today.toString()).orElse(DailyUsage.zero(tenantAlias, today.toString()));

        if (q.dailyTokenLimit() > 0 && daily.totalTokens() >= q.dailyTokenLimit()) {
            return QuotaCheckResult.tokensExceeded(
                    "daily_tokens", daily.totalTokens(), q.dailyTokenLimit(), nextDayStart(today, zone));
        }
        if (q.dailyCostLimitUsd() > 0 && daily.costEstimateUsd() >= q.dailyCostLimitUsd()) {
            return QuotaCheckResult.costExceeded(
                    "daily_cost", daily.costEstimateUsd(), q.dailyCostLimitUsd(), nextDayStart(today, zone));
        }

        if (q.monthlyTokenLimit() > 0 || q.monthlyCostLimitUsd() > 0) {
            YearMonth month = YearMonth.from(today);
            List<DailyUsage> all = store.listUsage(tenantAlias, 31);
            long monthTokens = 0L;
            double monthCost = 0.0;
            for (DailyUsage u : all) {
                if (u.date() != null && u.date().startsWith(month.toString())) {
                    monthTokens += u.totalTokens();
                    monthCost   += u.costEstimateUsd();
                }
            }
            if (q.monthlyTokenLimit() > 0 && monthTokens >= q.monthlyTokenLimit()) {
                return QuotaCheckResult.tokensExceeded(
                        "monthly_tokens", monthTokens, q.monthlyTokenLimit(), nextMonthStart(month, zone));
            }
            if (q.monthlyCostLimitUsd() > 0 && monthCost >= q.monthlyCostLimitUsd()) {
                return QuotaCheckResult.costExceeded(
                        "monthly_cost", monthCost, q.monthlyCostLimitUsd(), nextMonthStart(month, zone));
            }
        }
        return QuotaCheckResult.ok();
    }

    private static ZoneId resolveZone(String tz) {
        if (tz == null || tz.isBlank()) return ZoneId.of("UTC");
        try { return ZoneId.of(tz); } catch (Exception e) { return ZoneId.of("UTC"); }
    }

    private static Instant nextDayStart(LocalDate today, ZoneId zone) {
        return today.plusDays(1).atStartOfDay(zone).toInstant();
    }

    private static Instant nextMonthStart(YearMonth month, ZoneId zone) {
        return month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();
    }
}
