package com.mimir.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for {@link MimirSession} in FRIGG {@code frigg-mimir-sessions} database.
 *
 * <p>Operations:
 * <ul>
 *     <li>{@link #save(MimirSession)} — UPSERT by sessionId (idempotent)</li>
 *     <li>{@link #findById(String)} — SELECT by sessionId</li>
 *     <li>{@link #findByTenant(String, int)} — SELECT recent N sessions for tenant</li>
 * </ul>
 *
 * <p>Failures are logged WARN and surfaced via Optional.empty() — MIMIR LLM flow
 * не должен валиться если FRIGG недоступен (graceful degradation, как HEIMDALL).
 */
@ApplicationScoped
public class MimirSessionRepository {

    private static final Logger LOG = Logger.getLogger(MimirSessionRepository.class);

    @Inject @RestClient FriggClient frigg;
    @Inject ObjectMapper mapper;

    @ConfigProperty(name = "mimir.frigg.session-db", defaultValue = "frigg-mimir-sessions")
    String sessionDb;

    /** Idempotent UPSERT by sessionId. Returns true on success, false on FRIGG failure. */
    public boolean save(MimirSession session) {
        try {
            String sql = """
                    UPDATE MimirSession SET
                        sessionId      = :sessionId,
                        tenantAlias    = :tenantAlias,
                        status         = :status,
                        toolCallsUsed  = :toolCallsUsed,
                        highlightIds   = :highlightIds,
                        pauseState     = :pauseState,
                        createdAt      = :createdAt,
                        updatedAt      = :updatedAt
                    UPSERT WHERE sessionId = :sessionId
                    """;
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId",     session.sessionId());
            params.put("tenantAlias",   session.tenantAlias());
            params.put("status",        session.status());
            params.put("toolCallsUsed", toJson(session.toolCallsUsed()));
            params.put("highlightIds",  toJson(session.highlightIds()));
            params.put("pauseState",    toJson(session.pauseState()));
            params.put("createdAt",     session.createdAt() == null ? null : session.createdAt().toString());
            params.put("updatedAt",     session.updatedAt() == null ? null : session.updatedAt().toString());

            frigg.command(sessionDb, new FriggClient.FriggCommand("sql", sql, params));
            return true;
        } catch (Exception e) {
            LOG.warnf(e, "MimirSession.save failed for sessionId=%s — FRIGG may be down",
                    session.sessionId());
            return false;
        }
    }

    public Optional<MimirSession> findById(String sessionId) {
        try {
            String sql = "SELECT FROM MimirSession WHERE sessionId = :sid LIMIT 1";
            FriggClient.QueryResult r = frigg.query(sessionDb,
                    new FriggClient.FriggQuery("sql", sql, Map.of("sid", sessionId)));
            if (r.result().isEmpty()) return Optional.empty();
            return Optional.of(toSession(r.result().get(0)));
        } catch (Exception e) {
            LOG.warnf(e, "MimirSession.findById failed for sessionId=%s", sessionId);
            return Optional.empty();
        }
    }

    public List<MimirSession> findByTenant(String tenantAlias, int limit) {
        try {
            int boundedLimit = Math.min(Math.max(limit, 1), 100);
            String sql = "SELECT FROM MimirSession WHERE tenantAlias = :alias " +
                         "ORDER BY updatedAt DESC LIMIT :lim";
            FriggClient.QueryResult r = frigg.query(sessionDb,
                    new FriggClient.FriggQuery("sql", sql,
                            Map.of("alias", tenantAlias, "lim", boundedLimit)));
            return r.result().stream().map(this::toSession).toList();
        } catch (Exception e) {
            LOG.warnf(e, "MimirSession.findByTenant failed for alias=%s", tenantAlias);
            return List.of();
        }
    }

    // ── Marshaling ────────────────────────────────────────────────────────────

    private String toJson(Object o) {
        if (o == null) return null;
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            LOG.warnf(e, "JSON marshal failed for %s", o.getClass().getSimpleName());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private MimirSession toSession(Map<String, Object> row) {
        return new MimirSession(
                String.valueOf(row.get("sessionId")),
                String.valueOf(row.get("tenantAlias")),
                String.valueOf(row.getOrDefault("status", "completed")),
                parseStringList(row.get("toolCallsUsed")),
                parseStringList(row.get("highlightIds")),
                row.get("pauseState"),
                parseInstant(row.get("createdAt")),
                parseInstant(row.get("updatedAt")));
    }

    @SuppressWarnings("unchecked")
    private List<String> parseStringList(Object v) {
        if (v == null) return List.of();
        if (v instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (v instanceof String s && !s.isBlank()) {
            try {
                return mapper.readValue(s, List.class);
            } catch (Exception e) {
                return List.of();
            }
        }
        return List.of();
    }

    private Instant parseInstant(Object v) {
        if (v == null) return null;
        try {
            return Instant.parse(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}
