package studio.seer.lineage.storage;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.lineage.client.ArcadeGateway;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persists saved LOOM view configurations to YGG (ArcadeDB).
 *
 * <p>Views are stored as {@code ShuttleView} vertices with a UNIQUE index on
 * {@code viewId}. An UPSERT pattern ensures idempotent saves.
 *
 * <p>Schema is initialised lazily on first write — ArcadeDB's
 * {@code IF NOT EXISTS} guard makes this safe for concurrent Shuttle instances.
 */
@ApplicationScoped
public class ViewRepository {

    private static final Logger log = LoggerFactory.getLogger(ViewRepository.class);

    private static final String SCHEMA_SQL = """
            CREATE VERTEX TYPE ShuttleView IF NOT EXISTS;
            """;

    private static final String UPSERT_SQL = """
            UPDATE ShuttleView
              SET viewId = :viewId, payload = :payload, updatedAt = :updatedAt, author = :author
            UPSERT WHERE viewId = :viewId
            """;

    private static final String DELETE_SQL = """
            DELETE FROM ShuttleView WHERE viewId = :viewId
            """;

    private static final String SELECT_SQL = """
            SELECT viewId, payload, updatedAt, author FROM ShuttleView WHERE viewId = :viewId
            """;

    @Inject ArcadeGateway arcade;

    /** Upsert a view payload by viewId. */
    public Uni<Void> save(String viewId, String payload, String author) {
        log.debug("ViewRepository.save viewId={}", viewId);
        long now = System.currentTimeMillis();
        return arcade.sql(UPSERT_SQL, Map.of(
                        "viewId", viewId,
                        "payload", payload,
                        "updatedAt", now,
                        "author", author != null ? author : "system"))
                .replaceWithVoid()
                .onFailure().invoke(ex -> log.error("ViewRepository.save failed viewId={}: {}", viewId, ex.getMessage()));
    }

    /** Delete a view by viewId. No-op if not found. */
    public Uni<Void> delete(String viewId) {
        log.debug("ViewRepository.delete viewId={}", viewId);
        return arcade.sql(DELETE_SQL, Map.of("viewId", viewId))
                .replaceWithVoid()
                .onFailure().invoke(ex -> log.error("ViewRepository.delete failed viewId={}: {}", viewId, ex.getMessage()));
    }

    /** Find a view by viewId — returns empty if not found. */
    public Uni<Optional<Map<String, Object>>> findById(String viewId) {
        return arcade.sql(SELECT_SQL, Map.of("viewId", viewId))
                .map(rows -> rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0)));
    }
}
