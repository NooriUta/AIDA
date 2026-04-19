package studio.seer.dali.skadi;

import com.skadi.SkadiFetcher;
import com.skadi.adapters.ArcadeDBSkadiFetcher;
import com.skadi.adapters.ClickHouseSkadiFetcher;
import com.skadi.adapters.OracleSkadiFetcher;
import com.skadi.adapters.PostgreSQLSkadiFetcher;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * CDI singleton — owns one SKADI adapter per supported database type.
 *
 * <p>All adapters use DriverManager (no connection pool): {@link SkadiFetcher#close()}
 * is a no-op for JDBC adapters and is called here only to satisfy the {@link AutoCloseable}
 * contract in case future adapters do hold resources.
 *
 * <p>Adapter detection from JDBC URL prefix:
 * <ul>
 *   <li>{@code jdbc:oracle:*}      → oracle</li>
 *   <li>{@code jdbc:postgresql:*}  → postgresql</li>
 *   <li>{@code jdbc:clickhouse:*}  → clickhouse</li>
 *   <li>{@code *arcadedb*}         → arcadedb</li>
 * </ul>
 */
@ApplicationScoped
public class SkadiFetcherRegistry {

    private static final Logger log = LoggerFactory.getLogger(SkadiFetcherRegistry.class);

    private final Map<String, SkadiFetcher> fetchers;

    public SkadiFetcherRegistry() {
        fetchers = Map.of(
                "oracle",     new OracleSkadiFetcher(),
                "postgresql", new PostgreSQLSkadiFetcher(),
                "clickhouse", new ClickHouseSkadiFetcher(),
                "arcadedb",   new ArcadeDBSkadiFetcher()
        );
        log.info("SkadiFetcherRegistry initialized: adapters={}", fetchers.keySet());
    }

    /**
     * Returns the adapter registered under {@code adapterName} (case-insensitive).
     *
     * @param adapterName e.g. {@code "oracle"}, {@code "postgresql"}
     * @return matching adapter, or empty if not found
     */
    public Optional<SkadiFetcher> get(String adapterName) {
        if (adapterName == null) return Optional.empty();
        return Optional.ofNullable(fetchers.get(adapterName.toLowerCase()));
    }

    /**
     * Detects the correct adapter from a JDBC (or HTTP) URL.
     *
     * @param sourceUrl JDBC URL or ArcadeDB HTTP URL
     * @return matching adapter, or empty if the URL pattern is not recognised
     */
    public Optional<SkadiFetcher> detectByUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) return Optional.empty();
        String lower = sourceUrl.toLowerCase();
        if (lower.startsWith("jdbc:oracle:"))     return get("oracle");
        if (lower.startsWith("jdbc:postgresql:")) return get("postgresql");
        if (lower.startsWith("jdbc:clickhouse:")) return get("clickhouse");
        if (lower.contains("arcadedb"))           return get("arcadedb");
        return Optional.empty();
    }

    @PreDestroy
    public void shutdown() {
        log.info("SkadiFetcherRegistry: shutting down {} adapter(s)", fetchers.size());
        fetchers.values().forEach(f -> {
            try {
                f.close();
            } catch (Exception e) {
                log.warn("Error closing SKADI adapter '{}': {}", f.adapterName(), e.getMessage());
            }
        });
    }
}
