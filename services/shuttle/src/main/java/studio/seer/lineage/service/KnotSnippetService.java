package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.KnotScript;
import studio.seer.lineage.model.KnotSourceFile;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;

import java.util.List;
import java.util.Map;

/**
 * Lazy source-text retrieval for the KNOT report.
 *
 * <p>Extracted from {@link KnotService} (LOC refactor — QG-ARCH-INVARIANTS §2.4).
 * Handles SQL snippet look-up (by RID or stmt_geoid), full parsed-script retrieval,
 * and original source-file retrieval from the per-tenant archive database.
 */
@ApplicationScoped
class KnotSnippetService {

    @Inject ArcadeGateway            arcade;
    @Inject SeerIdentity             identity;
    @Inject YggLineageRegistry       lineageRegistry;
    @Inject YggSourceArchiveRegistry sourceArchiveRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    String sourceArchiveDb() {
        return sourceArchiveRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── Snippet by geoid OR RID (lazy, called from KNOT inspector on demand) ──
    //
    // The KNOT standalone page passes the real stmt_geoid (from the KnotStatement
    // batch query). The LOOM canvas inspector uses ArcadeDB RIDs as React Flow
    // node IDs (see ExploreService projection), so it passes values like "#25:12304".
    // We accept both:
    //   - Input starts with '#' → resolve via the DaliStatement RID → stmt_geoid
    //     → DaliSnippet lookup
    //   - Anything else → direct DaliSnippet lookup by stmt_geoid

    Uni<String> knotSnippet(String idOrGeoid) {
        if (idOrGeoid == null || idOrGeoid.isBlank()) return Uni.createFrom().nullItem();
        boolean isRid = idOrGeoid.startsWith("#");
        // DaliSnippet is a DOCUMENT (large SQL texts — VERTEX promotion rejected).
        //
        // @rid path (v28+): element_rid stores the ArcadeDB @rid of DaliStatement directly,
        //   so lookup is WHERE element_rid = :rid — O(1) via NOTUNIQUE index, no subquery.
        //   (Pre-v28 fallback: IN (SELECT stmt_geoid FROM DaliStatement WHERE @rid = :rid) —
        //   ArcadeDB SQL subqueries return a result set; `IN (SELECT …)` is the safe form.)
        //
        // stmt_geoid path: NOTUNIQUE index on stmt_geoid → O(1) lookup, no Session hop.
        String sql = isRid
            ? """
                SELECT snippet
                FROM DaliSnippet
                WHERE element_rid = :rid
                   OR stmt_geoid IN (SELECT stmt_geoid FROM DaliStatement WHERE @rid = :rid)
                LIMIT 1
                """
            : """
                SELECT snippet
                FROM DaliSnippet
                WHERE stmt_geoid = :geoid
                LIMIT 1
                """;
        Map<String, Object> params = isRid
            ? Map.of("rid",   idOrGeoid)
            : Map.of("geoid", idOrGeoid);
        return arcade.sqlIn(lineageDb(), sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.isEmpty() ? null : str(rows.get(0), "snippet"))
            .map(s -> (s == null || s.isBlank()) ? null : s);
    }

    // ── Full source file (lazy, called from KNOT "Исходник" tab) ─────────────
    //
    // Returns the full text of the parsed source file for a session.
    // DaliSnippetScript stores one document per Hound parse run with the
    // complete raw PL/SQL text (up to several MB for large packages).
    // Accessed via session_id SQL lookup (no graph edge to DaliSession).

    Uni<KnotScript> knotScript(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return Uni.createFrom().nullItem();
        String sql = """
            SELECT file_path, script, line_count, char_count
            FROM DaliSnippetScript
            WHERE session_id = :sid
            LIMIT 1
            """;
        return arcade.sqlIn(lineageDb(), sql, Map.of("sid", sessionId))
                .onFailure().recoverWithItem(List.of())
                .map(rows -> {
                    if (rows.isEmpty()) return null;
                    var r = rows.get(0);
                    return new KnotScript(
                            str(r, "file_path"),
                            str(r, "script"),
                            num(r, "line_count"),
                            num(r, "char_count")
                    );
                });
    }

    // ── Full source file from source archive (hound_src_{tenant}) ────────────
    //
    // Returns the complete, unprocessed SQL document stored by Dali during parse.
    // Unlike DaliSnippetScript (fragments in lineage DB), DaliSourceFile contains
    // the original full file text. Accessed by session_id in hound_src_{tenant}.

    Uni<KnotSourceFile> knotSourceFile(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return Uni.createFrom().nullItem();

        // Step 1: resolve file_path from lineage DB (DaliSession.session_id → file_path).
        // Step 2: look up DaliSourceFile by file_path in archive DB (hound_src_{tenant}).
        // This bridges Hound session_id ("session-{ts}") to Dali archive (stored by file_path).
        String lineageSql = "SELECT file_path FROM DaliSession WHERE session_id = :sid LIMIT 1";

        return arcade.sqlIn(lineageDb(), lineageSql, Map.of("sid", sessionId))
                .onFailure().recoverWithItem(List.of())
                .flatMap(rows -> {
                    if (rows.isEmpty()) return Uni.createFrom().nullItem();
                    String fp = str(rows.get(0), "file_path");
                    if (fp == null || fp.isBlank()) return Uni.createFrom().nullItem();

                    String archiveSql = """
                        SELECT session_id, file_path, sql_text, size_bytes, sql_text_hash
                        FROM DaliSourceFile
                        WHERE file_path = :fp
                        ORDER BY @rid DESC
                        LIMIT 1
                        """;
                    return arcade.sqlIn(sourceArchiveDb(), archiveSql, Map.of("fp", fp));
                })
                .onFailure().recoverWithItem(List.of())
                .map(rows -> {
                    if (rows.isEmpty()) return null;
                    var r = rows.get(0);
                    return new KnotSourceFile(
                            str(r, "session_id"),
                            str(r, "file_path"),
                            str(r, "sql_text"),
                            r.get("size_bytes") != null ? ((Number) r.get("size_bytes")).longValue() : 0L,
                            str(r, "sql_text_hash")
                    );
                });
    }

    // ── Row helpers ───────────────────────────────────────────────────────────

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : "";
    }

    private static int num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }
}
