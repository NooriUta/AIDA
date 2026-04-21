package studio.seer.anvil.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import studio.seer.anvil.model.ImpactResult;

import java.util.concurrent.TimeUnit;

/**
 * AV-03 — In-memory Caffeine cache for impact/lineage results.
 *
 * Key: {@code nodeId:direction:maxHops:dbName}
 * TTL: 5 minutes (covers typical analyst session between harvests).
 * Invalidation: call {@link #invalidateDb(String)} on dali.session_completed.
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

    public long size() {
        return cache.estimatedSize();
    }

    public static String key(String nodeId, String direction, int maxHops, String dbName) {
        return nodeId + ":" + direction + ":" + maxHops + ":" + dbName;
    }
}
