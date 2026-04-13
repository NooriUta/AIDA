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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     */
    public void save(Session session) {
        try {
            String json = MAPPER.writeValueAsString(session);
            // Delete existing record (idempotent)
            frigg.sql("DELETE FROM dali_sessions WHERE id = :id",
                    Map.of("id", session.id()));
            // Insert fresh record — use params to avoid JSON escaping issues
            frigg.sql("INSERT INTO dali_sessions SET id = :id, startedAt = :startedAt, sessionJson = :json",
                    Map.of("id", session.id(),
                           "startedAt", session.startedAt().toString(),
                           "json", json));
        } catch (JsonProcessingException e) {
            log.error("[SessionRepository] Failed to serialise session {}: {}", session.id(), e.getMessage());
        } catch (Exception e) {
            log.warn("[SessionRepository] Failed to save session {}: {}", session.id(), e.getMessage());
        }
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

    private Optional<Session> deserialise(String json) {
        try {
            return Optional.of(MAPPER.readValue(json, Session.class));
        } catch (Exception e) {
            log.warn("[SessionRepository] Failed to deserialise session: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
