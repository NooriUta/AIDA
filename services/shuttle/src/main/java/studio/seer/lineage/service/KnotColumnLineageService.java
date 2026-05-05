package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.*;
import studio.seer.tenantrouting.YggLineageRegistry;
import studio.seer.lineage.security.SeerIdentity;

import java.util.*;

/**
 * Column-lineage analytics extracted from KnotService to keep both files under the LOC limit.
 *
 * Handles:
 *   - knotTableDetail   — lazy column detail (PK/FK/type/default + SQL snippet)
 *   - knotTableRoutines — which routines read/write a given table
 *   - knotColumnStatements — which statements reference a given column
 *   - loadOutputColumns  — bulk output-column loader for knotReport
 *   - loadAffectedColumns — bulk affected-column loader for knotReport
 */
@ApplicationScoped
public class KnotColumnLineageService {

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;
    @Inject KnotBulkLoaders    bulkLoaders;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── Table detail (lazy) ───────────────────────────────────────────────────

    public Uni<KnotTableDetail> knotTableDetail(String sessionId, String tableGeoid) {
        Map<String, Object> params   = Map.of("sid", sessionId, "tg", tableGeoid);
        Map<String, Object> tgParams = Map.of("tg", tableGeoid);

        // Query 1: columns (canonical — no session scope needed)
        String sqlCols = """
            SELECT @rid AS id, column_name,
                   coalesce(data_type,'') AS data_type,
                   coalesce(ordinal_position,0) AS ord_pos,
                   coalesce(alias,'') AS alias,
                   coalesce(is_required, false) AS is_required,
                   coalesce(is_pk, false) AS is_pk,
                   coalesce(is_fk, false) AS is_fk,
                   coalesce(fk_ref_table,'') AS fk_ref_table,
                   coalesce(default_value,'') AS default_value,
                   coalesce(data_source,'reconstructed') AS col_ds
            FROM DaliColumn
            WHERE table_geoid = :tg
            ORDER BY ord_pos, column_name
            LIMIT 500
            """;

        // Query 2: table data_source
        String sqlTable = """
            SELECT coalesce(data_source,'reconstructed') AS data_source
            FROM DaliTable
            WHERE table_geoid = :tg
            LIMIT 1
            """;

        // Query 3: find one stmt_geoid that references this table in the session (for snippet)
        // Sprint 1.2 inversion: READS_FROM is now Table→Stmt, WRITES_TO remains Stmt→Table.
        // Use undirected edges (-[]-) to match either direction.
        String cypherStmt = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:READS_FROM|WRITES_TO]-(t:DaliTable {table_geoid: $tg})
            RETURN stmt.stmt_geoid AS stmtGeoid
            LIMIT 1
            """;

        return Uni.combine().all()
            .unis(
                arcade.sqlIn(lineageDb(), sqlCols,  tgParams).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlTable, tgParams).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherStmt, params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .chain(t -> {
                List<Map<String, Object>> colRows  = t.getItem1();
                List<Map<String, Object>> tblRows  = t.getItem2();
                List<Map<String, Object>> stmtRows = t.getItem3();

                String dataSource = tblRows.isEmpty()
                    ? "reconstructed"
                    : str(tblRows.get(0), "data_source");

                List<KnotColumn> cols = colRows.stream()
                    .map(r -> new KnotColumn(
                        str(r, "id"),
                        str(r, "column_name"),
                        str(r, "data_type"),
                        num(r, "ord_pos"),
                        0,                          // atomRefCount — not loaded in detail
                        str(r, "alias"),
                        bool(r, "is_required"),
                        bool(r, "is_pk"),
                        bool(r, "is_fk"),
                        str(r, "fk_ref_table"),
                        str(r, "default_value"),
                        str(r, "col_ds")
                    ))
                    .toList();

                if (stmtRows.isEmpty()) {
                    return Uni.createFrom().item(
                        new KnotTableDetail(tableGeoid, dataSource, cols, ""));
                }

                String stmtGeoid = str(stmtRows.get(0), "stmtGeoid");
                String sqlSnip = """
                    SELECT snippet
                    FROM DaliSnippet
                    WHERE session_id = :sid AND stmt_geoid = :sg
                    LIMIT 1
                    """;
                return arcade.sqlIn(lineageDb(), sqlSnip, Map.of("sid", sessionId, "sg", stmtGeoid))
                    .onFailure().recoverWithItem(List.of())
                    .map(snipRows -> {
                        String snippet = snipRows.isEmpty()
                            ? ""
                            : str(snipRows.get(0), "snippet");
                        return new KnotTableDetail(tableGeoid, dataSource, cols, snippet);
                    });
            });
    }

    // ── Table usage analytics ─────────────────────────────────────────────────

    public Uni<List<KnotTableUsage>> knotTableRoutines(String tableRid) {
        if (tableRid == null || tableRid.isBlank()) {
            return Uni.createFrom().item(List.of());
        }
        Map<String, Object> params = Map.of("rid", tableRid);
        String cypher = """
            MATCH (stmt:DaliStatement)-[r:READS_FROM|WRITES_TO]-(t:DaliTable)
            WHERE id(t) = $rid
            MATCH (ro:DaliRoutine)-[:CONTAINS_STMT]->(stmt)
            RETURN DISTINCT
              coalesce(ro.routine_geoid, '') AS routineGeoid,
              coalesce(ro.routine_name,  '') AS routineName,
              type(r)                        AS edgeType,
              coalesce(stmt.stmt_geoid, '')  AS stmtGeoid,
              coalesce(stmt.statement_type, stmt.stmt_type, '') AS stmtType
            ORDER BY routineName, edgeType
            LIMIT 100
            """;
        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotTableUsage(
                    str(r, "routineGeoid"),
                    str(r, "routineName"),
                    str(r, "edgeType"),
                    str(r, "stmtGeoid"),
                    str(r, "stmtType")
                ))
                .toList()
            );
    }

    public Uni<List<KnotColumnUsage>> knotColumnStatements(String columnGeoid) {
        if (columnGeoid == null || columnGeoid.isBlank()) {
            return Uni.createFrom().item(List.of());
        }
        Map<String, Object> params = Map.of("cg", columnGeoid);
        String cypher = """
            MATCH (a:DaliAtom)-[:IN_STATEMENT]->(stmt:DaliStatement)
            WHERE a.ref_geoid = $cg OR a.ref = $cg
            MATCH (ro:DaliRoutine)-[:CONTAINS_STMT]->(stmt)
            RETURN DISTINCT
              coalesce(stmt.stmt_geoid, '')  AS stmtGeoid,
              coalesce(stmt.statement_type, stmt.stmt_type, '') AS stmtType,
              coalesce(ro.routine_name,  '') AS routineName,
              coalesce(ro.routine_geoid, '') AS routineGeoid,
              coalesce(a.atom_type, '')      AS atomType
            ORDER BY routineName, stmtType
            LIMIT 50
            """;
        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotColumnUsage(
                    str(r, "stmtGeoid"),
                    str(r, "stmtType"),
                    str(r, "routineName"),
                    str(r, "routineGeoid"),
                    str(r, "atomType")
                ))
                .toList()
            );
    }

    // ── Bulk loaders (delegated to KnotBulkLoaders) ──────────────────────────

    Uni<List<KnotOutputColumn>>   loadOutputColumns(Map<String, Object> p)  { return bulkLoaders.loadOutputColumns(p);  }
    Uni<List<KnotAffectedColumn>> loadAffectedColumns(Map<String, Object> p){ return bulkLoaders.loadAffectedColumns(p);}
    Uni<KnotParamVars>            loadParamsAndVars(Map<String, Object> p)   { return bulkLoaders.loadParamsAndVars(p);  }
    Uni<List<KnotSnippet>>        loadSnippets(Map<String, Object> p)        { return bulkLoaders.loadSnippets(p);       }
    Uni<List<KnotAtom>>           loadAtoms(Map<String, Object> p)           { return bulkLoaders.loadAtoms(p);          }
    Uni<List<KnotCall>>           loadCalls(Map<String, Object> p)           { return bulkLoaders.loadCalls(p);          }

    // G4: source_file-filtered overloads — delegate to KnotBulkLoaders with sourceFile
    Uni<List<KnotOutputColumn>>   loadOutputColumns(Map<String, Object> p, String sf)  { return bulkLoaders.loadOutputColumns(p, sf);  }
    Uni<List<KnotAffectedColumn>> loadAffectedColumns(Map<String, Object> p, String sf){ return bulkLoaders.loadAffectedColumns(p, sf);}
    Uni<KnotParamVars>            loadParamsAndVars(Map<String, Object> p, String sf)   { return bulkLoaders.loadParamsAndVars(p, sf);  }
    Uni<List<KnotSnippet>>        loadSnippets(Map<String, Object> p, String sf)        { return bulkLoaders.loadSnippets(p, sf);       }
    Uni<List<KnotAtom>>           loadAtoms(Map<String, Object> p, String sf)           { return bulkLoaders.loadAtoms(p, sf);          }
    Uni<List<KnotCall>>           loadCalls(Map<String, Object> p, String sf)           { return bulkLoaders.loadCalls(p, sf);          }

    // ── Statement extras (descendants + atom stats, lazy from Inspector) ──────
    //
    // Powers the LOOM Inspector "Дополнительно" tab. Returns:
    //   * all recursive CHILD_OF descendants (sub-queries, CTEs, inline views)
    //   * DaliAtom counts grouped by parent_context for the root stmt
    //
    // Accepts either an ArcadeDB @rid ("#25:8333") — as passed by the LOOM
    // Inspector which uses RIDs as React Flow node ids — or a stmt_geoid
    // string (as the standalone KNOT page would pass). Mirrors knotSnippet's
    // dual-input pattern.

    public Uni<StatementExtras> knotStatementExtras(String idOrGeoid) {
        if (idOrGeoid == null || idOrGeoid.isBlank())
            return Uni.createFrom().item(new StatementExtras(List.of(), List.of(), 0, List.of()));

        boolean isRid = idOrGeoid.startsWith("#");

        // Descendants via TRAVERSE in('CHILD_OF') — matches the way Hound
        // stores parent/child ("child -[:CHILD_OF]-> parent", so in('CHILD_OF')
        // on the parent yields its children). MAXDEPTH 30 covers the deepest
        // nested CTE/subquery trees observed in the corpus with margin.
        String descendantSql = isRid
            ? """
                SELECT @rid as rid, stmt_geoid, type as stmtType, parent_statement as parentStmtGeoid
                FROM (
                    TRAVERSE in('CHILD_OF')
                    FROM (SELECT FROM DaliStatement WHERE @rid = :rid)
                    MAXDEPTH 30
                )
                WHERE @rid <> :rid
                ORDER BY parent_statement, stmt_geoid
                LIMIT 500
                """
            : """
                SELECT @rid as rid, stmt_geoid, type as stmtType, parent_statement as parentStmtGeoid
                FROM (
                    TRAVERSE in('CHILD_OF')
                    FROM (SELECT FROM DaliStatement WHERE stmt_geoid = :geoid)
                    MAXDEPTH 30
                )
                WHERE stmt_geoid <> :geoid
                ORDER BY parent_statement, stmt_geoid
                LIMIT 500
                """;

        // Atom breakdown — scoped to the root statement's stmt_geoid.
        // Subquery in the WHERE uses IN (SELECT …) per the scalar-vs-set
        // lesson learned in the knotSnippet fix (commit a7b810d).
        String atomSql = isRid
            ? """
                SELECT parent_context as ctx, count(*) as cnt
                FROM DaliAtom
                WHERE statement_geoid IN (SELECT stmt_geoid FROM DaliStatement WHERE @rid = :rid)
                GROUP BY parent_context
                """
            : """
                SELECT parent_context as ctx, count(*) as cnt
                FROM DaliAtom
                WHERE statement_geoid = :geoid
                GROUP BY parent_context
                """;

        Map<String, Object> params = isRid
            ? Map.of("rid",   idOrGeoid)
            : Map.of("geoid", idOrGeoid);

        Uni<List<SubqueryInfo>> descendantsUni = arcade.sqlIn(lineageDb(), descendantSql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new SubqueryInfo(
                    str(r, "rid"),
                    str(r, "stmt_geoid"),
                    str(r, "stmtType"),
                    str(r, "parentStmtGeoid")))
                .toList());

        Uni<List<AtomContextCount>> atomsUni = arcade.sqlIn(lineageDb(), atomSql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    String ctx = str(r, "ctx");
                    if (ctx == null || ctx.isBlank()) ctx = "UNKNOWN";
                    Object cntObj = r.get("cnt");
                    int cnt = cntObj instanceof Number n ? n.intValue() : 0;
                    return new AtomContextCount(ctx, cnt);
                })
                .sorted((a, b) -> Integer.compare(b.count(), a.count()))  // DESC
                .toList());

        // Source tables — DIRECT (root READS_FROM) + SUBQUERY (descendant reads,
        // hoisted to root). Two Cypher queries combined via Uni.concat.

        // Use Cypher for both — ArcadeDB SQL TRAVERSE doesn't easily join back to
        // stmt labels. Cypher handles the RID lookup via id() + numeric conversion
        // is fiddly, so we first resolve the stmt_geoid via SQL if input is a RID.

        Uni<String> geoidUni = isRid
            ? arcade.sqlIn(lineageDb(),
                "SELECT stmt_geoid FROM DaliStatement WHERE @rid = :rid LIMIT 1",
                Map.of("rid", idOrGeoid))
                .onFailure().recoverWithItem(List.of())
                .map(rows -> rows.isEmpty() ? null : str(rows.get(0), "stmt_geoid"))
            : Uni.createFrom().item(idOrGeoid);

        Uni<List<SourceTableRef>> sourcesUni = geoidUni.flatMap(rootGeoid -> {
            if (rootGeoid == null) return Uni.createFrom().item(List.<SourceTableRef>of());

            // Direct reads of the root stmt itself — Cypher keeps it clean.
            // id(t) in ArcadeDB Cypher returns the '#cluster:pos' string.
            Uni<List<SourceTableRef>> directUni = arcade.cypherIn(lineageDb(), """
                    MATCH (t:DaliTable)-[:READS_FROM]->(s:DaliStatement {stmt_geoid: $geoid})
                    RETURN DISTINCT id(t)         AS tblRid,
                                    t.table_geoid  AS tableGeoid,
                                    t.table_name   AS tableName,
                                    t.schema_geoid AS schemaGeoid
                    LIMIT 500
                    """, Map.of("geoid", rootGeoid))
                .onFailure().recoverWithItem(List.of())
                .map(rows -> rows.stream()
                    .map(r -> new SourceTableRef(
                        str(r, "tblRid"),
                        str(r, "tableGeoid"),
                        str(r, "tableName"),
                        str(r, "schemaGeoid"),
                        "DIRECT",
                        null))
                    .toList());

            // Hoisted reads — descendant reads a table via CHILD_OF* chain.
            // sub→root via CHILD_OF*1..30 (child→parent direction), then
            // sub-[:READS_FROM]->table. Returns which sub attributed the read.
            Uni<List<SourceTableRef>> hoistedUni = arcade.cypherIn(lineageDb(), """
                    MATCH (sub:DaliStatement)-[:CHILD_OF*1..30]->(root:DaliStatement {stmt_geoid: $geoid})
                    MATCH (t:DaliTable)-[:READS_FROM]->(sub)
                    RETURN DISTINCT id(t)          AS tblRid,
                                    t.table_geoid  AS tableGeoid,
                                    t.table_name   AS tableName,
                                    t.schema_geoid AS schemaGeoid,
                                    sub.stmt_geoid AS subGeoid
                    LIMIT 2000
                    """, Map.of("geoid", rootGeoid))
                .onFailure().recoverWithItem(List.of())
                .map(rows -> rows.stream()
                    .map(r -> new SourceTableRef(
                        str(r, "tblRid"),
                        str(r, "tableGeoid"),
                        str(r, "tableName"),
                        str(r, "schemaGeoid"),
                        "SUBQUERY",
                        str(r, "subGeoid")))
                    .toList());

            return Uni.combine().all().unis(directUni, hoistedUni)
                .asTuple()
                .map(tuple -> {
                    List<SourceTableRef> combined = new ArrayList<>(tuple.getItem1());
                    // Dedup by tableGeoid: if a table already appears as DIRECT,
                    // don't add a SUBQUERY row for it.
                    java.util.Set<String> seen = new java.util.HashSet<>();
                    for (SourceTableRef r : combined) if (r.tableGeoid() != null) seen.add(r.tableGeoid());
                    for (SourceTableRef r : tuple.getItem2()) {
                        if (r.tableGeoid() != null && seen.add(r.tableGeoid())) combined.add(r);
                    }
                    return combined;
                });
        });

        return Uni.combine().all().unis(descendantsUni, atomsUni, sourcesUni)
            .asTuple()
            .map(tuple -> {
                List<SubqueryInfo>     descendants = tuple.getItem1();
                List<AtomContextCount> atoms       = tuple.getItem2();
                List<SourceTableRef>   sources     = tuple.getItem3();
                int total = atoms.stream().mapToInt(AtomContextCount::count).sum();
                return new StatementExtras(descendants, atoms, total, sources);
            });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : "";
    }

    private static int num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }

    private static boolean bool(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Boolean b) return b;
        return false;
    }

}
