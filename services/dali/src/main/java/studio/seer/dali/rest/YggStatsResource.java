package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.storage.FriggCommand;
import studio.seer.dali.storage.FriggResponse;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YGG graph stats endpoint — exposes aggregate counts from the ArcadeDB "hound" database.
 *
 * <pre>
 * GET /api/stats  →  200 OK + YggStats JSON
 * </pre>
 */
@Path("/api/stats")
@Produces(MediaType.APPLICATION_JSON)
public class YggStatsResource {

    private static final Logger log = LoggerFactory.getLogger(YggStatsResource.class);

    @Inject @RestClient YggClient yggClient;

    @ConfigProperty(name = "ygg.db",       defaultValue = "hound")        String db;
    @ConfigProperty(name = "ygg.user",     defaultValue = "root")         String user;
    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata") String password;

    @GET
    public Response get() {
        try {
            String auth = "Basic " + Base64.getEncoder()
                    .encodeToString((user + ":" + password).getBytes());

            long tables     = count(auth, "DaliTable");
            long columns    = count(auth, "DaliColumn");
            long sessions   = count(auth, "DaliSession");
            long statements = count(auth, "DaliStatement");
            long routines   = count(auth, "DaliRoutine");

            Map<String, Long> atomsByStatus = atomCounts(auth);

            // resolved   = status in ('Обработано', 'constant')
            // unresolved = status is null OR status not in ('Обработано','constant') OR statement_geoid='unattached'
            long atomsResolved   = countAtoms(auth,
                    "status in ('Обработано', 'constant')");
            long atomsUnresolved = countAtoms(auth,
                    "status is null OR status NOT IN ['Обработано', 'constant'] OR statement_geoid = 'unattached'");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tables",           tables);
            result.put("columns",          columns);
            result.put("sessions",         sessions);
            result.put("statements",       statements);
            result.put("routines",         routines);
            result.put("atomsTotal",       atomsByStatus.values().stream().mapToLong(Long::longValue).sum());
            result.put("atomsResolved",    atomsResolved);
            result.put("atomsConstant",    atomsByStatus.getOrDefault("constant",   0L));
            result.put("atomsUnresolved",  atomsUnresolved);
            result.put("atomsPending",     atomsByStatus.getOrDefault("pending",    0L));

            return Response.ok(result).build();
        } catch (Exception e) {
            log.warn("YGG stats failed: {}", e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "YGG unavailable: " + e.getMessage()))
                    .build();
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────────

    /** Counts DaliAtom rows matching an explicit WHERE clause. */
    private long countAtoms(String auth, String where) {
        try {
            List<Map<String, Object>> rows = sql(auth,
                    "SELECT count(*) as cnt FROM `DaliAtom` WHERE " + where);
            if (rows == null || rows.isEmpty()) return 0L;
            Object v = rows.get(0).get("cnt");
            return v instanceof Number n ? n.longValue() : 0L;
        } catch (Exception e) {
            log.debug("countAtoms({}) failed: {}", where, e.getMessage());
            return 0L;
        }
    }

    private long count(String auth, String type) {
        try {
            List<Map<String, Object>> rows = sql(auth,
                    "SELECT count(*) as cnt FROM `" + type + "`");
            if (rows == null || rows.isEmpty()) return 0L;
            Object v = rows.get(0).get("cnt");
            return v instanceof Number n ? n.longValue() : 0L;
        } catch (Exception e) {
            log.debug("count({}) failed: {}", type, e.getMessage());
            return 0L;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> atomCounts(String auth) {
        Map<String, Long> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> rows = sql(auth,
                    "SELECT status, count(*) as cnt FROM `DaliAtom` GROUP BY status");
            if (rows == null) return result;
            for (Map<String, Object> row : rows) {
                Object statusObj = row.get("status");
                Object cntObj    = row.get("cnt");
                String status = statusObj != null ? statusObj.toString() : "pending";
                long   cnt    = cntObj instanceof Number n ? n.longValue() : 0L;
                result.put(status, cnt);
            }
        } catch (Exception e) {
            log.debug("atomCounts failed: {}", e.getMessage());
        }
        return result;
    }

    private List<Map<String, Object>> sql(String auth, String query) {
        FriggResponse resp = yggClient.command(db, auth, new FriggCommand("sql", query, null))
                .await().atMost(java.time.Duration.ofSeconds(5));
        return resp != null ? resp.result() : List.of();
    }
}
