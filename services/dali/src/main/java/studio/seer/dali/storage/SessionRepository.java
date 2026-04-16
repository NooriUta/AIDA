package studio.seer.dali.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.shared.Session;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import studio.seer.shared.SessionStatus;

/**
 * Persists {@link Session} records to the {@code dali_sessions} document type in FRIGG.
 *
 * <p>Strategy: full JSON document stored in a {@code sessionJson} property.
 * This avoids schema migrations when Session fields change — just re-serialise.
 * The {@code id} and {@code startedAt} properties are stored as top-level
 * properties for indexed queries.
 */
@ApplicationScoped
public class SessionRepository {

    private static final Logger log = LoggerFactory.getLogger(SessionRepository.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Inject FriggGateway frigg;

    /**
     * Upserts a session into FRIGG.
     * Uses DELETE + INSERT to avoid partial-update complexity with ArcadeDB.
     *
     * <p>Key performance stats are stored as top-level indexed properties alongside the
     * full {@code sessionJson} blob so they are directly queryable from ArcadeDB SQL:
     * <pre>
     *   SELECT id, dialect, status, durationMs, atomCount, resolutionRate
     *   FROM dali_sessions WHERE status = 'COMPLETED' ORDER BY durationMs DESC
     * </pre>
     */
    public void save(Session session) {
        try {
            String json = MAPPER.writeValueAsString(session);
            // Delete existing record (idempotent)
            frigg.sql("DELETE FROM dali_sessions WHERE id = :id",
                    Map.of("id", session.id()));

            // Build params with nulls allowed (Map.of() forbids null values)
            boolean terminal = isTerminal(session.status());
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("id",              session.id());
            params.put("startedAt",       session.startedAt().toString());
            params.put("finishedAt",      terminal ? session.updatedAt().toString() : null);
            params.put("status",          session.status().name());
            params.put("dialect",         session.dialect());
            params.put("instanceId",      session.instanceId());
            params.put("durationMs",      session.durationMs());
            params.put("atomCount",       session.atomCount());
            params.put("vertexCount",     session.vertexCount());
            params.put("edgeCount",       session.edgeCount());
            params.put("droppedEdgeCount",session.droppedEdgeCount());
            params.put("resolutionRate",  session.resolutionRate());
            params.put("json",            json);

            frigg.sql(
                "INSERT INTO dali_sessions SET " +
                "id = :id, startedAt = :startedAt, finishedAt = :finishedAt, " +
                "status = :status, dialect = :dialect, instanceId = :instanceId, " +
                "durationMs = :durationMs, atomCount = :atomCount, " +
                "vertexCount = :vertexCount, edgeCount = :edgeCount, " +
                "droppedEdgeCount = :droppedEdgeCount, resolutionRate = :resolutionRate, " +
                "sessionJson = :json",
                params);
        } catch (JsonProcessingException e) {
            log.error("[SessionRepository] Failed to serialise session {}: {}", session.id(), e.getMessage());
        } catch (Exception e) {
            log.warn("[SessionRepository] Failed to save session {}: {}", session.id(), e.getMessage());
        }
    }

    private static boolean isTerminal(SessionStatus status) {
        return status == SessionStatus.COMPLETED
            || status == SessionStatus.FAILED
            || status == SessionStatus.CANCELLED;
    }

    /**
     * Returns all sessions from FRIGG, newest first (by startedAt).
     */
    public List<Session> findAll(int limit) {
        try {
            List<Map<String, Object>> rows = frigg.sql(
                    "SELECT sessionJson FROM dali_sessions ORDER BY startedAt DESC LIMIT " + limit);
            if (rows == null) return Collections.emptyList();
            return rows.stream()
                    .map(row -> row.get("sessionJson"))
                    .filter(v -> v instanceof String)
                    .map(v -> deserialise((String) v))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            log.warn("[SessionRepository] findAll failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Returns a single session by id, or empty if not found.
     */
    public Optional<Session> findById(String id) {
        try {
            List<Map<String, Object>> rows = frigg.sql(
                    "SELECT sessionJson FROM dali_sessions WHERE id = :id LIMIT 1",
                    Map.of("id", id));
            if (rows == null) return Optional.empty();
            return rows.stream()
                    .map(row -> row.get("sessionJson"))
                    .filter(v -> v instanceof String)
                    .map(v -> deserialise((String) v))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } catch (Exception e) {
            log.warn("[SessionRepository] findById {} failed: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Deletes sessions older than {@code cutoff} (by {@code startedAt}).
     * Returns the number of records deleted.
     */
    public int deleteOlderThan(java.time.Instant cutoff) {
        try {
            String cutoffStr = cutoff.toString();
            List<Map<String, Object>> cnt = frigg.sql(
                    "SELECT count(*) as cnt FROM dali_sessions WHERE startedAt < :cutoff",
                    Map.of("cutoff", cutoffStr));
            int count = (cnt != null && !cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number)
                    ? ((Number) cnt.get(0).get("cnt")).intValue() : 0;
            if (count > 0) {
                frigg.sql("DELETE FROM dali_sessions WHERE startedAt < :cutoff",
                        Map.of("cutoff", cutoffStr));
            }
            log.debug("[SessionRepository] deleteOlderThan {}: removed {} records", cutoff, count);
            return count;
        } catch (Exception e) {
            log.warn("[SessionRepository] deleteOlderThan failed: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Deletes all session records from FRIGG.
     * Intended for use in tests ({@code @AfterEach}) to prevent cross-run accumulation.
     */
    public void deleteAll() {
        try {
            frigg.sql("DELETE FROM `dali_sessions`");
            log.debug("[SessionRepository] deleteAll: cleared all session records");
        } catch (Exception e) {
            log.warn("[SessionRepository] deleteAll failed: {}", e.getMessage());
        }
    }

    private Optional<Session> deserialise(String json) {
        try {
            return Optional.of(MAPPER.readValue(json, Session.class));
        } catch (Exception e) {
            log.warn("[SessionRepository] Failed to deserialise session: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
