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
 * Production implementation of {@link YggSourceArchiveRegistry}.
 *
 * <p>Reads {@link FriggTenantLookup.TenantRouting} for each tenant alias from FRIGG,
 * constructs an {@link HttpArcadeConnection} pointing at {@code hound_src_{alias}}
 * (or per-tenant {@code yggSourceArchiveDbName} override if present), and caches
 * by (alias, configVersion) with LRU + TTL eviction.
 *
 * <p>Activation: toggled by {@code aida.multitenant.enabled=true}.
 * {@link DefaultYggSourceArchiveRegistry} remains available as lower-priority fallback.
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION + 100)
@ApplicationScoped
public class FriggYggSourceArchiveRegistry implements YggSourceArchiveRegistry {

    private static final Logger log = LoggerFactory.getLogger(FriggYggSourceArchiveRegistry.class);

    @ConfigProperty(name = "frigg.url",      defaultValue = "http://localhost:2481") String friggUrl;
    @ConfigProperty(name = "frigg.user",     defaultValue = "root")                  String friggUser;
    @ConfigProperty(name = "frigg.password", defaultValue = "playwithdata")          String friggPassword;

    @ConfigProperty(name = "ygg.source.url", defaultValue = "${ygg.url:http://localhost:2480}") String yggSourceUrl;
    @ConfigProperty(name = "ygg.user",       defaultValue = "root")                              String yggUser;
    @ConfigProperty(name = "ygg.password",   defaultValue = "playwithdata")                      String yggPassword;

    @ConfigProperty(name = "ygg.source.registry.cache.maxEntries", defaultValue = "256")
    int cacheMaxEntries;
    @ConfigProperty(name = "ygg.source.registry.cache.ttl", defaultValue = "PT30M")
    Duration cacheTtl;

    private HttpClient http;
    private FriggTenantLookup lookup;

    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true);
    private record CacheEntry(ArcadeConnection connection, int configVersion, Instant expiresAt) {}

    @PostConstruct
    void init() {
        this.http   = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.lookup = new FriggTenantLookup(http, friggUrl, friggUser, friggPassword);
        log.info("FriggYggSourceArchiveRegistry ready — frigg={} yggSource={}", friggUrl, yggSourceUrl);
    }

    @Override
    public ArcadeConnection resourceFor(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) {
            throw new TenantNotAvailableException("(null)", TenantNotAvailableException.Reason.NOT_FOUND);
        }

        CacheEntry cached = getCached(tenantAlias);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.connection();
        }

        var routing = lookup.lookup(tenantAlias).orElseThrow(() ->
                new TenantNotAvailableException(tenantAlias, TenantNotAvailableException.Reason.NOT_FOUND));
        gateStatus(routing);

        String dbName = "hound_src_" + tenantAlias;
        String baseUrl = routing.yggInstanceUrl() != null && !routing.yggInstanceUrl().isBlank()
                ? routing.yggInstanceUrl() : yggSourceUrl;

        var connection = new HttpArcadeConnection(http, baseUrl, dbName, yggUser, yggPassword);
        putCache(tenantAlias, connection, routing.configVersion());
        return connection;
    }

    @Override
    public synchronized void invalidate(String tenantAlias) {
        cache.remove(tenantAlias);
    }

    @Override
    public synchronized void invalidateAll() {
        cache.clear();
    }

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
            default -> throw new TenantNotAvailableException(routing.tenantAlias(),
                    TenantNotAvailableException.Reason.NOT_FOUND);
        }
    }

    private synchronized CacheEntry getCached(String alias) {
        var entry = cache.get(alias);
        if (entry == null) return null;
        if (Instant.now().isAfter(entry.expiresAt())) { cache.remove(alias); return null; }
        return entry;
    }

    private synchronized void putCache(String alias, ArcadeConnection conn, int configVersion) {
        cache.put(alias, new CacheEntry(conn, configVersion, Instant.now().plus(cacheTtl)));
        while (cache.size() > cacheMaxEntries) {
            Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();
            if (!it.hasNext()) break;
            it.next(); it.remove();
        }
    }

    synchronized java.util.Set<String> cachedAliases() {
        return Collections.unmodifiableSet(new java.util.LinkedHashSet<>(cache.keySet()));
    }
}
