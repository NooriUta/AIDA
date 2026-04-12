// File: src/main/java/com/hound/storage/EmbeddedWriter.java
package com.hound.storage;

import com.arcadedb.database.Database;
import com.arcadedb.graph.MutableVertex;
import com.hound.metrics.PipelineTimer;
import com.hound.semantic.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.hound.storage.WriteHelpers.*;

/**
 * Writes one SemanticResult into an embedded ArcadeDB instance.
 *
 * Package-private — used only by {@link ArcadeDBSemanticWriter}.
 */
class EmbeddedWriter {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedWriter.class);

    private final Database db;

    EmbeddedWriter(Database db) {
        this.db = db;
    }

    // ═══════════════════════════════════════════════════════════════
    // ensureCanonicalPool — embedded half
    // ═══════════════════════════════════════════════════════════════

    CanonicalPool ensurePool(String dbName, String appName, String appGeoid) {
        CanonicalPool pool = new CanonicalPool(dbName);
        String resolvedAppGeoid = (appGeoid != null) ? appGeoid : appName;
        try {
            db.transaction(() -> {
                MutableVertex appVtx = null;
                if (appName != null) {
                    var appRs = db.query("sql",
                            "SELECT FROM DaliApplication WHERE app_geoid = :g LIMIT 1",
                            Map.of("g", resolvedAppGeoid));
                    if (appRs.hasNext()) {
                        appVtx = (MutableVertex) appRs.next().toElement();
                    } else {
                        appVtx = db.newVertex("DaliApplication")
                                .set("app_name",   appName)
                                .set("app_geoid",  resolvedAppGeoid)
                                .set("created_at", System.currentTimeMillis())
                                .save();
                    }
                    pool.setApplicationVtx(appVtx);
                }
                var dbRs = db.query("sql",
                        "SELECT FROM DaliDatabase WHERE db_geoid = :g LIMIT 1",
                        Map.of("g", dbName));
                MutableVertex dbVtx;
                if (dbRs.hasNext()) {
                    dbVtx = (MutableVertex) dbRs.next().toElement();
                } else {
                    dbVtx = db.newVertex("DaliDatabase")
                            .set("db_name",    dbName)
                            .set("db_geoid",   dbName)
                            .set("created_at", System.currentTimeMillis())
                            .save();
                    if (appVtx != null) dbVtx.newEdge("BELONGS_TO_APP", appVtx, true, new Object[0]);
                }
                pool.setDatabaseVtx(dbVtx);
            });
        } catch (Exception e) {
            logger.warn("ensurePool (embedded) failed for '{}': {}", dbName, e.getMessage());
        }
        logger.info("CanonicalPool created: db='{}' app='{}'", dbName, appName != null ? appName : "—");
        return pool;
    }

    // ═══════════════════════════════════════════════════════════════
    // Main write
    // ═══════════════════════════════════════════════════════════════

    void write(String sid, SemanticResult result, PipelineTimer timer,
               CanonicalPool pool, String dbName) {
        timer.start("write.vtx");
        db.transaction(() -> {
            Structure str = result.getStructure();
            if (str == null) return;

            MutableVertex sessionV = db.newVertex("DaliSession")
                    .set("session_id", sid)
                    .set("db_name",    dbName)
                    .set("file_path",  result.getFilePath())
                    .set("dialect",    result.getDialect())
                    .set("processing_time_ms", result.getProcessingTimeMs())
                    .set("created_at", System.currentTimeMillis())
                    .save();

            // ── DaliDatabase ──
            Map<String, MutableVertex> dbV = new LinkedHashMap<>();
            if (pool == null) {
                for (var e : str.getDatabases().entrySet()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> d = (Map<String, Object>) e.getValue();
                    var existRs = db.query("sql",
                            "SELECT FROM DaliDatabase WHERE db_geoid = :g LIMIT 1",
                            Map.of("g", e.getKey()));
                    MutableVertex v;
                    if (existRs.hasNext()) {
                        v = (MutableVertex) existRs.next().toElement();
                    } else {
                        v = db.newVertex("DaliDatabase")
                                .set("db_geoid",   e.getKey())
                                .set("db_name",    d.get("name"))
                                .set("created_at", System.currentTimeMillis())
                                .save();
                    }
                    dbV.put(e.getKey(), v);
                }
            }

            // ── DaliSchema ──
            Map<String, MutableVertex> schV = new LinkedHashMap<>();
            for (var e : str.getSchemas().entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sc = (Map<String, Object>) e.getValue();
                MutableVertex v;
                if (pool != null) {
                    String cg = pool.canonicalSchema(e.getKey());
                    v = pool.getSchemaVtx(cg);
                    if (v == null) {
                        var rs = db.query("sql",
                                "SELECT FROM DaliSchema WHERE db_name = :d AND schema_geoid = :s LIMIT 1",
                                Map.of("d", dbName, "s", e.getKey()));
                        if (rs.hasNext()) {
                            v = (MutableVertex) rs.next().toElement();
                        } else {
                            v = db.newVertex("DaliSchema")
                                    .set("db_name",      dbName)
                                    .set("db_geoid",     dbName)
                                    .set("schema_geoid", e.getKey())
                                    .set("schema_name",  sc.get("name"))
                                    .save();
                            if (pool.getDatabaseVtx() != null)
                                pool.getDatabaseVtx().newEdge("CONTAINS_SCHEMA", v, true, new Object[0]);
                        }
                        pool.putSchemaVtx(cg, v);
                    }
                } else {
                    String schDbGeoid = (String) sc.get("db");
                    @SuppressWarnings("unchecked")
                    String schDbName = (schDbGeoid != null && str.getDatabases().containsKey(schDbGeoid))
                            ? (String) ((Map<String, Object>) str.getDatabases().get(schDbGeoid)).get("name")
                            : dbName;
                    v = db.newVertex("DaliSchema")
                            .set("session_id",   sid)
                            .set("schema_geoid", e.getKey())
                            .set("schema_name",  sc.get("name"))
                            .set("db_name",      schDbName)
                            .set("db_geoid",     schDbGeoid != null ? schDbGeoid : schDbName)
                            .save();
                }
                sessionV.newEdge("BELONGS_TO_SESSION", v, true, new Object[0]);
                schV.put(e.getKey(), v);
            }

            // ── DaliPackage ──
            Map<String, MutableVertex> pkgV = new LinkedHashMap<>();
            for (var e : str.getPackages().entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pkg = (Map<String, Object>) e.getValue();
                MutableVertex v = db.newVertex("DaliPackage")
                        .set("session_id",   sid)
                        .set("package_geoid", e.getKey())
                        .set("package_name", pkg.get("package_name"))
                        .set("schema_geoid", pkg.get("schema_geoid"))
                        .set("routine_geoid", e.getKey())
                        .set("routine_name", pkg.get("package_name"))
                        .set("routine_type", "PACKAGE")
                        .save();
                pkgV.put(e.getKey(), v);
                String sg = (String) pkg.get("schema_geoid");
                if (sg != null && !sg.isEmpty()) {
                    MutableVertex sv2 = schV.get(sg.toUpperCase());
                    if (sv2 == null && pool != null)
                        sv2 = pool.getSchemaVtx(pool.canonicalSchema(sg));
                    if (sv2 == null) {
                        var rsSchema = db.query("sql",
                                "SELECT FROM DaliSchema WHERE schema_geoid = :s LIMIT 1",
                                Map.of("s", sg));
                        if (rsSchema.hasNext()) sv2 = (MutableVertex) rsSchema.next().toElement();
                    }
                    if (sv2 != null) sv2.newEdge("CONTAINS_ROUTINE", v, true, new Object[0]);
                }
            }

            // ── DaliTable ──
            Map<String, MutableVertex> tblV = new LinkedHashMap<>();
            for (var e : str.getTables().entrySet()) {
                TableInfo t = e.getValue();
                MutableVertex v;
                boolean tblMaster = isMasterTable(e.getKey(), str);
                if (pool != null) {
                    String cg = pool.canonical(e.getKey());
                    v = pool.getTableVtx(cg);
                    if (v == null) {
                        var rs = db.query("sql",
                                "SELECT FROM DaliTable WHERE db_name = :d AND table_geoid = :t LIMIT 1",
                                Map.of("d", dbName, "t", e.getKey()));
                        if (rs.hasNext()) {
                            v = (MutableVertex) rs.next().toElement();
                            // reconstructed → master upgrade when DDL arrives later
                            if (tblMaster && !MASTER.equals(v.get("data_source")))
                                v.set("data_source", MASTER).save();
                        } else {
                            v = db.newVertex("DaliTable")
                                    .set("db_name",      dbName)
                                    .set("table_geoid",  e.getKey())
                                    .set("table_name",   t.tableName())
                                    .set("schema_geoid", t.schemaGeoid())
                                    .set("table_type",   t.tableType())
                                    .set("aliases",      new ArrayList<>(t.aliases()))
                                    .set("column_count", t.columnCount())
                                    .set("data_source",  tblMaster ? MASTER : RECONSTRUCTED)
                                    .save();
                            if (t.schemaGeoid() != null) {
                                MutableVertex sv2 = schV.get(t.schemaGeoid().toUpperCase());
                                if (sv2 != null) sv2.newEdge("CONTAINS_TABLE", v, true, new Object[0]);
                            }
                        }
                        pool.putTableVtx(cg, v);
                    }
                } else {
                    v = db.newVertex("DaliTable")
                            .set("session_id",   sid)
                            .set("table_geoid",  e.getKey())
                            .set("table_name",   t.tableName())
                            .set("schema_geoid", t.schemaGeoid())
                            .set("table_type",   t.tableType())
                            .set("aliases",      new ArrayList<>(t.aliases()))
                            .set("column_count", t.columnCount())
                            .set("data_source",  tblMaster ? MASTER : RECONSTRUCTED)
                            .save();
                    if (t.schemaGeoid() != null) {
                        MutableVertex sv2 = schV.get(t.schemaGeoid().toUpperCase());
                        if (sv2 != null) sv2.newEdge("CONTAINS_TABLE", v, true, new Object[0]);
                    }
                }
                tblV.put(e.getKey(), v);
            }

            // ── DaliColumn ──
            Map<String, MutableVertex> colV = new LinkedHashMap<>();
            for (var e : str.getColumns().entrySet()) {
                ColumnInfo c = e.getValue();
                MutableVertex v;
                boolean colMaster = isMasterTable(c.getTableGeoid(), str);
                if (pool != null) {
                    String cg = pool.canonicalCol(c.getTableGeoid(), c.getColumnName());
                    v = pool.getColumnVtx(cg);
                    if (v == null) {
                        var rs = db.query("sql",
                                "SELECT FROM DaliColumn WHERE db_name = :d AND column_geoid = :c LIMIT 1",
                                Map.of("d", dbName, "c", e.getKey()));
                        if (rs.hasNext()) {
                            v = (MutableVertex) rs.next().toElement();
                            if (colMaster && !MASTER.equals(v.get("data_source")))
                                v.set("data_source", MASTER).save();
                        } else {
                            v = db.newVertex("DaliColumn")
                                    .set("db_name",        dbName)
                                    .set("column_geoid",   e.getKey())
                                    .set("table_geoid",    c.getTableGeoid())
                                    .set("column_name",    c.getColumnName())
                                    .set("expression",     c.getExpression())
                                    .set("alias",          c.getAlias())
                                    .set("is_output",      c.isOutput())
                                    .set("col_order",      c.getOrder())
                                    .set("ordinal_position", c.getOrdinalPosition())
                                    .set("used_in_statements", new ArrayList<>(c.getUsedInStatements()))
                                    .set("data_source",    colMaster ? MASTER : RECONSTRUCTED)
                                    .save();
                            MutableVertex tv = tblV.get(c.getTableGeoid());
                            if (tv != null) tv.newEdge("HAS_COLUMN", v, true, new Object[0]);
                        }
                        pool.putColumnVtx(cg, v);
                    }
                } else {
                    v = db.newVertex("DaliColumn")
                            .set("session_id",   sid)
                            .set("column_geoid", e.getKey())
                            .set("table_geoid",  c.getTableGeoid())
                            .set("column_name",  c.getColumnName())
                            .set("expression",   c.getExpression())
                            .set("alias",        c.getAlias())
                            .set("is_output",    c.isOutput())
                            .set("col_order",    c.getOrder())
                            .set("ordinal_position", c.getOrdinalPosition())
                            .set("used_in_statements", new ArrayList<>(c.getUsedInStatements()))
                            .set("data_source",  colMaster ? MASTER : RECONSTRUCTED)
                            .save();
                    MutableVertex tv = tblV.get(c.getTableGeoid());
                    if (tv != null) tv.newEdge("HAS_COLUMN", v, true, new Object[0]);
                }
                colV.put(e.getKey(), v);
            }

            // ── DaliRoutine ──
            Map<String, MutableVertex> rtV = new LinkedHashMap<>();
            for (var e : str.getRoutines().entrySet()) {
                RoutineInfo r = e.getValue();
                MutableVertex v = db.newVertex("DaliRoutine")
                        .set("session_id",   sid)
                        .set("routine_geoid", e.getKey())
                        .set("routine_name", r.getName())
                        .set("routine_type", r.getRoutineType())
                        .set("return_type",  r.getReturnType())
                        .set("line_start",   r.getLineStart() > 0 ? r.getLineStart() : null)
                        .set("package_geoid", r.getPackageGeoid())
                        .set("schema_geoid", r.getSchemaGeoid())
                        .set("data_source",  MASTER)
                        .save();
                rtV.put(e.getKey(), v);
                sessionV.newEdge("BELONGS_TO_SESSION", v, true, new Object[0]);
                if (r.getPackageGeoid() != null) {
                    MutableVertex pv = pkgV.get(r.getPackageGeoid().toUpperCase());
                    if (pv != null) pv.newEdge("CONTAINS_ROUTINE", v, true, new Object[0]);
                } else if (r.getSchemaGeoid() != null) {
                    MutableVertex sv2 = schV.get(r.getSchemaGeoid().toUpperCase());
                    if (sv2 != null) sv2.newEdge("CONTAINS_ROUTINE", v, true, new Object[0]);
                }
            }
            for (var e : str.getRoutines().entrySet()) {
                RoutineInfo r = e.getValue();
                if (r.getParentRoutineGeoid() != null) {
                    MutableVertex parent = rtV.get(r.getParentRoutineGeoid());
                    MutableVertex child  = rtV.get(e.getKey());
                    if (parent != null && child != null)
                        parent.newEdge("NESTED_IN", child, true, new Object[0]).set("session_id", sid).save();
                }
            }
            for (var e : str.getRoutines().entrySet()) {
                RoutineInfo r = e.getValue();
                MutableVertex rv = rtV.get(e.getKey());
                if (rv == null) continue;
                for (RoutineInfo.ParameterInfo p : r.getTypedParameters()) {
                    MutableVertex pV = db.newVertex("DaliParameter")
                            .set("session_id",   sid)
                            .set("routine_geoid", e.getKey())
                            .set("param_name",   p.name())
                            .set("param_type",   p.type())
                            .set("param_mode",   p.mode())
                            .save();
                    rv.newEdge("HAS_PARAMETER", pV, true, new Object[0]);
                }
                for (RoutineInfo.VariableInfo vInfo : r.getTypedVariables()) {
                    MutableVertex vV = db.newVertex("DaliVariable")
                            .set("session_id",   sid)
                            .set("routine_geoid", e.getKey())
                            .set("var_name",     vInfo.name())
                            .set("var_type",     vInfo.type())
                            .save();
                    rv.newEdge("HAS_VARIABLE", vV, true, new Object[0]);
                }
            }

            // ── DaliStatement ──
            Map<String, MutableVertex> stV = new LinkedHashMap<>();
            for (var e : str.getStatements().entrySet()) {
                StatementInfo s = e.getValue();
                MutableVertex v = db.newVertex("DaliStatement")
                        .set("session_id",            sid)
                        .set("stmt_geoid",            e.getKey())
                        .set("type",                  s.getType())
                        .set("subtype",               s.getSubtype())
                        .set("line_start",            s.getLineStart())
                        .set("line_end",              s.getLineEnd())
                        .set("parent_statement",      s.getParentStatementGeoid())
                        .set("parent_statement_type", parentType(s.getParentStatementGeoid(), str.getStatements()))
                        .set("routine_geoid",         s.getRoutineGeoid())
                        .set("short_name",            s.getShortName())
                        .set("aliases",               new ArrayList<>(s.getAliases()))
                        .set("child_statements",      new ArrayList<>(s.getChildStatements()))
                        .set("source_table_geoids",   new ArrayList<>(s.getSourceTables().keySet()))
                        .set("target_table_geoids",   new ArrayList<>(s.getTargetTables().keySet()))
                        .set("source_subquery_geoids",new ArrayList<>(s.getSourceSubqueries().keySet()))
                        .set("source_tables_json",    toJson(new ArrayList<>(s.getSourceTables().values())))
                        .set("target_tables_json",    toJson(new ArrayList<>(s.getTargetTables().values())))
                        .set("is_union",              s.isUnion())
                        .set("is_dml",                isDml(s.getType()))
                        .set("is_ddl",                isDdl(s.getType()))
                        .set("has_aggregation",       s.isHasAggregation())
                        .set("has_window",            s.isHasWindow())
                        .set("has_cte",               hasCte(s, str.getStatements()))
                        .set("join_count",            s.getJoins().size())
                        .set("col_count_output",      s.getColumnsOutput().size())
                        .set("col_count_input",       countInputColumns(s))
                        .set("depth",                 computeDepth(s.getParentStatementGeoid(), str.getStatements()))
                        .set("quality",               computeStatementQuality(s))
                        .save();
                stV.put(e.getKey(), v);
                sessionV.newEdge("BELONGS_TO_SESSION", v, true, new Object[0]);

                // DaliSnippet — per-statement text + line range
                String snip = truncate(s.getSnippet(), SNIPPET_MAX);
                if (snip != null) {
                    db.newDocument("DaliSnippet")
                            .set("session_id",   sid)
                            .set("stmt_geoid",   e.getKey())
                            .set("snippet",      snip)
                            .set("snippet_hash", md5(snip))
                            .set("line_start",   s.getLineStart())
                            .set("line_end",     s.getLineEnd())
                            .save();
                }
            }

            // DaliSnippetScript — full raw file text (v22)
            String rawScript = result.getRawScript();
            if (rawScript != null && !rawScript.isBlank()) {
                int lineCount = (int) rawScript.lines().count();
                db.newDocument("DaliSnippetScript")
                        .set("session_id",  sid)
                        .set("file_path",   result.getFilePath())
                        .set("script",      rawScript)
                        .set("script_hash", md5(rawScript))
                        .set("line_count",  lineCount)
                        .set("char_count",  rawScript.length())
                        .save();
            }

            // ── DaliOutputColumn / DaliJoin / DaliAffectedColumn + edges ──
            Map<String, MutableVertex> ocByKey       = new LinkedHashMap<>();
            Map<String, MutableVertex> ocByStmtAndName = new LinkedHashMap<>();
            Map<String, MutableVertex> affColByRef   = new LinkedHashMap<>();

            for (var e : str.getStatements().entrySet()) {
                StatementInfo s = e.getValue();
                MutableVertex sv = stV.get(e.getKey());
                if (sv == null) continue;

                if (s.getParentStatementGeoid() != null) {
                    MutableVertex pv = stV.get(s.getParentStatementGeoid());
                    if (pv != null) sv.newEdge("CHILD_OF", pv, true, new Object[0]);
                }
                if (s.getRoutineGeoid() != null) {
                    MutableVertex rv = rtV.get(s.getRoutineGeoid());
                    if (rv != null) rv.newEdge("CONTAINS_STMT", sv, true, new Object[0]);
                }

                for (var st : s.getSourceTables().entrySet()) {
                    MutableVertex tv = tblV.get(st.getKey());
                    if (tv != null) {
                        @SuppressWarnings("unchecked")
                        List<String> aliases = st.getValue().get("table_alias") instanceof List
                                ? (List<String>) st.getValue().get("table_alias") : List.of();
                        sv.newEdge("READS_FROM", tv, true, new Object[0])
                                .set("aliases", new ArrayList<>(aliases))
                                .set("session_id", sid).save();
                    }
                }
                for (var tt : s.getTargetTables().entrySet()) {
                    MutableVertex tv = tblV.get(tt.getKey());
                    if (tv != null) {
                        @SuppressWarnings("unchecked")
                        List<String> aliases = tt.getValue().get("table_alias") instanceof List
                                ? (List<String>) tt.getValue().get("table_alias") : List.of();
                        sv.newEdge("WRITES_TO", tv, true, new Object[0])
                                .set("aliases", new ArrayList<>(aliases))
                                .set("session_id", sid).save();
                    }
                }
                for (var sq : s.getSourceSubqueries().entrySet()) {
                    MutableVertex sqV = stV.get(sq.getKey());
                    if (sqV != null) {
                        sv.newEdge("USES_SUBQUERY", sqV, true, new Object[0])
                                .set("aliases",       new ArrayList<>(sq.getValue().subqueryAliases()))
                                .set("subquery_type", sq.getValue().subqueryType())
                                .set("session_id",    sid)
                                .save();
                    }
                }

                for (var oc : s.getColumnsOutput().entrySet()) {
                    Map<String, Object> col = oc.getValue();
                    MutableVertex ocV = db.newVertex("DaliOutputColumn")
                            .set("session_id",      sid)
                            .set("statement_geoid", e.getKey())
                            .set("col_key",         oc.getKey())
                            .set("name",            col.get("name"))
                            .set("expression",      col.get("expression"))
                            .set("alias",           col.get("alias"))
                            .set("col_order",       col.get("order"))
                            .set("source_type",     col.get("source_type"))
                            .set("table_ref",       col.get("table_ref"))
                            .save();
                    sv.newEdge("HAS_OUTPUT_COL", ocV, true, new Object[0])
                            .set("session_id",      sid)
                            .set("statement_geoid", e.getKey())
                            .save();
                    Object order = col.get("order");
                    if (order != null) ocByKey.put(e.getKey() + ":" + order, ocV);
                    String ocName = (String) col.get("name");
                    if (ocName != null) ocByStmtAndName.put(e.getKey() + ":" + ocName.toUpperCase(), ocV);
                }

                for (JoinInfo j : s.getJoins()) {
                    MutableVertex jV = db.newVertex("DaliJoin")
                            .set("session_id",         sid)
                            .set("statement_geoid",    e.getKey())
                            .set("join_type",          j.joinType())
                            .set("source_table_geoid", j.sourceTableGeoid())
                            .set("source_alias",       j.sourceTableAlias())
                            .set("source_type",        j.sourceType())
                            .set("target_table_geoid", j.targetTableGeoid())
                            .set("target_alias",       j.targetTableAlias())
                            .set("target_type",        j.targetType())
                            .set("conditions",         j.conditions())
                            .set("line_start",         j.lineStart())
                            .save();
                    sv.newEdge("HAS_JOIN", jV, true, new Object[0])
                            .set("session_id",      sid)
                            .set("statement_geoid", e.getKey())
                            .save();
                    if (j.sourceTableGeoid() != null) {
                        MutableVertex srcTbl = tblV.get(j.sourceTableGeoid());
                        if (srcTbl != null) jV.newEdge("JOIN_SOURCE_TABLE", srcTbl, true, new Object[0])
                                .set("session_id", sid).save();
                    }
                    if (j.targetTableGeoid() != null) {
                        MutableVertex tgtTbl = tblV.get(j.targetTableGeoid());
                        if (tgtTbl != null) jV.newEdge("JOIN_TARGET_TABLE", tgtTbl, true, new Object[0])
                                .set("session_id", sid).save();
                    }
                }

                for (Map<String, Object> ac : s.getAffectedColumns()) {
                    String typeAffect  = null;
                    Integer orderAffect = null;
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> poliage = (List<Map<String, Object>>) ac.get("poliage_update");
                    if (poliage != null && !poliage.isEmpty()) {
                        typeAffect  = (String)  poliage.get(0).get("type_affect");
                        Object oa   = poliage.get(0).get("order_affect");
                        orderAffect = oa instanceof Number n ? n.intValue() : null;
                    }
                    MutableVertex acV = db.newVertex("DaliAffectedColumn")
                            .set("session_id",        sid)
                            .set("statement_geoid",   e.getKey())
                            .set("column_ref",        ac.get("column_ref"))
                            .set("column_name",       ac.get("column_name"))
                            .set("table_geoid",       ac.get("table_geoid"))
                            .set("dataset_alias",     ac.get("dataset_alias"))
                            .set("source_type",       ac.get("source_type"))
                            .set("resolution_status", ac.get("resolution_status"))
                            .set("type_affect",       typeAffect)
                            .set("order_affect",      orderAffect)
                            .save();
                    sv.newEdge("HAS_AFFECTED_COL", acV, true, new Object[0]).set("session_id", sid).save();
                    String acColRef = (String) ac.get("column_ref");
                    if (acColRef != null) affColByRef.put(e.getKey() + ":" + acColRef, acV);
                    String acTblGeoid = (String) ac.get("table_geoid");
                    if (acTblGeoid != null) {
                        MutableVertex tblRef = tblV.get(acTblGeoid);
                        if (tblRef != null) acV.newEdge("AFFECTED_COL_REF_TABLE", tblRef, true, new Object[0]);
                    }
                }
            }

            // ── DaliAtom + resolution edges ──
            for (var container : result.getAtoms().entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cont = (Map<String, Object>) container.getValue();
                String stmtGeoid = (String) cont.get("source_geoid");
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> atoms =
                        (Map<String, Map<String, Object>>) cont.get("atoms");
                if (atoms == null || stmtGeoid == null) continue;
                MutableVertex sv = stV.get(stmtGeoid);
                if (sv == null) continue;

                for (var at : atoms.entrySet()) {
                    Map<String, Object> a = at.getValue();
                    String atomId = md5(stmtGeoid + ":" + at.getKey());
                    // Detect AtomInfo.STATUS_UNBOUND for column-reference atoms whose DaliColumn is absent from schema
                    String atomColForWarn = (String) a.get("column_name");
                    String atomTblForWarn = (String) a.get("table_geoid");
                    boolean isColRef = Boolean.TRUE.equals(a.get("is_column_reference"));
                    if (a.get("warning") == null && AtomInfo.STATUS_RESOLVED.equals(a.get("status"))
                            && isColRef && atomTblForWarn != null && atomColForWarn != null
                            && colV.get(atomTblForWarn + "." + atomColForWarn.toUpperCase()) == null) {
                        a.put("warning", AtomInfo.STATUS_UNBOUND);
                    }

                    MutableVertex aV = db.newVertex("DaliAtom")
                            .set("session_id",            sid)
                            .set("statement_geoid",       stmtGeoid)
                            .set("atom_id",               atomId)
                            .set("atom_text",             at.getKey())
                            .set("atom_context",          a.get("atom_context"))
                            .set("parent_context",        a.get("parent_context"))
                            .set("position",              a.get("position"))
                            .set("sposition",             a.get("sposition"))
                            .set("is_complex",            a.get("is_complex"))
                            .set("is_column_reference",   a.get("is_column_reference"))
                            .set("is_function_call",      a.get("is_function_call"))
                            .set("is_constant",           a.get("is_constant"))
                            .set("is_routine_param",      a.get("is_routine_param"))
                            .set("is_routine_var",        a.get("is_routine_var"))
                            .set("table_name",            a.get("table_name"))
                            .set("column_name",           a.get("column_name"))
                            .set("table_geoid",           a.get("table_geoid"))
                            .set("status",                a.get("status"))
                            .set("warning",               a.get("warning"))
                            .set("merge_clause",          a.get("merge_clause"))
                            .set("output_column_sequence",a.get("output_column_sequence"))
                            .set("nested_atoms_count",    a.get("nested_atoms_count"))
                            .save();
                    sv.newEdge("HAS_ATOM", aV, true, new Object[0]);

                    String atomTableGeoid = (String) a.get("table_geoid");
                    if (atomTableGeoid != null) {
                        MutableVertex atbl = tblV.get(atomTableGeoid);
                        if (atbl != null) {
                            aV.newEdge("ATOM_REF_TABLE", atbl, true, new Object[0]);
                            String atomColName = (String) a.get("column_name");
                            if (atomColName != null) {
                                MutableVertex acol = colV.get(atomTableGeoid + "." + atomColName.toUpperCase());
                                if (acol != null) aV.newEdge("ATOM_REF_COLUMN", acol, true, new Object[0]);
                            }
                        } else {
                            MutableVertex astmt = stV.get(atomTableGeoid);
                            if (astmt != null) {
                                aV.newEdge("ATOM_REF_STMT", astmt, true, new Object[0]);
                                String atomColName = (String) a.get("column_name");
                                if (atomColName != null) {
                                    MutableVertex aoc = ocByStmtAndName.get(
                                            atomTableGeoid + ":" + atomColName.toUpperCase());
                                    if (aoc != null) aV.newEdge("ATOM_REF_OUTPUT_COL", aoc, true, new Object[0]);
                                }
                            }
                        }
                    }

                    Object outSeq = a.get("output_column_sequence");
                    if (outSeq != null) {
                        MutableVertex ocV2 = ocByKey.get(stmtGeoid + ":" + outSeq);
                        if (ocV2 != null) aV.newEdge("ATOM_PRODUCES", ocV2, true, new Object[0])
                                .set("session_id", sid).save();
                    }
                    // ATOM_PRODUCES for DML target (UPDATE SET / MERGE / INSERT)
                    String dmlTargetRefProd = (String) a.get("dml_target_ref");
                    if (dmlTargetRefProd != null) {
                        MutableVertex acV2 = affColByRef.get(stmtGeoid + ":" + dmlTargetRefProd);
                        if (acV2 != null) {
                            String mergeClause = (String) a.get("merge_clause");
                            var prodEdge = aV.newEdge("ATOM_PRODUCES", acV2, true, new Object[0])
                                    .set("session_id", sid);
                            if (mergeClause != null) prodEdge.set("merge_clause", mergeClause);
                            prodEdge.save();
                        }
                    }
                    if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && outSeq != null) {
                        String atomColName2 = (String) a.get("column_name");
                        if (atomTableGeoid != null && atomColName2 != null) {
                            MutableVertex srcCol = colV.get(atomTableGeoid + "." + atomColName2.toUpperCase());
                            MutableVertex tgtOcV = ocByKey.get(stmtGeoid + ":" + outSeq);
                            if (srcCol != null && tgtOcV != null) {
                                srcCol.newEdge("DATA_FLOW", tgtOcV, true, new Object[0])
                                        .set("session_id",      sid)
                                        .set("statement_geoid", stmtGeoid)
                                        .set("atom_id",         atomId)
                                        .set("flow_type",       resolveFlowType(a, str.getStatements().get(stmtGeoid)))
                                        .save();
                            }
                        }
                    }
                    String dmlTargetRef = (String) a.get("dml_target_ref");
                    if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && dmlTargetRef != null && atomTableGeoid != null) {
                        String atomColName3 = (String) a.get("column_name");
                        if (atomColName3 != null) {
                            MutableVertex srcCol = colV.get(atomTableGeoid + "." + atomColName3.toUpperCase());
                            MutableVertex tgtAcV = affColByRef.get(stmtGeoid + ":" + dmlTargetRef);
                            if (srcCol != null && tgtAcV != null) {
                                srcCol.newEdge("DATA_FLOW", tgtAcV, true, new Object[0])
                                        .set("session_id",      sid)
                                        .set("statement_geoid", stmtGeoid)
                                        .set("atom_id",         atomId)
                                        .set("flow_type",       resolveDmlFlowType(str.getStatements().get(stmtGeoid)))
                                        .save();
                            }
                        }
                    }
                    String parentCtx = (String) a.get("parent_context");
                    if (("WHERE".equals(parentCtx) || "HAVING".equals(parentCtx) || "JOIN".equals(parentCtx))
                            && AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && atomTableGeoid != null) {
                        String atomColName = (String) a.get("column_name");
                        if (atomColName != null) {
                            MutableVertex colVt = colV.get(atomTableGeoid + "." + atomColName.toUpperCase());
                            if (colVt != null) {
                                colVt.newEdge("FILTER_FLOW", sv, true, new Object[0])
                                        .set("filter_type",     parentCtx)
                                        .set("session_id",      sid)
                                        .set("statement_geoid", stmtGeoid)
                                        .set("via_atom",        at.getKey())
                                        .save();
                            }
                        }
                    }
                }
            }

            // ── DaliResolutionLog ──
            for (Map<String, Object> logEntry : result.getResolutionLog()) {
                db.newDocument("DaliResolutionLog")
                        .set("session_id",       sid)
                        .set("file_path",        result.getFilePath())
                        .set("statement_geoid",  logEntry.get("statement_geoid"))
                        .set("raw_input",        logEntry.get("raw_input"))
                        .set("result_kind",      logEntry.get("result_kind"))
                        .set("is_function_call", logEntry.get("is_function_call"))
                        .set("atom_context",     logEntry.get("atom_context"))
                        .set("parent_context",   logEntry.get("parent_context"))
                        .set("note",             logEntry.get("note"))
                        .set("strategy",         logEntry.get("strategy"))
                        .set("table_name",       logEntry.get("table_name"))
                        .set("column_name",      logEntry.get("column_name"))
                        .set("position",         logEntry.get("position"))
                        .save();
            }

            // ── DaliSchemaLog ──
            for (Map<String, Object> schEntry : result.getSchemaRegistrationLog()) {
                db.newDocument("DaliSchemaLog")
                        .set("session_id",  sid)
                        .set("schema_name", schEntry.get("schema_name"))
                        .set("reason",      schEntry.get("reason"))
                        .set("backtrace",   schEntry.get("backtrace"))
                        .save();
            }

            // ── CALLS edges ──
            for (var callerEntry : result.getCalledRoutines().entrySet()) {
                MutableVertex callerV = rtV.get(callerEntry.getKey());
                if (callerV == null) continue;
                for (Map<String, String> call : callerEntry.getValue()) {
                    String calleeName = call.get("name");
                    if (calleeName == null) continue;
                    MutableVertex calleeV = null;
                    for (var rtEntry : rtV.entrySet()) {
                        String geoid = rtEntry.getKey().toUpperCase();
                        if (geoid.endsWith(":" + calleeName.toUpperCase())
                                || geoid.equals(calleeName.toUpperCase())) {
                            calleeV = rtEntry.getValue();
                            break;
                        }
                    }
                    // External call: callee not defined in this session →
                    // find-or-create a reconstructed stub so the CALLS edge has a target.
                    if (calleeV == null) {
                        String stubGeoid = "EXT:" + calleeName.toUpperCase();
                        var rsStub = db.query("sql",
                                "SELECT FROM DaliRoutine WHERE routine_geoid = :g AND session_id = :s LIMIT 1",
                                Map.of("g", stubGeoid, "s", sid));
                        if (rsStub.hasNext()) {
                            calleeV = (MutableVertex) rsStub.next().toElement();
                        } else {
                            calleeV = db.newVertex("DaliRoutine")
                                    .set("session_id",    sid)
                                    .set("routine_geoid", stubGeoid)
                                    .set("routine_name",  calleeName.toUpperCase())
                                    .set("routine_type",  "UNKNOWN")
                                    .set("data_source",   RECONSTRUCTED)
                                    .save();
                        }
                        rtV.put(stubGeoid, calleeV);
                    }
                    if (calleeV != null) {
                        callerV.newEdge("CALLS", calleeV, true, new Object[0])
                                .set("session_id",   sid)
                                .set("caller_geoid", callerEntry.getKey())
                                .set("callee_name",  calleeName)
                                .set("line_start",   call.get("line"))
                                .save();
                    }
                }
            }

            // ── Lineage edges ──
            for (LineageEdge le : result.getLineage()) {
                MutableVertex from = resolve(le.sourceGeoid(), stV, tblV, rtV);
                MutableVertex to   = resolve(le.targetGeoid(), stV, tblV, rtV);
                if (from != null && to != null) {
                    from.newEdge(le.relationType(), to, true, new Object[0])
                            .set("session_id",      sid)
                            .set("statement_geoid", le.statementGeoid())
                            .save();
                }
            }
        });
        timer.stop("write.vtx");
    }

    // ═══════════════════════════════════════════════════════════════
    // writePerfStats — embedded half
    // ═══════════════════════════════════════════════════════════════

    void writePerfStats(String sid, SemanticResult result, PipelineTimer timer,
                        String dbName,
                        int cntTables, int cntColumns, int cntStatements, int cntRoutines,
                        int cntAtoms, int cntJoins, int cntOutputCols, int cntLineage,
                        int atomResolved, int atomConst, int atomFunc, int atomFailed) {
        try {
            db.transaction(() ->
                db.newDocument("DaliPerfStats")
                    .set("session_id",         sid)
                    .set("db_name",            dbName)
                    .set("file_path",          result.getFilePath())
                    .set("dialect",            result.getDialect())
                    .set("created_at",         System.currentTimeMillis())
                    .set("ms_parse",           timer.ms("parse"))
                    .set("ms_walk",            timer.ms("walk"))
                    .set("ms_resolve",         timer.ms("resolve"))
                    .set("ms_write_vtx",       timer.ms("write.vtx"))
                    .set("ms_write_edge",      timer.ms("write.edge"))
                    .set("ms_total",           timer.totalMs())
                    .set("count_tokens",       timer.count("tokens"))
                    .set("cnt_tables",         cntTables)
                    .set("cnt_columns",        cntColumns)
                    .set("cnt_statements",     cntStatements)
                    .set("cnt_routines",       cntRoutines)
                    .set("cnt_atoms",          cntAtoms)
                    .set("cnt_atoms_resolved", atomResolved)
                    .set("cnt_atoms_const",    atomConst)
                    .set("cnt_atoms_func",     atomFunc)
                    .set("cnt_atoms_failed",   atomFailed)
                    .set("cnt_output_cols",    cntOutputCols)
                    .set("cnt_joins",          cntJoins)
                    .set("cnt_lineage",        cntLineage)
                    .save()
            );
        } catch (Exception e) {
            logger.warn("DaliPerfStats write failed for {}: {}", sid, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // cleanAll — embedded half
    // ═══════════════════════════════════════════════════════════════

    void cleanAll(String[] edgeTypes, String[] vtxTypes, String[] docTypes) {
        for (String t : edgeTypes) deleteType(t);
        for (String t : vtxTypes)  deleteType(t);
        for (String t : docTypes)  deleteType(t);
    }

    private int deleteType(String typeName) {
        try {
            if (db.getSchema().existsType(typeName)) {
                db.transaction(() -> db.command("sql", "DELETE FROM " + typeName));
                return 1;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    @SafeVarargs
    private MutableVertex resolve(String geoid, Map<String, MutableVertex>... maps) {
        if (geoid == null) return null;
        for (var m : maps) { MutableVertex v = m.get(geoid); if (v != null) return v; }
        return null;
    }
}
