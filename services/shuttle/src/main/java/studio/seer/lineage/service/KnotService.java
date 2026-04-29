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

    @Inject ArcadeGateway              arcade;
    @Inject SeerIdentity               identity;
    @Inject YggLineageRegistry         lineageRegistry;
    @Inject YggSourceArchiveRegistry   sourceArchiveRegistry;
    @Inject KnotColumnLineageService   columnLineageService;

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
        Uni<List<KnotSnippet>>           snippetsUni   = columnLineageService.loadSnippets(params);
        Uni<List<KnotAtom>>              atomsUni      = columnLineageService.loadAtoms(params);
        Uni<List<KnotOutputColumn>>      outColsUni    = columnLineageService.loadOutputColumns(params);
        Uni<List<KnotAffectedColumn>>    affColsUni    = columnLineageService.loadAffectedColumns(params);
        Uni<List<KnotCall>>              callsUni      = columnLineageService.loadCalls(params);
        Uni<KnotColumnLineageService.KnotParamVars> paramVarsUni = columnLineageService.loadParamsAndVars(params);

        return Uni.combine().all()
            .unis(sessionUni, tablesUni, statementsUni, snippetsUni, atomsUni, outColsUni, affColsUni, callsUni, paramVarsUni)
            .asTuple()
            .map(t -> {
                KnotColumnLineageService.KnotParamVars pv = t.getItem9();
                return new KnotReport(
                    t.getItem1(), t.getItem2(), pv.routines(), t.getItem3(),
                    t.getItem4(), t.getItem5(), t.getItem6(), t.getItem7(),
                    t.getItem8(), pv.parameters(), pv.variables()
                );
            });
    }

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

    // ── Table detail + column analytics (delegated to KnotColumnLineageService) ──

    public Uni<KnotTableDetail> knotTableDetail(String sessionId, String tableGeoid) {
        return columnLineageService.knotTableDetail(sessionId, tableGeoid);
    }

    public Uni<List<KnotTableUsage>> knotTableRoutines(String tableRid) {
        return columnLineageService.knotTableRoutines(tableRid);
    }

    public Uni<List<KnotColumnUsage>> knotColumnStatements(String columnGeoid) {
        return columnLineageService.knotColumnStatements(columnGeoid);
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
            List<KnotSourceRef> sources = new ArrayList<>(srcByStmt.getOrDefault(id, Map.of()).values());
            List<KnotSourceRef> targets = new ArrayList<>(tgtByStmt.getOrDefault(id, Map.of()).values());
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

    // ── Statement extras (delegated to KnotColumnLineageService) ────────────

    public Uni<StatementExtras> knotStatementExtras(String idOrGeoid) {
        return columnLineageService.knotStatementExtras(idOrGeoid);
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
