package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SearchResult;

import java.util.List;
import java.util.Map;

/**
 * Full-text search across tables, columns, packages, routines, schemas and databases.
 *
 * Indexes are created automatically by HOUND SchemaInitializer v10 (FULL_TEXT = Lucene).
 * Index name convention: `DaliType[field_ft]`
 *
 * Schema v10 fields used:
 *   DaliTable:    table_name,    schema_geoid (scope)
 *   DaliColumn:   column_name,   table_geoid  (scope)
 *   DaliPackage:  package_name,  schema_geoid (scope, via routine inheritance)
 *   DaliRoutine:  routine_name,  schema_geoid (scope)
 *   DaliSchema:    schema_name,   db_name      (scope)
 *   DaliDatabase:  database_name, db_name      (scope)
 *   DaliStatement: stmt_geoid (label/path),    snippet (searched via snippet_ft index)
 */
@ApplicationScoped
public class SearchService {

    @Inject
    ArcadeGateway arcade;

    public Uni<List<SearchResult>> search(String query, int limit) {
        // Lucene prefix query: "cust" → "cust*"  (case-insensitive by default)
        String luceneQ = esc(query) + "*";

        String sql = String.format("""
            SELECT @rid AS rid, @type AS type, table_name    AS label, schema_geoid   AS scope, $score AS score FROM DaliTable    WHERE SEARCH_INDEX('DaliTable[table_name_ft]',       '%s') = true LIMIT %d
            UNION ALL
            SELECT @rid AS rid, @type AS type, column_name   AS label, table_geoid    AS scope, $score AS score FROM DaliColumn   WHERE SEARCH_INDEX('DaliColumn[column_name_ft]',     '%s') = true LIMIT %d
            UNION ALL
            SELECT @rid AS rid, @type AS type, package_name  AS label, schema_geoid   AS scope, $score AS score FROM DaliPackage  WHERE SEARCH_INDEX('DaliPackage[package_name_ft]',   '%s') = true LIMIT %d
            UNION ALL
            SELECT @rid AS rid, @type AS type, routine_name  AS label, schema_geoid   AS scope, $score AS score FROM DaliRoutine  WHERE SEARCH_INDEX('DaliRoutine[routine_name_ft]',   '%s') = true LIMIT %d
            UNION ALL
            SELECT @rid AS rid, @type AS type, schema_name   AS label, db_name        AS scope, $score AS score FROM DaliSchema   WHERE SEARCH_INDEX('DaliSchema[schema_name_ft]',     '%s') = true LIMIT %d
            UNION ALL
            SELECT @rid AS rid, @type AS type, database_name AS label, db_name        AS scope, $score AS score FROM DaliDatabase WHERE SEARCH_INDEX('DaliDatabase[database_name_ft]', '%s') = true LIMIT %d
            UNION ALL
            SELECT @rid AS rid, @type AS type, stmt_geoid    AS label, session_id     AS scope, $score AS score FROM DaliStatement WHERE SEARCH_INDEX('DaliStatement[snippet_ft]',       '%s') = true LIMIT %d
            """,
            luceneQ, limit,
            luceneQ, limit,
            luceneQ, limit,
            luceneQ, limit,
            luceneQ, limit,
            luceneQ, limit,
            luceneQ, limit
        );

        return arcade.sql(sql).map(rows -> rows.stream()
            .map(SearchService::toResult)
            .sorted((a, b) -> Double.compare(b.score(), a.score()))
            .limit(limit)
            .toList()
        );
    }

    private static SearchResult toResult(Map<String, Object> row) {
        Object scoreRaw = row.get("score");
        double score = (scoreRaw instanceof Number n) ? n.doubleValue() : 0.0;
        return new SearchResult(
            str(row, "@rid"),
            str(row, "@type"),
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
