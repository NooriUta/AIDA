package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SchemaNode;

import java.util.List;
import java.util.Map;

/**
 * L1 — aggregated schema overview.
 *
 * Confirmed against hound DB (2026-04-04):
 *   DaliSchema properties: schema_name, schema_geoid, database_geoid
 *   CONTAINS_TABLE  → DaliTable  (active)
 *   CONTAINS_PACKAGE → DaliPackage (edge type exists; count depends on data)
 *   CONTAINS_ROUTINE → DaliRoutine (edge type exists)
 */
@ApplicationScoped
public class OverviewService {

    @Inject
    ArcadeGateway arcade;

    public Uni<List<SchemaNode>> overview() {
        // Traversal chain:
        //   DaliSchema  <-[CONTAINS_SCHEMA]-  DaliDatabase
        //   DaliDatabase properties: db_geoid, db_name
        //   Schemas without CONTAINS_SCHEMA edge → databaseGeoid/databaseName = null
        //   applicationGeoid/applicationName — не заполняем (BELONGS_TO_APP после HOUND-DB-001)
        String sql = """
            SELECT
                @rid                                                 AS rid,
                schema_name,
                out('CONTAINS_TABLE').size()                         AS tableCount,
                in('CONTAINS_SCHEMA')[0].db_geoid                    AS databaseGeoid,
                in('CONTAINS_SCHEMA')[0].db_name                     AS databaseName
            FROM DaliSchema
            ORDER BY schema_name
            """;

        return arcade.sql(sql).map(rows -> rows.stream()
            .map(OverviewService::toSchemaNode)
            .toList()
        );
    }

    private static SchemaNode toSchemaNode(Map<String, Object> row) {
        return new SchemaNode(
            str(row, "rid"),
            str(row, "schema_name"),
            num(row, "tableCount"),
            num(row, "routineCount"),
            num(row, "packageCount"),
            strOrNull(row, "databaseGeoid"),
            strOrNull(row, "databaseName"),
            null,                              // databaseEngine — property absent in DaliDatabase
            strOrNull(row, "applicationGeoid"),
            strOrNull(row, "applicationName")
        );
    }

    static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : "";
    }

    /** Returns null for absent/empty values — used for optional hierarchy fields. */
    static String strOrNull(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        String s = v.toString().strip();
        return s.isEmpty() ? null : s;
    }

    static int num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }
}
