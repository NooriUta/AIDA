package studio.seer.lineage.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SHT-09: Token-bucket rate limiter per tenantAlias.
 *
 * Soft limit: 100 req/sec. Hard limit: 1000 req/min.
 * Superadmin role (X-Seer-Role: super-admin) bypasses all limits.
 * Local per-replica (no distributed coordination — Q-RL-1 decision).
 */
@ApplicationScoped
public class TenantRateLimiter {

    private static final int SOFT_PER_SEC  = 100;
    private static final int HARD_PER_MIN  = 1_000;

    record Bucket(AtomicLong secondCount, AtomicLong minuteCount,
                  AtomicLong secondReset, AtomicLong minuteReset) {

        static Bucket create(long now) {
            return new Bucket(new AtomicLong(0), new AtomicLong(0),
                    new AtomicLong(now + 1_000), new AtomicLong(now + 60_000));
        }

        boolean check(long now) {
            if (now > secondReset.get()) { secondCount.set(0); secondReset.set(now + 1_000); }
            if (now > minuteReset.get()) { minuteCount.set(0); minuteReset.set(now + 60_000); }
            long sec = secondCount.incrementAndGet();
            long min = minuteCount.incrementAndGet();
            return sec <= SOFT_PER_SEC && min <= HARD_PER_MIN;
        }
    }

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Returns true if the request should be allowed, false if rate-limited.
     * Does NOT abort the context — callers should call {@link #checkAndAbort} instead.
     */
    public boolean allow(String tenantAlias, String role) {
        if ("super-admin".equals(role)) return true;
        String key = tenantAlias != null ? tenantAlias : "default";
        long now = System.currentTimeMillis();
        Bucket b = buckets.computeIfAbsent(key, k -> Bucket.create(now));
        return b.check(now);
    }

    /**
     * Checks the rate limit and aborts the request with 429 if exceeded.
     * Returns true if aborted (caller should stop processing).
     */
    public boolean checkAndAbort(ContainerRequestContext ctx) {
        String alias = ctx.getHeaderString("X-Seer-Tenant-Alias");
        String role  = ctx.getHeaderString("X-Seer-Role");
        if (!allow(alias, role)) {
            ctx.abortWith(Response.status(429)
                    .header("Retry-After", "60")
                    .entity("{\"error\":\"Too Many Requests\",\"retryAfter\":60}")
                    .type("application/json")
                    .build());
            return true;
        }
        return false;
    }
}
