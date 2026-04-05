package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Full-text search across all Dali vertex types.
 *
 * ArcadeDB does not support UNION ALL — queries run in parallel, merged in Java.
 * LIKE '%q%' is used because built-in FULL_TEXT doesn't tokenise on '_'.
 * For Lucene ENGINE LUCENE, FT indexes are needed on the fields below (see comment).
 *
 * Covered types (12 parallel queries):
 *   DaliTable       table_name     scope=schema_name (1-hop traversal)   score=1.0
 *   DaliColumn      column_name    scope=schema_name (2-hop traversal)   score=0.9
 *   DaliOutputColumn name          scope=session_id                       score=0.9
 *   DaliPackage     package_name   scope=package_name                     score=0.8
 *   DaliRoutine     routine_name   scope=package_name (1-hop traversal)  score=0.8
 *   DaliParameter   param_name     scope=session_id    score=0.75
 *   DaliVariable    var_name       scope=session_id    score=0.75
 *   DaliStatement   stmt_geoid     scope=package_name (2-hop) or session_id  score=0.6
 *   DaliSchema      schema_name    scope=db_name       score=0.7
 *   DaliDatabase    db_name        scope=db_name       score=0.7
 *   DaliApplication app_name       scope=''            score=0.7
 *   DaliSession     file_path      scope=db_name       score=0.5
 *
 * FT indexes missing (needed for future Lucene ENGINE LUCENE migration):
 *   DaliApplication(app_name), DaliOutputColumn(name),
 *   DaliSession(file_path),    DaliStatement(stmt_geoid)
 */
@ApplicationScoped
public class SearchService {

    @Inject
    ArcadeGateway arcade;

    @SuppressWarnings("unchecked")
    public Uni<List<SearchResult>> search(String query, int limit) {
        String like = "%" + esc(query) + "%";
        int n = Math.max(limit / 2, 10);

        // Scope values are used by the frontend for navigation:
        //   DaliTable/DaliColumn  → scope = schema_name  → jumpTo L2 "schema-<scope>"
        //   DaliRoutine/DaliStatement/DaliPackage → scope = package_name → jumpTo L2 "pkg-<scope>"
        //   Others                → scope used for display only
        List<Uni<List<Map<String, Object>>>> unis = List.of(
            // Tables: scope = schema_name via 1-hop CONTAINS_TABLE traversal
            q("SELECT @rid AS rid, @type AS type, table_name AS label, in('CONTAINS_TABLE')[0].schema_name AS scope, 1.0 AS score FROM DaliTable WHERE table_name LIKE '%s' LIMIT %d", like, n),
            // Columns: scope = schema_name via 2-hop HAS_COLUMN→CONTAINS_TABLE traversal
            q("SELECT @rid AS rid, @type AS type, column_name AS label, in('HAS_COLUMN')[0].in('CONTAINS_TABLE')[0].schema_name AS scope, 0.9 AS score FROM DaliColumn WHERE column_name LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, name           AS label, session_id   AS scope, 0.9  AS score FROM DaliOutputColumn WHERE name           LIKE '%s' LIMIT %d", like, n),
            // Package: scope = package_name (same as label — used as key for pkg-<name> scope)
            q("SELECT @rid AS rid, @type AS type, package_name   AS label, package_name AS scope, 0.8  AS score FROM DaliPackage      WHERE package_name   LIKE '%s' LIMIT %d", like, n),
            // Routine: scope = package_name via 1-hop CONTAINS_ROUTINE traversal
            q("SELECT @rid AS rid, @type AS type, routine_name AS label, in('CONTAINS_ROUTINE')[0].package_name AS scope, 0.8 AS score FROM DaliRoutine WHERE routine_name LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, param_name     AS label, session_id   AS scope, 0.75 AS score FROM DaliParameter    WHERE param_name     LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, var_name       AS label, session_id   AS scope, 0.75 AS score FROM DaliVariable     WHERE var_name       LIKE '%s' LIMIT %d", like, n),
            // Statement: scope = package_name via 2-hop CONTAINS_STMT→CONTAINS_ROUTINE traversal; fallback session_id
            q("SELECT @rid AS rid, @type AS type, stmt_geoid AS label, coalesce(in('CONTAINS_STMT')[0].in('CONTAINS_ROUTINE')[0].package_name, session_id, '') AS scope, 0.6 AS score FROM DaliStatement WHERE stmt_geoid LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, schema_name    AS label, db_name      AS scope, 0.7  AS score FROM DaliSchema       WHERE schema_name    LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, db_name        AS label, db_name      AS scope, 0.7  AS score FROM DaliDatabase     WHERE db_name        LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, app_name       AS label, ''           AS scope, 0.7  AS score FROM DaliApplication  WHERE app_name       LIKE '%s' LIMIT %d", like, n),
            q("SELECT @rid AS rid, @type AS type, file_path      AS label, db_name      AS scope, 0.5  AS score FROM DaliSession      WHERE file_path      LIKE '%s' LIMIT %d", like, n)
        );

        return Uni.combine().all()
            .unis(unis)
            .combinedWith(results -> {
                List<SearchResult> merged = new ArrayList<>();
                for (Object raw : results) {
                    for (var row : (List<Map<String, Object>>) raw)
                        merged.add(toResult(row));
                }
                merged.sort((a, b) -> Double.compare(b.score(), a.score()));
                return merged.subList(0, Math.min(limit, merged.size()));
            });
    }

    private Uni<List<Map<String, Object>>> q(String template, String like, int n) {
        return arcade.sql(String.format(template, like, n));
    }

    private static SearchResult toResult(Map<String, Object> row) {
        Object scoreRaw = row.get("score");
        double score = (scoreRaw instanceof Number num) ? num.doubleValue() : 0.0;
        return new SearchResult(
            str(row, "rid"),
            str(row, "type"),
            str(row, "label"),
            str(row, "scope"),
            score
        );
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : "";
    }

    private static String esc(String input) {
        return input.replace("'", "''");
    }
}
