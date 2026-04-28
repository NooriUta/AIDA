package studio.seer.heimdall.metrics;

import jakarta.enterprise.context.ApplicationScoped;
import studio.seer.heimdall.metrics.TenantMetricsSummary.TenantCounter;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * HTA-09: Per-tenant event counters.
 *
 * Maintains thread-safe in-memory counters keyed by tenantAlias.
 * LRU-eviction at MAX_TENANTS to cap memory; recency is tracked via lastEventAt.
 *
 * Events without a tenantAlias in payload are routed to {@link #PLATFORM_KEY}.
 */
@ApplicationScoped
public class TenantMetricsService {

    static final String PLATFORM_KEY = "_platform";
    static final int    MAX_TENANTS  = 1000;
    private static final int TOP_N   = 20;

    private final ConcurrentHashMap<String, Counters> byTenant = new ConcurrentHashMap<>();

    public void record(HeimdallEvent event) {
        if (event == null) return;
        String alias = extractAlias(event);
        Counters c   = byTenant.computeIfAbsent(alias, k -> new Counters());
        c.total.incrementAndGet();
        c.lastEventAt.set(event.timestamp() > 0 ? event.timestamp() : System.currentTimeMillis());

        if (event.level() == EventLevel.ERROR) c.errors.incrementAndGet();

        String type = event.eventType();
        if (type != null) {
            if (type.contains("SESSION_STARTED"))   c.parseSessions.incrementAndGet();
            else if (type.contains("JOB_STARTED"))  c.activeJobs.incrementAndGet();
            else if (type.contains("JOB_COMPLETED") || type.contains("JOB_FAILED")) {
                c.activeJobs.updateAndGet(v -> Math.max(0, v - 1));
            }
            else if (type.contains("ATOM"))         c.atoms.incrementAndGet();
        }

        evictIfNeeded();
    }

    public TenantMetricsSummary summary() {
        List<TenantCounter> all = byTenant.entrySet().stream()
                .map(e -> e.getValue().toCounter(e.getKey()))
                .sorted(Comparator.comparingLong(TenantCounter::totalEvents).reversed())
                .toList();

        List<TenantCounter> top  = all.stream().limit(TOP_N).toList();
        List<TenantCounter> tail = all.stream().skip(TOP_N).toList();

        TenantCounter rest = aggregate("_rest", tail);
        TenantCounter totals = aggregate("_total", all);

        return new TenantMetricsSummary(top, rest, totals, all.size());
    }

    public Map<String, TenantCounter> raw() {
        return byTenant.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toCounter(e.getKey())));
    }

    /* ── internal ─────────────────────────────────────────────────────────── */

    private static String extractAlias(HeimdallEvent event) {
        Map<String, Object> payload = event.payload();
        if (payload == null) return PLATFORM_KEY;
        Object v = payload.get("tenantAlias");
        if (v instanceof String s && !s.isBlank()) return s;
        Object v2 = payload.get("tenant");
        if (v2 instanceof String s && !s.isBlank()) return s;
        return PLATFORM_KEY;
    }

    private void evictIfNeeded() {
        if (byTenant.size() <= MAX_TENANTS) return;
        // Evict the LRU (oldest lastEventAt) tenant. O(N) but called rarely; simple & predictable.
        byTenant.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().lastEventAt.get()))
                .map(Map.Entry::getKey)
                .ifPresent(byTenant::remove);
    }

    private static TenantCounter aggregate(String label, List<TenantCounter> list) {
        long total = 0, sessions = 0, atoms = 0, jobs = 0, errors = 0, last = 0;
        for (TenantCounter c : list) {
            total    += c.totalEvents();
            sessions += c.parseSessions();
            atoms    += c.atoms();
            jobs     += c.activeJobs();
            errors   += c.errors();
            if (c.lastEventAt() > last) last = c.lastEventAt();
        }
        return new TenantCounter(label, total, sessions, atoms, jobs, errors, last);
    }

    private static final class Counters {
        final AtomicLong total         = new AtomicLong();
        final AtomicLong parseSessions = new AtomicLong();
        final AtomicLong atoms         = new AtomicLong();
        final AtomicLong activeJobs    = new AtomicLong();
        final AtomicLong errors        = new AtomicLong();
        final AtomicLong lastEventAt   = new AtomicLong();

        TenantCounter toCounter(String alias) {
            return new TenantCounter(
                    alias,
                    total.get(),
                    parseSessions.get(),
                    atoms.get(),
                    activeJobs.get(),
                    errors.get(),
                    lastEventAt.get());
        }
    }
}
