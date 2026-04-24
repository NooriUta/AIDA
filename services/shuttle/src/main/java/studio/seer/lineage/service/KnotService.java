package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.*;
import studio.seer.tenantrouting.YggLineageRegistry;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;
import studio.seer.lineage.security.SeerIdentity;

import java.util.*;

/**
 * KNOT Report service — session-level analytics for parsed Hound data.
 *
 * Confirmed ArcadeDB schema (verified 2026-04-06 against live hound DB, 241 sessions, 561995 atoms):
 *
 * ┌──────────────────┬─────────────────────────┬─────────────────────────────────────────────────────┐
 * │ Entity           │ Fields (populated)      │ Deficiencies found                                  │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliSession      │ session_id, file_path,  │ session_name = null (all rows) → derive from        │
 * │                  │ dialect                 │ file_path; processing_ms = null                     │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliRoutine      │ routine_name,           │ No deficiencies found                               │
 * │                  │ routine_type            │                                                     │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliStatement    │ stmt_geoid              │ stmt_type = null (all rows) → parse from geoid[3]   │
 * │                  │                         │ line_number = null (all rows) → parse from geoid[4] │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliAtom         │ status                  │ atom_type = null (all rows) — type is encoded in    │
 * │                  │                         │ status: 'Обработано'|'unresolved'|'constant'|        │
 * │                  │                         │ 'function_call' (NOT the English values in plan)    │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliTable        │ table_name, table_geoid,│ No direct BELONGS_TO_SESSION edges (count = 0) →   │
 * │                  │ schema_geoid            │ must traverse via Routine→Statement→READS_FROM       │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliColumn       │ column_name             │ data_type = null, position = null (all rows)        │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliPackage      │ (not verified)          │ NOT in BELONGS_TO_SESSION path — edge goes directly │
 * │                  │                         │ Session→Routine, DaliPackage is orphaned here        │
 * └──────────────────┴─────────────────────────┴─────────────────────────────────────────────────────┘
 *
 * Edge topology (verified):
 *   DaliSession   -[BELONGS_TO_SESSION]-> DaliRoutine      (NOT via DaliPackage — was wrong in plan)
 *   DaliRoutine   -[CONTAINS_STMT]->      DaliStatement
 *   DaliStatement -[CHILD_OF]->           DaliStatement    (child→parent; was inverted in original query)
 *   DaliStatement -[READS_FROM]->         DaliTable
 *   DaliStatement -[WRITES_TO]->          DaliTable
 *   DaliStatement -[HAS_ATOM]->           DaliAtom
 *   DaliTable     -[HAS_COLUMN]->         DaliColumn
 * DaliSnippet is a DOCUMENT (not vertex): large SQL texts, VERTEX promotion rejected.
 *   v28+: element_rid = ArcadeDB @rid of owning DaliStatement → O(1) lookup by node id.
 *   Fallback: stmt_geoid NOTUNIQUE index (pre-v28 data or stmt_geoid path).
 *   DaliRoutine   -[HAS_PARAMETER]->      DaliParameter
 *   DaliRoutine   -[HAS_VARIABLE]->       DaliVariable
 *
 * stmt_geoid format: "SCHEMA.PACKAGE:ROUTINE_TYPE:ROUTINE_NAME:STMT_TYPE:LINE[:nested...]"
 *   e.g. "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:152"
 *   Nested: "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:2333:SELECT:2336:ACC_DEAL:2370:SQ:2372"
 */
@ApplicationScoped
public class KnotService {

    @Inject ArcadeGateway           arcade;
    @Inject SeerIdentity            identity;
    @Inject YggLineageRegistry      lineageRegistry;
    @Inject YggSourceArchiveRegistry sourceArchiveRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    String sourceArchiveDb() {
        return sourceArchiveRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── Session list ──────────────────────────────────────────────────────────

    public Uni<List<KnotSession>> knotSessions() {
        String sql = """
            SELECT
                @rid                                              AS id,
                session_id,
                session_name,
                coalesce(dialect, 'plsql')                       AS dialect,
                coalesce(file_path, '')                          AS file_path,
                coalesce(processing_ms, 0)                       AS processing_ms
            FROM DaliSession
            ORDER BY file_path
            """;

        return arcade.sqlIn(lineageDb(), sql, Map.of()).map(rows -> rows.stream()
            .map(r -> {
                String filePath    = str(r, "file_path");
                String sessionName = deriveName(str(r, "session_name"), filePath);
                return new KnotSession(
                    str(r, "id"),
                    str(r, "session_id"),
                    sessionName,
                    str(r, "dialect"),
                    filePath,
                    num(r, "processing_ms"),
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0
                );
            })
            .toList()
        );
    }

    // ── Full report ───────────────────────────────────────────────────────────

    public Uni<KnotReport> knotReport(String sessionId) {
        Map<String, Object> params = Map.of("sid", sessionId);

        Uni<KnotSession>                 sessionUni    = loadSession(sessionId, params);
        Uni<List<KnotTable>>             tablesUni     = loadTables(params);
        Uni<List<KnotStatement>>         statementsUni = loadStatements(params);
        Uni<List<KnotSnippet>>           snippetsUni   = loadSnippets(params);
        Uni<List<KnotAtom>>              atomsUni      = loadAtoms(params);
        Uni<List<KnotOutputColumn>>      outColsUni    = loadOutputColumns(params);
        Uni<List<KnotAffectedColumn>>    affColsUni    = loadAffectedColumns(params);
        Uni<List<KnotCall>>              callsUni      = loadCalls(params);
        Uni<KnotParamVars>               paramVarsUni  = loadParamsAndVars(params);

        return Uni.combine().all()
            .unis(sessionUni, tablesUni, statementsUni, snippetsUni, atomsUni, outColsUni, affColsUni, callsUni, paramVarsUni)
            .asTuple()
            .map(t -> {
                KnotParamVars pv = t.getItem9();
                return new KnotReport(
                    t.getItem1(), t.getItem2(), pv.routines(), t.getItem3(),
                    t.getItem4(), t.getItem5(), t.getItem6(), t.getItem7(),
                    t.getItem8(), pv.parameters(), pv.variables()
                );
            });
    }

    /** Internal holder for params + vars + routines combined query. */
    private record KnotParamVars(List<KnotRoutine> routines, List<KnotParameter> parameters, List<KnotVariable> variables) {}

    // ── Session summary ───────────────────────────────────────────────────────

    private Uni<KnotSession> loadSession(String sessionId, Map<String, Object> params) {

        // ArcadeDB SQL uses :param syntax; Cypher uses $param
        String sqlMeta = """
            SELECT @rid AS id, session_id, session_name,
                   coalesce(dialect,'plsql') AS dialect,
                   coalesce(file_path,'')    AS file_path,
                   coalesce(processing_ms,0) AS processing_ms
            FROM DaliSession WHERE session_id = :sid
            LIMIT 1
            """;

        // Routine count — uses DaliRoutine(session_id) NOTUNIQUE index directly.
        // Was: in('BELONGS_TO_SESSION')[session_id=:sid].size() > 0 — O(n) full scan.
        String sqlRoutineCount = """
            SELECT count(*) AS routineCount
            FROM DaliRoutine
            WHERE session_id = :sid
            """;

        // Stmt geoids — uses DaliStatement(session_id) NOTUNIQUE index directly.
        // Was: in('CONTAINS_STMT').in('BELONGS_TO_SESSION')[...].size() > 0 — 2-hop reverse scan.
        String sqlStmtGeoids = """
            SELECT stmt_geoid
            FROM DaliStatement
            WHERE session_id = :sid
              AND outE('CHILD_OF').size() = 0
            """;

        // Atom counts grouped by status — uses DaliAtom(session_id) NOTUNIQUE index directly.
        // Was: 3-hop reverse traversal on ALL atoms (561 995 rows) — catastrophic full scan.
        String sqlAtoms = """
            SELECT status, count(*) AS cnt
            FROM DaliAtom
            WHERE session_id = :sid
            GROUP BY status
            """;

        return Uni.combine().all()
            .unis(
                arcade.sqlIn(lineageDb(), sqlMeta, params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlRoutineCount, params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlStmtGeoids, params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlAtoms, params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> {
                List<Map<String, Object>> metaRows     = t.getItem1();
                List<Map<String, Object>> routineRows  = t.getItem2();
                List<Map<String, Object>> stmtRows     = t.getItem3();
                List<Map<String, Object>> atomRows     = t.getItem4();

                if (metaRows.isEmpty()) return emptySession(sessionId);

                Map<String, Object> meta = metaRows.get(0);

                int routineCount = routineRows.isEmpty() ? 0 : num(routineRows.get(0), "routineCount");

                // Parse stmt type breakdown from geoids
                int sel = 0, ins = 0, upd = 0, del = 0, mer = 0, cur = 0, oth = 0;
                for (var row : stmtRows) {
                    String geoid = str(row, "stmt_geoid");
                    String stype = parseStmtType(geoid).toUpperCase();
                    switch (stype) {
                        case "SELECT"                            -> sel++;
                        case "INSERT"                            -> ins++;
                        case "UPDATE"                            -> upd++;
                        case "DELETE"                            -> del++;
                        case "MERGE"                             -> mer++;
                        case "CURSOR", "DINAMIC_CURSOR",
                             "DYNAMIC_CURSOR", "FOR_CURSOR"      -> cur++;
                        default                                  -> { if (!stype.isEmpty()) oth++; }
                    }
                }

                // Atom breakdown — actual status values in hound DB
                int atomTotal = 0, atomResolved = 0, atomFailed = 0, atomConst = 0, atomFunc = 0;
                for (var row : atomRows) {
                    String status = str(row, "status").toLowerCase();
                    int cnt = num(row, "cnt");
                    atomTotal += cnt;
                    switch (status) {
                        case "обработано"    -> atomResolved += cnt;
                        case "unresolved"    -> atomFailed   += cnt;
                        case "constant"      -> atomConst    += cnt;
                        case "function_call" -> atomFunc     += cnt;
                    }
                }

                String filePath    = str(meta, "file_path");
                String sessionName = deriveName(str(meta, "session_name"), filePath);

                return new KnotSession(
                    str(meta, "id"),
                    str(meta, "session_id"),
                    sessionName,
                    str(meta, "dialect"),
                    filePath,
                    num(meta, "processing_ms"),
                    0, 0, 0, 0, routineCount, 0, 0,
                    sel, ins, upd, del, mer, cur, oth,
                    atomTotal, atomResolved, atomFailed, atomConst, atomFunc,
                    0, 0, 0, 0
                );
            });
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    private Uni<List<KnotTable>> loadTables(Map<String, Object> params) {
        // Both queries start from DaliStatement(session_id) NOTUNIQUE index.
        // Avoids 3-hop traversal Session→Routine→Statement used in the original code.
        // Column details are lazy-loaded via knotTableDetail — only count here.
        String cypherMain = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:READS_FROM|WRITES_TO]->(t:DaliTable)
            OPTIONAL MATCH (t)-[:HAS_COLUMN]->(c:DaliColumn)
            WITH t, count(DISTINCT c) AS colCount
            RETURN DISTINCT
                   id(t)                                        AS tid,
                   t.table_geoid                               AS geoid,
                   t.table_name                                AS name,
                   coalesce(t.schema_geoid,'')                 AS schema,
                   'TABLE'                                     AS tableType,
                   t.aliases                                   AS tableAliases,
                   coalesce(t.data_source,'reconstructed')     AS dataSource,
                   colCount
            ORDER BY name
            LIMIT 300
            """;

        // READS_FROM and WRITES_TO counts in a single query — split by edgeType in Java.
        // Was: 2 separate Cypher queries with 3-hop traversal each (3 total → now 2).
        String cypherCounts = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[e:READS_FROM|WRITES_TO]->(t:DaliTable)
            RETURN t.table_name AS tableName, type(e) AS edgeType, count(*) AS cnt
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypherIn(lineageDb(), cypherMain,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherCounts, params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> buildTables(t.getItem1(), t.getItem2()));
    }

    private List<KnotTable> buildTables(
        List<Map<String, Object>> rows,
        List<Map<String, Object>> countRows
    ) {
        // Split merged count query by edgeType into src/tgt maps
        Map<String, Integer> srcMap = new HashMap<>();
        Map<String, Integer> tgtMap = new HashMap<>();
        for (var r : countRows) {
            String name = str(r, "tableName");
            if ("READS_FROM".equals(str(r, "edgeType"))) srcMap.put(name, num(r, "cnt"));
            else                                          tgtMap.put(name, num(r, "cnt"));
        }

        // Each row is one table (columns aggregated to count by Cypher WITH clause)
        List<KnotTable> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (var r : rows) {
            String tid = str(r, "tid");
            if (!seen.add(tid)) continue; // safety dedup
            String tableName = str(r, "name");
            List<String> aliases = toStringList(r.get("tableAliases"));
            result.add(new KnotTable(
                tid,
                str(r, "geoid"),
                tableName,
                str(r, "schema"),
                str(r, "tableType"),
                num(r, "colCount"),
                srcMap.getOrDefault(tableName, 0),
                tgtMap.getOrDefault(tableName, 0),
                str(r, "dataSource"),
                aliases
            ));
        }
        return result;
    }

    // ── Table detail (lazy) ───────────────────────────────────────────────────

    public Uni<KnotTableDetail> knotTableDetail(String sessionId, String tableGeoid) {
        Map<String, Object> params    = Map.of("sid", sessionId, "tg", tableGeoid);
        Map<String, Object> tgParams  = Map.of("tg", tableGeoid);

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

    // ── Statements ────────────────────────────────────────────────────────────

    private Uni<List<KnotStatement>> loadStatements(Map<String, Object> params) {
        // Fetch ALL statements for the session (not just roots).
        // Tree is built in Java via CHILD_OF edges from a second query.
        // Atom status values in hound DB: 'Обработано' | 'unresolved' | 'constant' | 'function_call'
        // All queries start from DaliStatement(session_id) NOTUNIQUE index — avoids 3-hop
        // traversal prefix Session→Routine→Statement used in older versions.

        // Query 1: statement metadata + atom counts.
        // Sources/targets are intentionally NOT collected here to avoid Cartesian product
        // with atoms that breaks collect(DISTINCT {aliases: list}) in ArcadeDB Cypher.
        String cypherStmts = """
            MATCH (stmt:DaliStatement {session_id: $sid})
            MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(stmt)
            OPTIONAL MATCH (stmt)-[:HAS_ATOM]->(a:DaliAtom)
            RETURN id(stmt)                                                         AS sid,
                   stmt.stmt_geoid                                                  AS geoid,
                   coalesce(stmt.type, '')                                          AS stmtType,
                   coalesce(stmt.line_start, 0)                                     AS lineStart,
                   r.routine_name                                                   AS routineName,
                   coalesce(r.routine_type, '')                                     AS routineType,
                   stmt.aliases                                                     AS stmtAliases,
                   count(a)                                                         AS atomTotal,
                   count(CASE WHEN toLower(a.status)='обработано'  THEN 1 END)     AS atomResolved,
                   count(CASE WHEN toLower(a.status)='unresolved'  THEN 1 END)     AS atomFailed,
                   count(CASE WHEN toLower(a.status)='constant'    THEN 1 END)     AS atomConst,
                   count(CASE WHEN toLower(a.status)='function_call' THEN 1 END)   AS atomFunc
            ORDER BY r.routine_name, geoid
            LIMIT 1000
            """;

        // Query 2: TABLE sources/targets — READS_FROM/WRITES_TO → DaliTable
        String cypherTables = """
            MATCH (stmt:DaliStatement {session_id: $sid})
            OPTIONAL MATCH (stmt)-[rf:READS_FROM]->(src:DaliTable)
            OPTIONAL MATCH (stmt)-[wt:WRITES_TO]->(tgt:DaliTable)
            RETURN id(stmt)                             AS sid,
                   src.table_name                       AS srcName,
                   coalesce(src.table_geoid, '')        AS srcGeoid,
                   rf.aliases                           AS srcAliases,
                   tgt.table_name                       AS tgtName,
                   coalesce(tgt.table_geoid, '')        AS tgtGeoid,
                   wt.aliases                           AS tgtAliases
            LIMIT 5000
            """;

        // Query 3: STMT sources — USES_SUBQUERY → DaliStatement (inline subqueries)
        String cypherStmtSrc = """
            MATCH (stmt:DaliStatement {session_id: $sid})
            MATCH (stmt)-[rf:USES_SUBQUERY]->(src:DaliStatement)
            RETURN id(stmt)                             AS sid,
                   src.stmt_geoid                       AS srcGeoid,
                   rf.aliases                           AS srcAliases
            LIMIT 5000
            """;

        // Query 4: CHILD_OF direction: child -[CHILD_OF]-> parent
        String cypherEdges = """
            MATCH (child:DaliStatement {session_id: $sid})-[:CHILD_OF]->(parent:DaliStatement)
            RETURN id(child) AS childId, id(parent) AS parentId
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypherIn(lineageDb(), cypherStmts,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherTables,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherStmtSrc, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherEdges,   params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> buildStatementTree(t.getItem1(), t.getItem2(), t.getItem3(), t.getItem4()));
    }

    private List<KnotStatement> buildStatementTree(
        List<Map<String, Object>> stmtRows,
        List<Map<String, Object>> tableRows,
        List<Map<String, Object>> stmtSrcRows,
        List<Map<String, Object>> edgeRows
    ) {
        // Build source/target maps: stmtId → LinkedHashMap<key, KnotSourceRef> (deduplicated)
        Map<String, Map<String, KnotSourceRef>> srcByStmt = new LinkedHashMap<>();
        Map<String, Map<String, KnotSourceRef>> tgtByStmt = new LinkedHashMap<>();

        // TABLE sources/targets
        for (var r : tableRows) {
            String sid = str(r, "sid");
            String srcName = str(r, "srcName");
            if (!srcName.isEmpty()) {
                srcByStmt.computeIfAbsent(sid, k -> new LinkedHashMap<>())
                    .putIfAbsent(srcName, new KnotSourceRef(
                        srcName,
                        str(r, "srcGeoid"),
                        toStringList(r.get("srcAliases")),
                        "TABLE"
                    ));
            }
            String tgtName = str(r, "tgtName");
            if (!tgtName.isEmpty()) {
                tgtByStmt.computeIfAbsent(sid, k -> new LinkedHashMap<>())
                    .putIfAbsent(tgtName, new KnotSourceRef(
                        tgtName,
                        str(r, "tgtGeoid"),
                        toStringList(r.get("tgtAliases")),
                        "TABLE"
                    ));
            }
        }

        // STMT sources (CTEs, subqueries) — READS_FROM → DaliStatement
        for (var r : stmtSrcRows) {
            String sid      = str(r, "sid");
            String srcGeoid = str(r, "srcGeoid");
            if (!srcGeoid.isEmpty()) {
                // Use stmt_geoid as key; short display name = last meaningful segment
                String displayName = parseStmtShortName(srcGeoid);
                srcByStmt.computeIfAbsent(sid, k -> new LinkedHashMap<>())
                    .putIfAbsent(srcGeoid, new KnotSourceRef(
                        displayName,
                        srcGeoid,
                        toStringList(r.get("srcAliases")),
                        "STMT"
                    ));
            }
        }

        // Build flat map of all statements (children list is mutable ArrayList)
        LinkedHashMap<String, KnotStatement> byId = new LinkedHashMap<>();
        for (var r : stmtRows) {
            String id         = str(r, "sid");
            String geoid      = str(r, "geoid");
            String stmtTypeDb = str(r, "stmtType");
            int    lineStartDb = num(r, "lineStart");
            List<KnotSourceRef> sources = srcByStmt.containsKey(id)
                ? new ArrayList<>(srcByStmt.get(id).values()) : List.of();
            List<KnotSourceRef> targets = tgtByStmt.containsKey(id)
                ? new ArrayList<>(tgtByStmt.get(id).values()) : List.of();
            byId.put(id, new KnotStatement(
                id, geoid,
                !stmtTypeDb.isEmpty() ? stmtTypeDb : parseStmtType(geoid),
                lineStartDb > 0       ? lineStartDb  : parseLineNumber(geoid),
                str(r, "routineName"),
                parsePackageName(geoid),
                str(r, "routineType"),
                sources,
                targets,
                toStringList(r.get("stmtAliases")),
                num(r, "atomTotal"),
                num(r, "atomResolved"),
                num(r, "atomFailed"),
                num(r, "atomConst"),
                num(r, "atomFunc"),
                new ArrayList<>()
            ));
        }

        // Attach children to parents via CHILD_OF edges
        Set<String> childIds = new HashSet<>();
        for (var e : edgeRows) {
            String childId  = str(e, "childId");
            String parentId = str(e, "parentId");
            KnotStatement parent = byId.get(parentId);
            KnotStatement child  = byId.get(childId);
            if (parent != null && child != null) {
                parent.children().add(child);
                childIds.add(childId);
            }
        }

        // Return only root statements (those not appearing as children)
        return byId.entrySet().stream()
            .filter(e -> !childIds.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .toList();
    }

    // ── Snippet by geoid OR RID (lazy, called from KNOT inspector on demand) ──
    //
    // The KNOT standalone page passes the real stmt_geoid (from the KnotStatement
    // batch query). The LOOM canvas inspector uses ArcadeDB RIDs as React Flow
    // node IDs (see ExploreService projection), so it passes values like
    // "#25:12304". We accept both:
    //   - Input starts with '#' → resolve via the DaliStatement RID → stmt_geoid
    //     → DaliSnippet lookup
    //   - Anything else → direct DaliSnippet lookup by stmt_geoid

    public Uni<String> knotSnippet(String idOrGeoid) {
        if (idOrGeoid == null || idOrGeoid.isBlank()) return Uni.createFrom().nullItem();
        boolean isRid = idOrGeoid.startsWith("#");
        // DaliSnippet is a DOCUMENT (large SQL texts — VERTEX promotion rejected).
        //
        // @rid path (v28+): element_rid stores the ArcadeDB @rid of DaliStatement directly,
        //   so lookup is WHERE element_rid = :rid — O(1) via NOTUNIQUE index, no subquery.
        //   (Pre-v28 fallback: IN (SELECT stmt_geoid FROM DaliStatement WHERE @rid = :rid) —
        //   ArcadeDB SQL subqueries return a result set, `= (SELECT …)` compares string to
        //   record and always yields 0 rows; `IN (SELECT …)` is the safe form.)
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

    public Uni<KnotScript> knotScript(String sessionId) {
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

    public Uni<KnotSourceFile> knotSourceFile(String sessionId) {
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

    // ── Snippets ──────────────────────────────────────────────────────────────

    private Uni<List<KnotSnippet>> loadSnippets(Map<String, Object> params) {
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

    // ── Atoms ─────────────────────────────────────────────────────────────────

    private Uni<List<KnotAtom>> loadAtoms(Map<String, Object> params) {
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

    // ── Routine calls ─────────────────────────────────────────────────────────

    private Uni<List<KnotCall>> loadCalls(Map<String, Object> params) {
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

    // ── Output columns ───────────────────────────────────────────────────────

    private Uni<List<KnotOutputColumn>> loadOutputColumns(Map<String, Object> params) {
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

    // ── Affected columns ─────────────────────────────────────────────────────

    private Uni<List<KnotAffectedColumn>> loadAffectedColumns(Map<String, Object> params) {
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

    // ── Parameters & Variables ────────────────────────────────────────────────

    private Uni<KnotParamVars> loadParamsAndVars(Map<String, Object> params) {
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Parse stmt type from geoid: "SCHEMA.PKG:RTYPE:RNAME:STMT_TYPE:LINE"
     * Returns part[3] (e.g. "INSERT", "SELECT").
     */
    static String parseStmtType(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "UNKNOWN";
        String[] parts = geoid.split(":");
        String t = parts.length >= 4 ? parts[3] : "";
        return t.isEmpty() ? "UNKNOWN" : t;
    }

    /**
     * Parse line number from geoid: part[4].
     */
    static int parseLineNumber(String geoid) {
        if (geoid == null || geoid.isEmpty()) return 0;
        String[] parts = geoid.split(":");
        if (parts.length >= 5) {
            try { return Integer.parseInt(parts[4]); }
            catch (NumberFormatException ignored) { return 0; }
        }
        return 0;
    }

    /**
     * Short display name for a statement used as a source (CTE/subquery).
     * Returns "ROUTINE_NAME:STMT_TYPE:LINE" — enough to identify it without full geoid noise.
     * E.g. "DWH.PKG:PROCEDURE:LOAD:SELECT:42" → "LOAD:SELECT:42"
     */
    static String parseStmtShortName(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "?";
        String[] parts = geoid.split(":");
        // parts: [0]=pkg, [1]=routineType, [2]=routineName, [3]=stmtType, [4]=line, ...
        if (parts.length >= 5) return parts[2] + ":" + parts[3] + ":" + parts[4];
        if (parts.length >= 3) return parts[2];
        return geoid;
    }

    /**
     * Parse package name from geoid: part[0] (e.g. "DWH.CALC_PKL_CRED").
     */
    static String parsePackageName(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "";
        int idx = geoid.indexOf(':');
        return idx > 0 ? geoid.substring(0, idx) : geoid;
    }

    /**
     * Derive a display name: use session_name if present, otherwise filename without extension from filePath.
     */
    static String deriveName(String sessionName, String filePath) {
        if (sessionName != null && !sessionName.isBlank()) return sessionName;
        if (filePath == null || filePath.isBlank()) return "";
        // Extract filename from Windows or Unix path
        int slash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        String filename = slash >= 0 ? filePath.substring(slash + 1) : filePath;
        int dot = filename.indexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
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

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object v) {
        if (v instanceof List<?> list) {
            return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isEmpty())
                .toList();
        }
        // ArcadeDB Cypher may return array-type properties as a JSON string e.g. ["alias1","alias2"]
        if (v instanceof String s && !s.isBlank() && s.startsWith("[")) {
            String inner = s.substring(1, s.length() - 1).trim();
            if (inner.isEmpty()) return List.of();
            return java.util.Arrays.stream(inner.split(","))
                .map(item -> item.trim().replaceAll("^\"|\"$", ""))
                .filter(item -> !item.isEmpty())
                .toList();
        }
        return List.of();
    }


    private static KnotSession emptySession(String sessionId) {
        return new KnotSession(sessionId, sessionId, sessionId, "plsql", "", 0,
            0,0,0,0,0,0,0, 0,0,0,0,0,0,0, 0,0,0,0,0, 0,0,0,0);
    }
}
