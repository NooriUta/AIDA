package studio.seer.anvil.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import studio.seer.anvil.model.ImpactResult;

import java.util.concurrent.TimeUnit;

/**
 * AV-03 — In-memory Caffeine cache for impact/lineage results.
 *
 * <p>MTN-30: the cache key now embeds {@code tenantAlias} so that a request
 * from tenant {@code B} can never read or evict an entry seeded by tenant
 * {@code A} — even if both reference the same {@code nodeId}/{@code dbName}.
 * Before the change, cross-tenant cache poisoning was possible whenever two
 * tenants happened to collide on those fields.
 *
 * Key: {@code tenantAlias:nodeId:direction:maxHops:dbName}
 * TTL: 5 minutes (covers typical analyst session between harvests).
 * Invalidation:
 *   - {@link #invalidateDb(String)}      — on dali.session_completed per db
 *   - {@link #invalidateTenant(String)}  — on seer.control.tenant_invalidated
 */
@ApplicationScoped
public class AnvilCache {

    private final Cache<String, ImpactResult> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public ImpactResult get(String key) {
        return cache.getIfPresent(key);
    }

    public void put(String key, ImpactResult result) {
        cache.put(key, result);
    }

    /** Invalidate all entries for a specific database (called on dali.session_completed). */
    public void invalidateDb(String dbName) {
        cache.asMap().keySet().removeIf(k -> k.endsWith(":" + dbName));
    }

    /** MTN-30: drop every cache entry for a tenant (called on /reconnect fan-out). */
    public void invalidateTenant(String tenantAlias) {
        String prefix = tenantAlias + ":";
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    public long size() {
        return cache.estimatedSize();
    }

    /** Expose underlying map — for tests that need a true full wipe via {@code asMap().clear()}. */
    public java.util.concurrent.ConcurrentMap<String, ImpactResult> asMap() {
        return cache.asMap();
    }

    /**
     * MTN-30: build a cache key scoped by tenantAlias. Call sites must pass the
     * tenantAlias resolved by {@code TenantContextFilter} (never derive it from
     * request body — that would re-introduce cross-tenant poisoning).
     */
    public static String key(String tenantAlias, String nodeId, String direction, int maxHops, String dbName) {
        return tenantAlias + ":" + nodeId + ":" + direction + ":" + maxHops + ":" + dbName;
    }
}
