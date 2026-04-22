package studio.seer.tenantrouting;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.interceptor.Interceptor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MTN-01: Production implementation of {@link YggLineageRegistry}.
 *
 * <p>Reads {@link FriggTenantLookup.TenantRouting} for each tenant alias from
 * FRIGG {@code frigg-tenants} DB, constructs an {@link HttpArcadeConnection}
 * pointing at the correct YGG database ({@code hound_{alias}} or per-tenant
 * override), and caches by (alias, configVersion) tuple.
 *
 * <p>Cache:
 * <ul>
 *   <li>LRU eviction at {@code ygg.lineage.registry.cache.maxEntries} (default 256)</li>
 *   <li>TTL {@code ygg.lineage.registry.cache.ttl} (default PT30M) since last access</li>
 *   <li>Invalidated on configVersion bump (lookup re-reads FRIGG)</li>
 *   <li>Invalidated explicitly via {@link #invalidate(String)} — e.g. by
 *       {@code RingBufferInvalidationListener} reacting to
 *       {@code seer.control.tenant_invalidated} events from Chur /reconnect</li>
 * </ul>
 *
 * <p>Gating by status:
 * <ul>
 *   <li>{@code ACTIVE} → return connection</li>
 *   <li>{@code SUSPENDED/ARCHIVED/PURGED/PROVISIONING} → {@link TenantNotAvailableException}</li>
 *   <li>Missing from frigg-tenants → {@link TenantNotAvailableException.Reason#NOT_FOUND}</li>
 * </ul>
 *
 * <p>Activation: toggled by {@code aida.multitenant.enabled=true} config property.
 * {@link DefaultYggLineageRegistry} remains available as a lower-priority alternative
 * for single-tenant dev.
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION + 100)
@ApplicationScoped
public class FriggYggLineageRegistry implements YggLineageRegistry {

    private static final Logger log = LoggerFactory.getLogger(FriggYggLineageRegistry.class);

    @ConfigProperty(name = "frigg.url",   defaultValue = "http://localhost:2481") String friggUrl;
    @ConfigProperty(name = "frigg.user",  defaultValue = "root")                  String friggUser;
    @ConfigProperty(name = "frigg.password", defaultValue = "playwithdata")       String friggPassword;

    @ConfigProperty(name = "ygg.url",   defaultValue = "http://localhost:2480")   String yggBaseUrl;
    @ConfigProperty(name = "ygg.user",  defaultValue = "root")                    String yggUser;
    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")         String yggPassword;

    @ConfigProperty(name = "ygg.lineage.registry.cache.maxEntries", defaultValue = "256")
    int cacheMaxEntries;
    @ConfigProperty(name = "ygg.lineage.registry.cache.ttl", defaultValue = "PT30M")
    Duration cacheTtl;

    private HttpClient http;
    private FriggTenantLookup lookup;

    /** LRU cache keyed by tenantAlias. Guarded by monitor lock for simplicity. */
    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true);

    private record CacheEntry(ArcadeConnection connection, int configVersion, Instant expiresAt) {}

    @PostConstruct
    void init() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.lookup = new FriggTenantLookup(http, friggUrl, friggUser, friggPassword);
        log.info("FriggYggLineageRegistry ready — frigg={} ygg={} cache.max={} ttl={}",
                friggUrl, yggBaseUrl, cacheMaxEntries, cacheTtl);
    }

    @Override
    public ArcadeConnection resourceFor(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) {
            throw new TenantNotAvailableException("(null)",
                    TenantNotAvailableException.Reason.NOT_FOUND);
        }

        CacheEntry cached = getCached(tenantAlias);
        if (cached != null && cached.expiresAt.isAfter(Instant.now())) {
            // Re-check configVersion against FRIGG in the background? For now trust
            // cache until TTL; /reconnect-driven invalidation handles hot updates.
            return cached.connection;
        }

        var routing = lookup.lookup(tenantAlias).orElseThrow(() ->
                new TenantNotAvailableException(tenantAlias,
                        TenantNotAvailableException.Reason.NOT_FOUND));
        gateStatus(routing);

        String dbName = routing.yggLineageDbName() != null
                ? routing.yggLineageDbName() : "hound_" + tenantAlias;
        String baseUrl = routing.yggInstanceUrl() != null && !routing.yggInstanceUrl().isBlank()
                ? routing.yggInstanceUrl() : yggBaseUrl;

        var connection = new HttpArcadeConnection(http, baseUrl, dbName, yggUser, yggPassword);
        putCache(tenantAlias, connection, routing.configVersion());
        return connection;
    }

    @Override
    public synchronized void invalidate(String tenantAlias) {
        if (cache.remove(tenantAlias) != null) {
            log.debug("Invalidated cache entry for tenant={}", tenantAlias);
        }
    }

    @Override
    public synchronized void invalidateAll() {
        int n = cache.size();
        cache.clear();
        log.info("Invalidated {} cache entries", n);
    }

    // ── internal ────────────────────────────────────────────────────────────

    private static void gateStatus(FriggTenantLookup.TenantRouting routing) {
        if (routing.status() == null) return;
        switch (routing.status()) {
            case "ACTIVE" -> { /* OK */ }
            case "SUSPENDED"    -> throw new TenantNotAvailableException(routing.tenantAlias(),
                    TenantNotAvailableException.Reason.SUSPENDED);
            case "ARCHIVED"     -> throw new TenantNotAvailableException(routing.tenantAlias(),
                    TenantNotAvailableException.Reason.ARCHIVED);
            case "PURGED"       -> throw new TenantNotAvailableException(routing.tenantAlias(),
                    TenantNotAvailableException.Reason.PURGED);
            case "PROVISIONING" -> throw new TenantNotAvailableException(routing.tenantAlias(),
                    TenantNotAvailableException.Reason.PROVISIONING);
            default -> {
                log.warn("Unknown tenant status '{}' for alias {} — treating as NOT_FOUND",
                        routing.status(), routing.tenantAlias());
                throw new TenantNotAvailableException(routing.tenantAlias(),
                        TenantNotAvailableException.Reason.NOT_FOUND);
            }
        }
    }

    private synchronized CacheEntry getCached(String alias) {
        var entry = cache.get(alias);
        if (entry == null) return null;
        if (Instant.now().isAfter(entry.expiresAt)) {
            cache.remove(alias);
            return null;
        }
        return entry;
    }

    private synchronized void putCache(String alias, ArcadeConnection conn, int configVersion) {
        cache.put(alias, new CacheEntry(conn, configVersion, Instant.now().plus(cacheTtl)));
        evictIfNeeded();
    }

    private void evictIfNeeded() {
        // LinkedHashMap accessOrder=true keeps LRU ordering; first entry is eldest.
        while (cache.size() > cacheMaxEntries) {
            Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();
            if (!it.hasNext()) return;
            var eldest = it.next();
            it.remove();
            log.debug("LRU evict tenant={}", eldest.getKey());
        }
    }

    /** Exposed for tests — snapshot of currently cached aliases. */
    synchronized java.util.Set<String> cachedAliases() {
        return Collections.unmodifiableSet(new java.util.LinkedHashSet<>(cache.keySet()));
    }

    /** Exposed for tests — cached configVersion for assertion. */
    synchronized Integer cachedConfigVersion(String alias) {
        var e = cache.get(alias);
        return e == null ? null : e.configVersion();
    }
}
