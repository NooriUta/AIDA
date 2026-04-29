package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.DaliApplicationDto;
import studio.seer.lineage.model.DaliDatabaseDto;
import studio.seer.lineage.model.DaliSchemaDto;
import studio.seer.tenantrouting.YggLineageRegistry;
import studio.seer.lineage.security.SeerIdentity;

import java.util.*;

/**
 * Application hierarchy: DaliApplication → DaliDatabase → DaliSchema.
 *
 * Variant A: traverse BELONGS_TO_APP + CONTAINS_SCHEMA edges from DaliApplication.
 * Variant B (fallback): group DaliSchema by db_name when no DaliApplication nodes exist.
 */
@ApplicationScoped
public class ApplicationsService {

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    public Uni<List<DaliApplicationDto>> fetchApplicationHierarchy() {
        // Query each DaliSchema with its full App→DB context in one pass.
        // Schemas without a database link are silently skipped (they appear in overview as stub).
        String sql = """
            SELECT
                @rid                                                                   AS schId,
                schema_name                                                            AS schName,
                schema_geoid                                                           AS schGeoid,
                out('CONTAINS_TABLE').size()                                           AS tableCount,
                out('CONTAINS_ROUTINE')[@type = 'DaliRoutine'].size()                  AS routineCount,
                out('CONTAINS_ROUTINE')[@type = 'DaliPackage'].size()                  AS packageCount,
                in('CONTAINS_SCHEMA')[0].@rid                                          AS dbId,
                in('CONTAINS_SCHEMA')[0].db_name                                       AS dbName,
                in('CONTAINS_SCHEMA')[0].db_geoid                                      AS dbGeoid,
                in('CONTAINS_SCHEMA')[0].out('BELONGS_TO_APP')[0].@rid                 AS appId,
                in('CONTAINS_SCHEMA')[0].out('BELONGS_TO_APP')[0].app_geoid            AS appGeoid,
                in('CONTAINS_SCHEMA')[0].out('BELONGS_TO_APP')[0].app_name             AS appName
            FROM DaliSchema
            WHERE in('CONTAINS_SCHEMA') IS NOT NULL
            ORDER BY appName, dbName, schName
            """;

        return arcade.sqlIn(lineageDb(), sql, Map.of()).map(ApplicationsService::buildHierarchy);
    }

    private static List<DaliApplicationDto> buildHierarchy(List<Map<String, Object>> rows) {
        // appId → { appName, appGeoid, dbId → { dbName, dbGeoid, schemas[] } }
        Map<String, AppAcc>   appMap    = new LinkedHashMap<>();
        Map<String, DbAcc>    dbMap     = new LinkedHashMap<>();  // keyed by appId+":"+dbId
        List<DaliSchemaDto>   orphans   = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            String schId    = str(r, "schId");
            String schName  = str(r, "schName");
            String schGeoid = str(r, "schGeoid");
            int tableCount    = num(r, "tableCount");
            int routineCount  = num(r, "routineCount");
            int packageCount  = num(r, "packageCount");

            String dbId    = strOrNull(r, "dbId");
            String dbName  = strOrNull(r, "dbName");
            String dbGeoid = strOrNull(r, "dbGeoid");
            String appId   = strOrNull(r, "appId");
            String appName = strOrNull(r, "appName");
            String appGeoid= strOrNull(r, "appGeoid");

            DaliSchemaDto schema = new DaliSchemaDto(schId, schName, schGeoid,
                    tableCount, routineCount, packageCount);

            if (dbId == null) { orphans.add(schema); continue; }

            String dbKey = (appId != null ? appId : "__orphan__") + ":" + dbId;

            if (appId != null) {
                appMap.computeIfAbsent(appId, k -> new AppAcc(appId, appName, appGeoid));
            }
            DbAcc db = dbMap.computeIfAbsent(dbKey, k -> {
                DbAcc d = new DbAcc(dbId, dbName, dbGeoid);
                AppAcc app = appId != null ? appMap.get(appId) : null;
                if (app != null) app.dbs.put(dbKey, d);
                return d;
            });
            db.schemas.add(schema);
        }

        List<DaliApplicationDto> result = new ArrayList<>();

        // Named applications
        for (AppAcc app : appMap.values()) {
            List<DaliDatabaseDto> dbs = app.dbs.values().stream()
                    .map(d -> new DaliDatabaseDto(d.id, d.name, d.geoid, List.copyOf(d.schemas)))
                    .toList();
            result.add(new DaliApplicationDto(app.id, app.name, app.geoid, dbs));
        }

        // Orphan databases (no application)
        List<DaliDatabaseDto> orphanDbs = dbMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith("__orphan__:"))
                .map(e -> new DaliDatabaseDto(e.getValue().id, e.getValue().name,
                        e.getValue().geoid, List.copyOf(e.getValue().schemas)))
                .toList();
        if (!orphanDbs.isEmpty()) {
            result.add(new DaliApplicationDto(null, null, null, orphanDbs));
        }

        return result;
    }

    // ── Accumulator helpers ───────────────────────────────────────────────────

    private static class AppAcc {
        final String id, name, geoid;
        final Map<String, DbAcc> dbs = new LinkedHashMap<>();
        AppAcc(String id, String name, String geoid) { this.id = id; this.name = name; this.geoid = geoid; }
    }

    private static class DbAcc {
        final String id, name, geoid;
        final List<DaliSchemaDto> schemas = new ArrayList<>();
        DbAcc(String id, String name, String geoid) { this.id = id; this.name = name; this.geoid = geoid; }
    }

    // ── Row helpers ───────────────────────────────────────────────────────────

    static String str(Map<String, Object> r, String k) {
        Object v = r.get(k); return v != null ? v.toString() : "";
    }
    static String strOrNull(Map<String, Object> r, String k) {
        Object v = r.get(k); if (v == null) return null;
        String s = v.toString().strip(); return s.isEmpty() ? null : s;
    }
    static int num(Map<String, Object> r, String k) {
        Object v = r.get(k); return v instanceof Number n ? n.intValue() : 0;
    }
}
