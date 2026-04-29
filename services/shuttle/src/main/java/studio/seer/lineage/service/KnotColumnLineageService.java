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
        String cypherStmt = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:READS_FROM|WRITES_TO]->(t:DaliTable {table_geoid: $tg})
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
            MATCH (stmt:DaliStatement)-[r:READS_FROM|WRITES_TO]->(t:DaliTable)
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

    // ── Output columns ────────────────────────────────────────────────────────

    Uni<List<KnotOutputColumn>> loadOutputColumns(Map<String, Object> params) {
        // Start from DaliStatement(session_id) index — avoids 3-hop traversal prefix.
        String cypher = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:HAS_OUTPUT_COL]->(oc:DaliOutputColumn)
            OPTIONAL MATCH (a:DaliAtom)-[:ATOM_PRODUCES]->(oc)
            RETURN stmt.stmt_geoid             AS stmtGeoid,
                   coalesce(oc.col_order, 0)   AS colOrder,
                   coalesce(oc.name, '')        AS name,
                   coalesce(oc.expression, '')  AS expression,
                   coalesce(oc.alias, '')       AS alias,
                   coalesce(oc.source_type, '') AS sourceType,
                   coalesce(oc.table_ref, '')   AS tableRef,
                   collect({
                       text:   a.atom_text,
                       col:    a.column_name,
                       tbl:    a.table_name,
                       status: a.status
                   })                          AS atoms
            ORDER BY stmtGeoid, colOrder
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    // collect() with OPTIONAL MATCH returns [{text:null,...}] when no atom — filter out
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rawAtoms =
                        (List<Map<String, Object>>) r.getOrDefault("atoms", List.of());

                    List<KnotOutputColumnAtom> atoms = rawAtoms.stream()
                        .filter(a -> a.get("text") != null)
                        .map(a -> new KnotOutputColumnAtom(
                            str(a, "text"),
                            str(a, "col"),
                            str(a, "tbl"),
                            str(a, "status")
                        ))
                        .toList();

                    return new KnotOutputColumn(
                        str(r, "stmtGeoid"),
                        str(r, "name"),
                        str(r, "expression"),
                        str(r, "alias"),
                        num(r, "colOrder"),
                        str(r, "sourceType"),
                        str(r, "tableRef"),
                        atoms
                    );
                })
                .toList()
            );
    }

    // ── Affected columns ──────────────────────────────────────────────────────

    Uni<List<KnotAffectedColumn>> loadAffectedColumns(Map<String, Object> params) {
        // Start from DaliStatement(session_id) index — avoids 3-hop traversal prefix.
        String cypher = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:HAS_AFFECTED_COL]->(col:DaliAffectedColumn)
            RETURN stmt.stmt_geoid                            AS stmtGeoid,
                   coalesce(col.column_name, '')              AS columnName,
                   coalesce(col.table_name, '')               AS tableName,
                   coalesce(col.position, 0)                  AS position
            ORDER BY stmtGeoid, position
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotAffectedColumn(
                    str(r, "stmtGeoid"),
                    str(r, "columnName"),
                    str(r, "tableName"),
                    num(r, "position")
                ))
                .toList()
            );
    }

    // ── Parameters, Variables & Routines (bulk loader for knotReport) ────────

    /** Internal holder for routines + params + vars returned as a single Uni. */
    record KnotParamVars(List<KnotRoutine> routines, List<KnotParameter> parameters, List<KnotVariable> variables) {}

    Uni<KnotParamVars> loadParamsAndVars(Map<String, Object> params) {
        // All three use DaliRoutine(session_id) NOTUNIQUE index directly.
        // Was: 2-hop traversal Session→BELONGS_TO_SESSION→Routine.
        String cypherRoutines = """
            MATCH (r:DaliRoutine {session_id: $sid})
            RETURN r.routine_name                AS routineName,
                   coalesce(r.routine_type, '')  AS routineType,
                   coalesce(r.package_geoid, '') AS packageGeoid
            ORDER BY routineName
            LIMIT 5000
            """;

        String cypherParams = """
            MATCH (r:DaliRoutine {session_id: $sid})-[:HAS_PARAMETER]->(p:DaliParameter)
            RETURN r.routine_name                   AS routineName,
                   p.param_name                     AS paramName,
                   coalesce(p.data_type, '')         AS dataType,
                   coalesce(p.direction, '')         AS direction
            ORDER BY routineName, paramName
            LIMIT 5000
            """;

        String cypherVars = """
            MATCH (r:DaliRoutine {session_id: $sid})-[:HAS_VARIABLE]->(v:DaliVariable)
            RETURN r.routine_name                   AS routineName,
                   v.var_name                       AS varName,
                   coalesce(v.data_type, '')         AS dataType
            ORDER BY routineName, varName
            LIMIT 5000
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypherIn(lineageDb(), cypherRoutines, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherParams,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherVars,     params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> {
                List<KnotRoutine> rList = t.getItem1().stream()
                    .map(r -> new KnotRoutine(
                        str(r, "routineName"),
                        str(r, "routineType"),
                        str(r, "packageGeoid")
                    ))
                    .toList();

                List<KnotParameter> pList = t.getItem2().stream()
                    .map(r -> new KnotParameter(
                        str(r, "routineName"),
                        str(r, "paramName"),
                        str(r, "dataType"),
                        str(r, "direction")
                    ))
                    .toList();

                List<KnotVariable> vList = t.getItem3().stream()
                    .map(r -> new KnotVariable(
                        str(r, "routineName"),
                        str(r, "varName"),
                        str(r, "dataType")
                    ))
                    .toList();

                return new KnotParamVars(rList, pList, vList);
            });
    }

    // ── Snippets (bulk loader for knotReport) ────────────────────────────────

    Uni<List<KnotSnippet>> loadSnippets(Map<String, Object> params) {
        // DaliSnippet has no graph edges — DOCUMENT type joined by session_id + stmt_geoid.
        // Bulk load for knotReport: fetch all snippets for a session via session_id NOTUNIQUE index.
        //
        // BUG-SS-042: LIMIT 2000 was too low for large packages (e.g. >100 routines × 20+ stmts).
        // Snippets are small text records; 50 000 rows load in <100ms on typical hardware.
        // Alphabetical ORDER BY stmt_geoid means CURSORs (sorted after SELECT/INSERT) were cut off.
        String sql = """
            SELECT stmt_geoid, snippet
            FROM DaliSnippet
            WHERE session_id = :sid
            ORDER BY stmt_geoid
            LIMIT 50000
            """;

        return arcade.sqlIn(lineageDb(), sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotSnippet(str(r, "stmt_geoid"), str(r, "snippet")))
                .filter(s -> s.stmtGeoid() != null && !s.stmtGeoid().isBlank()
                          && s.snippet()   != null && !s.snippet().isBlank())
                .toList()
            );
    }

    // ── Atoms (bulk loader for knotReport) ───────────────────────────────────

    Uni<List<KnotAtom>> loadAtoms(Map<String, Object> params) {
        // Cypher instead of SQL — each graph edge traversal is an explicit OPTIONAL MATCH,
        // which is safe and portable. ArcadeDB SQL out()[0].field syntax is fragile
        // (returns null or wrong type when the edge list is empty).
        // Start from DaliAtom(session_id) NOTUNIQUE index — avoids 3-hop traversal prefix.
        // Reverse edge (a)<-[:HAS_ATOM]-(stmt) is a single RID lookup after index hit.
        String cypher = """
            MATCH (a:DaliAtom {session_id: $sid})<-[:HAS_ATOM]-(stmt:DaliStatement)
            OPTIONAL MATCH (a)-[:ATOM_PRODUCES]->(oc:DaliOutputColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_OUTPUT_COL]->(roc:DaliOutputColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_STMT]->(rs:DaliStatement)
            OPTIONAL MATCH (a)-[:ATOM_REF_COLUMN]->(rc:DaliColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_TABLE]->(rt:DaliTable)
            RETURN stmt.stmt_geoid                             AS stmtGeoid,
                   coalesce(a.atom_text, '')                   AS atomText,
                   coalesce(a.column_name, '')                 AS columnName,
                   coalesce(a.table_geoid, '')                 AS tableGeoid,
                   coalesce(a.table_name, '')                  AS tableName,
                   coalesce(a.status, '')                      AS status,
                   coalesce(a.atom_context, '')                AS atomContext,
                   coalesce(a.parent_context, '')              AS parentContext,
                   a.output_column_sequence                    AS outputColumnSequence,
                   coalesce(oc.name, '')                       AS outputColName,
                   coalesce(roc.output_col_name, roc.name, '') AS refSourceName,
                   coalesce(rs.stmt_geoid, '')                 AS refStmtGeoid,
                   coalesce(rc.column_name, '')                AS refColEdge,
                   coalesce(rt.table_name, '')                 AS refTblEdge,
                   coalesce(rt.table_geoid, '')                AS refTblGeoidEdge,
                   coalesce(a.is_column_reference, false)      AS isColumnRef,
                   coalesce(a.is_function_call, false)         AS isFuncCall,
                   coalesce(a.is_constant, false)              AS isConst,
                   coalesce(a.s_complex, false)                AS isComplex,
                   coalesce(a.is_routine_param, false)         AS isRoutineParam,
                   coalesce(a.is_routine_var, false)           AS isRoutineVar,
                   a.nested_atoms_count                        AS nestedAtomsCount
            LIMIT 5000
            """; // ORDER BY removed — Java re-sorts by stmtGeoid+atomLine+atomPos below

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    String atomText = str(r, "atomText");
                    return new KnotAtom(
                        str(r, "stmtGeoid"),
                        atomText,
                        str(r, "columnName"),
                        str(r, "tableGeoid"),
                        str(r, "tableName"),
                        str(r, "status"),
                        str(r, "atomContext"),
                        str(r, "parentContext"),
                        intOrNull(r, "outputColumnSequence"),
                        str(r, "outputColName"),
                        str(r, "refSourceName"),
                        str(r, "refStmtGeoid"),
                        str(r, "refColEdge"),
                        str(r, "refTblEdge"),
                        str(r, "refTblGeoidEdge"),
                        bool(r, "isColumnRef"),
                        bool(r, "isFuncCall"),
                        bool(r, "isConst"),
                        bool(r, "isComplex"),
                        bool(r, "isRoutineParam"),
                        bool(r, "isRoutineVar"),
                        intOrNull(r, "nestedAtomsCount"),
                        atomLine(atomText),
                        atomPos(atomText)
                    );
                })
                .sorted(Comparator.comparing(KnotAtom::stmtGeoid)
                    .thenComparingInt(KnotAtom::atomLine)
                    .thenComparingInt(KnotAtom::atomPos))
                .toList()
            );
    }

    // ── Routine calls (bulk loader for knotReport) ────────────────────────────

    Uni<List<KnotCall>> loadCalls(Map<String, Object> params) {
        // CALLS edge: DaliRoutine -[CALLS]-> DaliRoutine (or any vertex)
        // Edge properties: callee_name, line_start; caller identified by caller node
        // Start from DaliRoutine(session_id) index — avoids 2-hop traversal prefix.
        String cypher = """
            MATCH (caller:DaliRoutine {session_id: $sid})-[c:CALLS]->(callee)
            RETURN caller.routine_name                             AS callerName,
                   coalesce(caller.package_geoid, '')             AS callerPackage,
                   coalesce(c.callee_name, callee.routine_name, '') AS calleeName,
                   coalesce(c.line_start, 0)                      AS lineStart
            ORDER BY callerName, lineStart
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotCall(
                    str(r, "callerName"),
                    str(r, "callerPackage"),
                    str(r, "calleeName"),
                    num(r, "lineStart")
                ))
                .toList()
            );
    }

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
                    MATCH (s:DaliStatement {stmt_geoid: $geoid})-[:READS_FROM]->(t:DaliTable)
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
                    MATCH (sub)-[:READS_FROM]->(t:DaliTable)
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

    private static Integer intOrNull(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return null;
    }

    /**
     * Extract line number from atom_text: format is "NAME~line:pos" (e.g. "CODE~78:0").
     * Returns the integer before ':' after the last '~'. Returns 0 if not parseable.
     */
    static int atomLine(String atomText) {
        if (atomText == null) return 0;
        int tilde = atomText.lastIndexOf('~');
        if (tilde < 0) return 0;
        String rest = atomText.substring(tilde + 1);
        int colon = rest.indexOf(':');
        String lineStr = colon >= 0 ? rest.substring(0, colon) : rest;
        try { return Integer.parseInt(lineStr); }
        catch (NumberFormatException ignored) { return 0; }
    }

    /**
     * Extract column position from atom_text: format is "NAME~line:pos" (e.g. "CODE~78:0").
     * Returns the integer after ':' after the last '~'. Returns 0 if not parseable.
     */
    static int atomPos(String atomText) {
        if (atomText == null) return 0;
        int tilde = atomText.lastIndexOf('~');
        if (tilde < 0) return 0;
        String rest = atomText.substring(tilde + 1);
        int colon = rest.indexOf(':');
        if (colon < 0) return 0;
        try { return Integer.parseInt(rest.substring(colon + 1)); }
        catch (NumberFormatException ignored) { return 0; }
    }
}
