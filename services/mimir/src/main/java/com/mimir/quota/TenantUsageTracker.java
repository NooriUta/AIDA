package com.mimir.quota;

import com.mimir.heimdall.MimirEventEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Post-call usage recorder. Updates the day's aggregate in FRIGG and emits a
 * {@code TOKEN_USAGE_RECORDED} HEIMDALL event. Failures are logged WARN — a
 * tracking outage cannot break the response path.
 *
 * <p>Token counts are populated from whatever the LLM provider returned via
 * the orchestrator. When the provider is silent, {@link #estimateTokens}
 * provides a coarse 4-chars-per-token heuristic so quota gates have something
 * to measure against (better than 0).
 */
@ApplicationScoped
public class TenantUsageTracker {

    private static final Logger LOG = Logger.getLogger(TenantUsageTracker.class);

    @Inject TenantQuotaStore store;
    @Inject LlmPriceBook    prices;
    @Inject TenantQuotaGuard guard;
    @Inject MimirEventEmitter emitter;

    public void record(String tenantAlias, String provider, String model,
                       long promptTokens, long completionTokens) {
        if (tenantAlias == null || tenantAlias.isBlank()) return;
        try {
            String date = LocalDate.now(ZoneOffset.UTC).toString();
            double cost = prices.estimate(provider, model, promptTokens, completionTokens);

            DailyUsage current = store.findUsage(tenantAlias, date)
                    .orElse(DailyUsage.zero(tenantAlias, date));
            DailyUsage updated = current.add(promptTokens, completionTokens, cost);
            store.upsertUsage(updated);

            emitter.tokenUsageRecorded(tenantAlias, provider, model,
                    promptTokens, completionTokens, cost);

            // Force the next quota check to see fresh totals
            guard.invalidate(tenantAlias);
        } catch (Exception e) {
            LOG.warnf(e, "TenantUsageTracker.record failed for tenant=%s — usage not persisted", tenantAlias);
        }
    }

    /** Coarse heuristic when provider doesn't report token counts. */
    public static long estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0L;
        return Math.max(1L, text.length() / 4);
    }
}
