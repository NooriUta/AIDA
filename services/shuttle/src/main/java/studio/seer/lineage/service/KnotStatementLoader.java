package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.KnotSourceRef;
import studio.seer.lineage.model.KnotStatement;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.*;

/**
 * Loads and assembles the statement tree for a KNOT report session.
 *
 * <p>Extracted from {@link KnotService} (LOC refactor — QG-ARCH-INVARIANTS §2.4).
 * Owns the four Cypher queries that fetch DaliStatement data and the
 * {@code buildStatementTree} assembly that wires parent–child edges.
 */
@ApplicationScoped
class KnotStatementLoader {

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── Statements ──────────────────────────��──────────────────────────��──────

    /** G4: overload with source_file filter for batch sessions. */
    Uni<List<KnotStatement>> loadStatements(Map<String, Object> params, String sourceFile) {
        if (sourceFile == null) return loadStatements(params);
        // Inject source_file into Cypher property maps
        String sfProp = ", source_file: $sf";
        return loadStatementsFiltered(params, sfProp);
    }

    Uni<List<KnotStatement>> loadStatements(Map<String, Object> params) {
        // Fetch ALL statements for the session (not just roots).
        // Tree is built in Java via CHILD_OF edges from a second query.
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
                   count(CASE WHEN a.primary_status='RESOLVED'      THEN 1
                              WHEN toLower(a.status)='обработано' THEN 1 END)     AS atomResolved,
                   count(CASE WHEN a.primary_status='UNRESOLVED'   THEN 1
                              WHEN toLower(a.status)='unresolved'  THEN 1 END)     AS atomFailed,
                   count(CASE WHEN a.primary_status='CONSTANT'     THEN 1
                              WHEN toLower(a.status)='constant'    THEN 1 END)     AS atomConst,
                   count(CASE WHEN a.primary_status='FUNCTION_CALL' THEN 1
                              WHEN toLower(a.status)='function_call' THEN 1 END)   AS atomFunc
            ORDER BY r.routine_name, geoid
            LIMIT 1000
            """;

        // Query 2: TABLE sources/targets — READS_FROM/WRITES_TO → DaliTable
        String cypherTables = """
            MATCH (stmt:DaliStatement {session_id: $sid})
            OPTIONAL MATCH (src:DaliTable)-[rf:READS_FROM]->(stmt)
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

    /**
     * G4: filtered variant — appends source_file to all Cypher property maps.
     * Duplicated queries with sfProp injected (DaliStatement nodes carry source_file after G4 schema migration).
     */
    private Uni<List<KnotStatement>> loadStatementsFiltered(Map<String, Object> params, String sfProp) {
        String cypherStmts = """
            MATCH (stmt:DaliStatement {session_id: $sid""" + sfProp + """
            })
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
                   count(CASE WHEN a.primary_status='RESOLVED'      THEN 1
                              WHEN toLower(a.status)='обработано' THEN 1 END)     AS atomResolved,
                   count(CASE WHEN a.primary_status='UNRESOLVED'   THEN 1
                              WHEN toLower(a.status)='unresolved'  THEN 1 END)     AS atomFailed,
                   count(CASE WHEN a.primary_status='CONSTANT'     THEN 1
                              WHEN toLower(a.status)='constant'    THEN 1 END)     AS atomConst,
                   count(CASE WHEN a.primary_status='FUNCTION_CALL' THEN 1
                              WHEN toLower(a.status)='function_call' THEN 1 END)   AS atomFunc
            ORDER BY r.routine_name, geoid
            LIMIT 1000
            """;

        String cypherTables = """
            MATCH (stmt:DaliStatement {session_id: $sid""" + sfProp + """
            })
            OPTIONAL MATCH (src:DaliTable)-[rf:READS_FROM]->(stmt)
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

        String cypherStmtSrc = """
            MATCH (stmt:DaliStatement {session_id: $sid""" + sfProp + """
            })
            MATCH (stmt)-[rf:USES_SUBQUERY]->(src:DaliStatement)
            RETURN id(stmt)                             AS sid,
                   src.stmt_geoid                       AS srcGeoid,
                   rf.aliases                           AS srcAliases
            LIMIT 5000
            """;

        String cypherEdges = """
            MATCH (child:DaliStatement {session_id: $sid""" + sfProp + """
            })-[:CHILD_OF]->(parent:DaliStatement)
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

        // STMT sources (CTEs, subqueries) — USES_SUBQUERY → DaliStatement
        for (var r : stmtSrcRows) {
            String sid      = str(r, "sid");
            String srcGeoid = str(r, "srcGeoid");
            if (!srcGeoid.isEmpty()) {
                // Use stmt_geoid as key; short display name = last meaningful segment
                String displayName = KnotGeoidParser.parseStmtShortName(srcGeoid);
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
                !stmtTypeDb.isEmpty() ? stmtTypeDb : KnotGeoidParser.parseStmtType(geoid),
                lineStartDb > 0       ? lineStartDb  : KnotGeoidParser.parseLineNumber(geoid),
                str(r, "routineName"),
                KnotGeoidParser.parsePackageName(geoid),
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
}
