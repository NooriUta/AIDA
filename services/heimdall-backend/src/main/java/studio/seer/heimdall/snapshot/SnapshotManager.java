package studio.seer.heimdall.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.shared.HeimdallEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persists and retrieves HEIMDALL snapshots in FRIGG (ArcadeDB :2481).
 *
 * Schema (DDL on first use):
 *   CREATE VERTEX TYPE HeimdallSnapshot IF NOT EXISTS
 *   Fields: snapshotId (String), name (String), ts (Long), eventCount (Integer), eventsJson (String)
 *
 * Events are stored as a single JSON string per snapshot row,
 * not as individual vertices — optimised for write-once, read-rarely access pattern.
 */
@ApplicationScoped
public class SnapshotManager {

    private static final Logger LOG = Logger.getLogger(SnapshotManager.class);

    @Inject FriggGateway frigg;
    @Inject ObjectMapper mapper;

    private volatile boolean schemaEnsured = false;

    public Uni<String> save(String name, List<HeimdallEvent> events) {
        String id       = UUID.randomUUID().toString();
        long   ts       = System.currentTimeMillis();
        String safeName = (name == null || name.isBlank()) ? "unnamed" : name;

        String eventsJson;
        try {
            eventsJson = mapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            return Uni.createFrom().failure(e);
        }

        String sql = "INSERT INTO HeimdallSnapshot SET " +
                "snapshotId = :id, name = :name, ts = :ts, eventCount = :count, eventsJson = :json";

        return ensureSchema()
                .chain(() -> frigg.sql(sql, Map.of(
                        "id",    id,
                        "name",  safeName,
                        "ts",    ts,
                        "count", events.size(),
                        "json",  eventsJson)))
                .map(__ -> {
                    LOG.infof("Snapshot '%s' saved id=%s events=%d", safeName, id, events.size());
                    return id;
                })
                .onFailure().invoke(ex ->
                        LOG.errorf("Save snapshot failed: %s", ex.getMessage()));
    }

    public Uni<List<SnapshotInfo>> list() {
        return ensureSchema()
                .chain(() -> frigg.sql(
                        "SELECT snapshotId, name, ts, eventCount FROM HeimdallSnapshot ORDER BY ts DESC LIMIT 100",
                        null))
                .map(rows -> rows.stream()
                        .map(r -> new SnapshotInfo(
                                (String) r.get("snapshotId"),
                                (String) r.get("name"),
                                toLong(r.get("ts")),
                                toInt(r.get("eventCount"))))
                        .toList());
    }

    private Uni<Void> ensureSchema() {
        if (schemaEnsured) return Uni.createFrom().voidItem();
        return frigg.sql("CREATE VERTEX TYPE HeimdallSnapshot IF NOT EXISTS", null)
                .invoke(__ -> schemaEnsured = true)
                .replaceWithVoid();
    }

    private static long toLong(Object v) { return v instanceof Number n ? n.longValue() : 0L; }
    private static int  toInt(Object v)  { return v instanceof Number n ? n.intValue()  : 0;  }
}
