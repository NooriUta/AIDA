// File: src/main/java/com/hound/storage/JsonlBatchBuilder.java
package com.hound.storage;

import com.hound.semantic.model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Собирает NDJSON payload для ArcadeDB HTTP Batch endpoint.
 *
 * <p>Правила:
 * <ol>
 *   <li>Сначала все вершины ({@link #appendVertex} / {@link #appendDocument})
 *   <li>Потом все рёбра ({@link #appendEdge})
 *   <li>{@code @id} для вершин = geoid (уникален в рамках сессии)
 *   <li>{@code @from} / {@code @to} в рёбрах = geoid соответствующих вершин
 * </ol>
 */
public class JsonlBatchBuilder {

    private final StringBuilder vertices = new StringBuilder(64 * 1024);
    private final StringBuilder edges    = new StringBuilder(32 * 1024);

    /** Accumulates per-type insert/duplicate counts for all vertices and edges. */
    private final WriteStats writeStats = new WriteStats();

    /**
     * Tracks all vertex @id values added to this batch.
     * appendEdge() guards against referencing vertices not in this batch,
     * which would cause ArcadeDB batch endpoint to reject with "Unknown temporary ID".
     */
    private final Set<String> vertexIds = new HashSet<>();

    /**
     * Canonical object RIDs: geoid → actual DB RID (e.g. "#15:3").
     * Used in namespace/pool mode when canonical vertices (DaliSchema, DaliTable, DaliColumn)
     * are pre-inserted via REMOTE rcmd() and not included in the batch payload.
     * appendEdge() uses these RIDs directly in @from/@to when an endpoint is not in vertexIds.
     */
    private Map<String, String> canonicalRids = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Adds a vertex line. Skips silently if extId was already added (deduplication).
     * saveRemote() uses UPSERT; batch mode must deduplicate manually to avoid duplicate vertices.
     */
    public void appendVertex(String type, String extId, Map<String, Object> props) {
        if (extId != null && !vertexIds.add(extId)) {
            writeStats.markDuplicate(type); // in-batch dedup guard triggered
            return;
        }
        vertices.append(toJsonLine("vertex", type, extId, null, null, props)).append('\n');
        writeStats.insert(type);
    }

    /**
     * Document types (DaliSnippet, DaliSnippetScript, DaliResolutionLog, DaliSchemaLog) are
     * skipped in batch mode: ArcadeDB /api/v1/batch only accepts "vertex" and "edge" @type values.
     * These types have no edges and are diagnostic-only, so graph integrity is unaffected.
     *
     * <p>DaliSnippet intentionally stays a DOCUMENT: large SQL texts (1 000s of lines) would
     * cause memory pressure on any TRAVERSE operation that loads the full vertex payload, and
     * risk batch-endpoint payload overflow. Written via {@code rcmd()} in RemoteWriter.
     */
    public void appendDocument(String type, Map<String, Object> props) {
        // no-op: batch endpoint rejects @type "document"
    }

    /**
     * Adds an edge line. Skipped if either endpoint is null or not resolvable —
     * ArcadeDB batch endpoint rejects edges referencing unknown temporary IDs.
     *
     * <p>Resolution order for each endpoint:
     * <ol>
     *   <li>If the geoid is in {@code vertexIds}: use it as a batch-local temporary ID.
     *   <li>If the geoid is in {@code canonicalRids}: use the actual DB RID (e.g. {@code #15:3}).
     *   <li>Otherwise: skip the edge.
     * </ol>
     */
    public void appendEdge(String edgeType, String fromExtId, String toExtId, Map<String, Object> props) {
        if (fromExtId == null || toExtId == null) { writeStats.dropEdge(); return; }
        String resolvedFrom = resolveEndpoint(fromExtId);
        String resolvedTo   = resolveEndpoint(toExtId);
        if (resolvedFrom == null || resolvedTo == null) { writeStats.dropEdge(); return; }
        edges.append(toJsonLine("edge", edgeType, null, resolvedFrom, resolvedTo, props)).append('\n');
        writeStats.addEdge();
    }

    /**
     * Resolves an edge endpoint to either a batch-local temp ID or an actual DB RID.
     * Returns null if the endpoint cannot be resolved.
     */
    private String resolveEndpoint(String geoid) {
        if (vertexIds.contains(geoid)) return geoid;
        String rid = canonicalRids.get(geoid);
        return rid; // null if not found in either map
    }

    /** Builds final payload: all vertices then all edges (NDJSON). */
    public String build() {
        StringBuilder sb = new StringBuilder(vertices.length() + edges.length());
        sb.append(vertices);
        sb.append(edges);
        return sb.toString();
    }

    // ── Counters (derived from writeStats) ────────────────────────────────────

    public int vertexCount()      { return writeStats.totalInserted(); }
    public int edgeCount()        { return writeStats.edgeCount(); }
    public int droppedEdgeCount() { return writeStats.droppedEdgeCount(); }

    /** Full per-type breakdown (inserted + duplicate). */
    public WriteStats writeStats() { return writeStats; }

    /**
     * Records a canonical vertex that was inserted into YGG via individual rcmd()
     * before this batch (new object, not seen before in the pool/db).
     */
    public void recordInserted(String type)  { writeStats.insert(type); }

    /**
     * Records a canonical vertex that was skipped because it already exists in YGG
     * (pool hit, pre-existing schema/table/column, or explicitly excluded from this batch).
     */
    public void recordDuplicate(String type) { writeStats.markDuplicate(type); }

    // ═══════════════════════════════════════════════════════════════
    // Factory: SemanticResult → NDJSON builder
    // ═══════════════════════════════════════════════════════════════

    private static final int SNIPPET_MAX = 4000;

    /**
     * Builds a complete NDJSON payload from a {@link SemanticResult}.
     * Vertex order mirrors {@code saveRemote()} in {@link ArcadeDBSemanticWriter}.
     */
    public static JsonlBatchBuilder buildFromResult(String sid, SemanticResult result) {
        return buildFromResult(sid, result, null);
    }

    /**
     * Ad-hoc batch mode with schema deduplication support.
     *
     * <p>If {@code preInsertedSchemaRids} is non-null and non-empty, those DaliSchema vertices
     * have already been inserted into ArcadeDB via individual rcmd() calls before this batch.
     * They are skipped from the NDJSON payload (to avoid DuplicatedKeyException) and their
     * actual DB RIDs are registered in canonicalRids so edges can reference them correctly.
     *
     * @param preInsertedSchemaRids schema_geoid → actual DB RID for pre-existing DaliSchema vertices,
     *                              or null/empty to include all schemas in the batch as usual.
     */
    public static JsonlBatchBuilder buildFromResult(String sid, SemanticResult result,
                                                    Map<String, String> preInsertedSchemaRids) {
        JsonlBatchBuilder b = new JsonlBatchBuilder();
        // Register pre-existing schema RIDs so edges resolve to actual DB RIDs, not temp IDs
        if (preInsertedSchemaRids != null && !preInsertedSchemaRids.isEmpty()) {
            b.canonicalRids.putAll(preInsertedSchemaRids);
        }
        Structure str = result.getStructure();
        if (str == null) return b;

        Map<String, Object> sidProps = Map.of("session_id", sid);

        // ─────────────────────────────────────────────────────
        // Phase 1: Vertices (strict order)
        // ─────────────────────────────────────────────────────

        // 1. DaliSession
        b.appendVertex("DaliSession", sid, mapOf(
                "session_id", sid,
                "file_path", nvl(result.getFilePath()),
                "dialect", nvl(result.getDialect()),
                "processing_time_ms", result.getProcessingTimeMs(),
                "created_at", System.currentTimeMillis()
        ));

        // 2. DaliDatabase
        for (var e : str.getDatabases().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = (Map<String, Object>) e.getValue();
            b.appendVertex("DaliDatabase", e.getKey(), mapOf(
                    "session_id", sid,
                    "db_geoid", e.getKey(),
                    "db_name", d.get("name")
            ));
        }

        // 3. DaliSchema — skip if geoid is in canonicalRids (pre-existing in DB)
        // NOTE: schema new/dup split is tracked externally by RemoteWriter (preInsertAdHocSchemas).
        // Here we only skip — no recordDuplicate to avoid double-counting.
        for (var e : str.getSchemas().entrySet()) {
            if (b.canonicalRids.containsKey(e.getKey())) continue; // already in DB, use RID for edges
            @SuppressWarnings("unchecked")
            Map<String, Object> sc = (Map<String, Object>) e.getValue();
            String dbGeoid = (String) sc.get("db");
            @SuppressWarnings("unchecked")
            String dbName = (dbGeoid != null && str.getDatabases().containsKey(dbGeoid))
                    ? (String) ((Map<String, Object>) str.getDatabases().get(dbGeoid)).get("name")
                    : dbGeoid;
            b.appendVertex("DaliSchema", e.getKey(), mapOf(
                    "session_id", sid,
                    "schema_geoid", e.getKey(),
                    "schema_name", sc.get("name"),
                    "db_name",   dbName,
                    "db_geoid",  dbGeoid
            ));
        }

        // 4. DaliPackage
        for (var e : str.getPackages().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = (Map<String, Object>) e.getValue();
            b.appendVertex("DaliPackage", e.getKey(), mapOf(
                    "session_id", sid,
                    "package_geoid", e.getKey(),
                    "package_name", pkg.get("package_name"),
                    "schema_geoid", pkg.get("schema_geoid"),
                    "routine_geoid", e.getKey(),
                    "routine_name", pkg.get("package_name"),
                    "routine_type", "PACKAGE"
            ));
        }

        // 5. DaliTable — skip if geoid is in canonicalRids (pre-existing in DB)
        for (var e : str.getTables().entrySet()) {
            if (b.canonicalRids.containsKey(e.getKey())) { b.recordDuplicate("DaliTable"); continue; }
            TableInfo t = e.getValue();
            // data_source: 'master' if defined by DDL in this session, else 'reconstructed'
            String tblDs = WriteHelpers.isMasterTable(e.getKey(), str)
                    ? WriteHelpers.MASTER : WriteHelpers.RECONSTRUCTED;
            // CREATE VIEW overrides any default 'TABLE' type assigned during reconstruction
            String effectiveType = WriteHelpers.isViewTable(e.getKey(), str) ? "VIEW" : t.tableType();
            b.appendVertex("DaliTable", e.getKey(), mapOf(
                    "session_id", sid,
                    "table_geoid", e.getKey(),
                    "table_name", t.tableName(),
                    "schema_geoid", t.schemaGeoid(),
                    "table_type", effectiveType,
                    "column_count", t.columnCount(),
                    "data_source", tblDs
            ));
        }

        // 6. DaliColumn — skip if geoid is in canonicalRids (pre-existing in DB)
        for (var e : str.getColumns().entrySet()) {
            if (b.canonicalRids.containsKey(e.getKey())) { b.recordDuplicate("DaliColumn"); continue; }
            ColumnInfo c = e.getValue();
            // data_source: inherit from the table — master if the table is DDL-defined
            String colDs = WriteHelpers.isMasterTable(c.getTableGeoid(), str)
                    ? WriteHelpers.MASTER : WriteHelpers.RECONSTRUCTED;
            b.appendVertex("DaliColumn", e.getKey(), mapOf(
                    "session_id", sid,
                    "column_geoid", e.getKey(),
                    "table_geoid", c.getTableGeoid(),
                    "column_name", c.getColumnName(),
                    "expression", c.getExpression(),
                    "alias", c.getAlias(),
                    "is_output", c.isOutput(),
                    "col_order", c.getOrder(),
                    "ordinal_position", c.getOrdinalPosition(),
                    "data_source", colDs,
                    "data_type", c.getDataType(),
                    "is_required", c.isRequired(),
                    "default_value", c.getDefaultValue(),
                    "is_pk", c.isPk(),
                    "is_fk", c.isFk(),
                    "fk_ref_table", c.getFkRefTable(),
                    "fk_ref_column", c.getFkRefColumn()
            ));
        }

        // 7. DaliRoutine (or DaliPackage if geoid is in packages map)
        Set<String> packageGeoids = str.getPackages().keySet();
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            // If this routine's geoid matches a package entry, skip (already emitted as DaliPackage)
            if (packageGeoids.contains(e.getKey())) continue;
            b.appendVertex("DaliRoutine", e.getKey(), mapOf(
                    "session_id", sid,
                    "routine_geoid", e.getKey(),
                    "routine_name", r.getName(),
                    "routine_type", r.getRoutineType(),
                    "package_geoid", r.getPackageGeoid(),
                    "schema_geoid", r.getSchemaGeoid(),
                    "is_pipelined", r.isPipelined() ? true : null,  // KI-PIPE-1
                    "autonomous_transaction", r.isAutonomousTransaction() ? true : null  // KI-PRAGMA-1
            ));
        }

        // 7b. DaliParameter, DaliVariable
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            int pIdx = 0;
            for (RoutineInfo.ParameterInfo p : r.getTypedParameters()) {
                b.appendVertex("DaliParameter", e.getKey() + ":PARAM:" + pIdx, mapOf(
                        "session_id", sid,
                        "routine_geoid", e.getKey(),
                        "param_name", p.name(),
                        "param_type", p.type(),
                        "param_mode", p.mode()
                ));
                pIdx++;
            }
            int vIdx = 0;
            for (RoutineInfo.VariableInfo v : r.getTypedVariables()) {
                b.appendVertex("DaliVariable", e.getKey() + ":VAR:" + vIdx, mapOf(
                        "session_id", sid,
                        "routine_geoid", e.getKey(),
                        "var_name", v.name(),
                        "var_type", v.type()
                ));
                vIdx++;
            }
        }

        // 8. DaliStatement — DDL (CREATE/ALTER/DROP) are excluded: they go to DaliDDLStatement only
        for (var e : str.getStatements().entrySet()) {
            StatementInfo s = e.getValue();
            if (isDdl(s.getType())) continue;  // DDL → DaliDDLStatement, not DaliStatement
            b.appendVertex("DaliStatement", e.getKey(), mapOf(
                    "session_id", sid,
                    "stmt_geoid", e.getKey(),
                    "type", s.getType(),
                    "subtype", s.getSubtype(),
                    "line_start", s.getLineStart(),
                    "line_end", s.getLineEnd(),
                    "parent_statement", s.getParentStatementGeoid(),
                    "parent_statement_type", parentType(s.getParentStatementGeoid(), str),
                    "routine_geoid", s.getRoutineGeoid(),
                    "short_name", s.getShortName(),
                    "is_union", s.isUnion(),
                    "is_dml", isDml(s.getType()),
                    "is_ddl", false,
                    "has_aggregation", s.isHasAggregation(),
                    "has_window", s.isHasWindow(),
                    "has_cte", hasCte(s, str.getStatements()),
                    "join_count", s.getJoins().size(),
                    "col_count_output", s.getColumnsOutput().size(),
                    "col_count_input", countInputColumns(s),
                    "depth", computeDepth(s.getParentStatementGeoid(), str.getStatements()),
                    "quality", computeStatementQuality(s)
            ));
        }

        // 8b. DaliDDLStatement (v27) — separate vertex for schema-mutating DDL
        // KNOWN ISSUE: DaliDDLStatement → DaliTable / DaliColumn edges deferred.
        for (var e : str.getStatements().entrySet()) {
            StatementInfo s = e.getValue();
            if (!isDdl(s.getType())) continue;
            b.appendVertex("DaliDDLStatement", "DDL_" + e.getKey(), mapOf(
                    "session_id", sid,
                    "stmt_geoid", e.getKey(),
                    "type", s.getType(),
                    "line_start", s.getLineStart(),
                    "line_end", s.getLineEnd(),
                    "target_table_geoids", WriteHelpers.toJson(new ArrayList<>(s.getTargetTables().keySet())),
                    "short_name", s.getShortName()
            ));
        }

        // 8c. DaliPrimaryKey / DaliForeignKey / DaliUniqueConstraint / DaliCheckConstraint
        // Note: edges (HAS_PRIMARY_KEY, IS_PK_COLUMN, HAS_FOREIGN_KEY, IS_FK_COLUMN,
        //       REFERENCES_TABLE, REFERENCES_COLUMN, HAS_UNIQUE_KEY, IS_UNIQUE_COLUMN,
        //       HAS_CHECK) are written by RemoteWriter post-batch.
        for (var e : str.getConstraints().entrySet()) {
            ConstraintInfo c = e.getValue();
            String colNamesJson = WriteHelpers.toJson(c.getColumnNames());
            if (c.isPrimaryKey()) {
                b.appendVertex("DaliPrimaryKey", e.getKey(), mapOf(
                        "session_id", sid,
                        "constraint_geoid", e.getKey(),
                        "constraint_type", c.getConstraintType(),
                        "constraint_name", c.getConstraintName(),
                        "table_geoid", c.getHostTableGeoid(),
                        "column_names", colNamesJson
                ));
            } else if (c.isForeignKey()) {
                b.appendVertex("DaliForeignKey", e.getKey(), mapOf(
                        "session_id", sid,
                        "constraint_geoid", e.getKey(),
                        "constraint_type", c.getConstraintType(),
                        "constraint_name", c.getConstraintName(),
                        "table_geoid", c.getHostTableGeoid(),
                        "column_names", colNamesJson,
                        "ref_table_geoid", c.getRefTableGeoid(),
                        "ref_column_names", WriteHelpers.toJson(c.getRefColumnNames()),
                        "on_delete", c.getOnDelete()
                ));
            } else if (c.isUniqueConstraint()) {
                // KI-005: DaliUniqueConstraint
                b.appendVertex("DaliUniqueConstraint", e.getKey(), mapOf(
                        "session_id", sid,
                        "constraint_geoid", e.getKey(),
                        "constraint_type", c.getConstraintType(),
                        "constraint_name", c.getConstraintName(),
                        "table_geoid", c.getHostTableGeoid(),
                        "column_names", colNamesJson
                ));
            } else if (c.isCheckConstraint()) {
                // KI-005: DaliCheckConstraint
                b.appendVertex("DaliCheckConstraint", e.getKey(), mapOf(
                        "session_id", sid,
                        "constraint_geoid", e.getKey(),
                        "constraint_type", c.getConstraintType(),
                        "constraint_name", c.getConstraintName(),
                        "table_geoid", c.getHostTableGeoid(),
                        "check_expression", c.getCheckExpression()
                ));
            }
        }

        // 9. DaliOutputColumn
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                Map<String, Object> col = oc.getValue();
                String ocExtId = e.getKey() + ":OC:" + oc.getKey();
                b.appendVertex("DaliOutputColumn", ocExtId, mapOf(
                        "session_id", sid,
                        "statement_geoid", e.getKey(),
                        "col_key", oc.getKey(),
                        "name", col.get("name"),
                        "expression", col.get("expression"),
                        "alias", col.get("alias"),
                        "col_order", col.get("order"),
                        "source_type", col.get("source_type"),
                        "table_ref", col.get("table_ref")
                ));
            }
        }

        // 9b. DaliAffectedColumn
        for (var e : str.getStatements().entrySet()) {
            int acIdx = 0;
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String typeAffect = null;
                Integer orderAffect = null;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> poliage = (List<Map<String, Object>>) ac.get("poliage_update");
                if (poliage != null && !poliage.isEmpty()) {
                    typeAffect  = (String)  poliage.get(0).get("type_affect");
                    Object oa   = poliage.get(0).get("order_affect");
                    orderAffect = oa instanceof Number n ? n.intValue() : null;
                }
                String acExtId = e.getKey() + ":AC:" + acIdx;
                b.appendVertex("DaliAffectedColumn", acExtId, mapOf(
                        "session_id", sid,
                        "statement_geoid", e.getKey(),
                        "column_ref", ac.get("column_ref"),
                        "column_name", ac.get("column_name"),
                        "table_geoid", ac.get("table_geoid"),
                        "dataset_alias", ac.get("dataset_alias"),
                        "source_type", ac.get("source_type"),
                        "resolution_status", ac.get("resolution_status"),
                        "type_affect", typeAffect,
                        "order_affect", orderAffect
                ));
                acIdx++;
            }
        }

        // 9c. DaliRecord (G6: BULK COLLECT targets)
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            b.appendVertex("DaliRecord", rec.getGeoid(), mapOf(
                    "session_id", sid,
                    "record_geoid", rec.getGeoid(),
                    "record_name", rec.getVarName(),
                    "routine_geoid", rec.getRoutineGeoid(),
                    "source_stmt_geoid", rec.getSourceStatementGeoid(),
                    "fields", String.join(",", rec.getFields())
            ));
        }

        // 9d. DaliRecordField (G6: one vertex per named field; mirrors RemoteWriter.java:924-931)
        //     Dedup within batch: same field_geoid may appear if multiple routines define records
        //     with identical names (edge case). HashSet guards duplicate vertex writes.
        {
            Set<String> insertedFieldGeoids = new HashSet<>();
            for (var e : str.getRecords().entrySet()) {
                RecordInfo rec = e.getValue();
                for (RecordInfo.FieldInfo fi : rec.getFieldInfos()) {
                    String fieldGeoid = rec.getGeoid() + ":FIELD:" + fi.name();
                    if (!insertedFieldGeoids.add(fieldGeoid)) continue; // dedup
                    b.appendVertex("DaliRecordField", fieldGeoid, mapOf(
                            "session_id",        sid,
                            "field_geoid",       fieldGeoid,
                            "field_name",        fi.name(),
                            "field_order",       fi.ordinalPosition(),
                            "record_geoid",      rec.getGeoid(),
                            "data_type",         fi.dataType(),          // null-ok: nullable for untyped collections
                            "ordinal_position",  fi.ordinalPosition(),
                            "source_column_geoid", fi.sourceColumnGeoid() // null-ok: populated for %ROWTYPE fields only
                    ));
                }
            }
        }

        // 10. DaliAtom
        // Build atomId map for edge creation later.
        // IMPORTANT: skip "routine" containers — they aggregate the same atoms already present
        // in "statement" containers. Including them would insert duplicate DaliAtom vertices
        // (different atomId = md5(routineGeoid:key) vs md5(stmtGeoid:key)) and inflate counts.
        // Only "statement" and "unattached" containers contain unique atoms.
        Map<String, String> atomIdMap = new LinkedHashMap<>(); // "stmtGeoid:atomText" → atomId
        for (var container : result.getAtoms().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cont = (Map<String, Object>) container.getValue();
            String sourceType = (String) cont.get("source_type");
            if ("routine".equals(sourceType)) continue; // duplicate view — skip
            String stmtGeoid = (String) cont.get("source_geoid");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) cont.get("atoms");
            if (atoms == null || stmtGeoid == null) continue;

            for (var at : atoms.entrySet()) {
                Map<String, Object> a = at.getValue();
                String atomId = md5(stmtGeoid + ":" + at.getKey());
                atomIdMap.put(stmtGeoid + ":" + at.getKey(), atomId);
                // Detect AtomInfo.STATUS_UNBOUND for column-reference atoms whose DaliColumn is absent from schema.
                // Only applies to physical-table atoms: SubQuery/CTE/MERGE-USING SELECT atoms resolve via
                // ATOM_REF_OUTPUT_COL (not DaliColumn), so absence from str.getColumns() is expected.
                String atomColForWarn1 = (String) a.get("column_name");
                String atomTblForWarn1 = (String) a.get("table_geoid");
                boolean isColRef1 = Boolean.TRUE.equals(a.get("is_column_reference"));
                String atomWarning1 = (String) a.get("warning");
                if (atomWarning1 == null && AtomInfo.STATUS_RESOLVED.equals(a.get("status"))
                        && isColRef1 && atomTblForWarn1 != null && atomColForWarn1 != null
                        && str.getTables().containsKey(atomTblForWarn1)
                        && !str.getColumns().containsKey(atomTblForWarn1 + "." + atomColForWarn1.toUpperCase())) {
                    atomWarning1 = AtomInfo.STATUS_UNBOUND;
                }

                b.appendVertex("DaliAtom", atomId, mapOf(
                        "session_id", sid,
                        "statement_geoid", stmtGeoid,
                        "atom_id", atomId,
                        "atom_text", at.getKey(),
                        "atom_context", a.get("atom_context"),
                        "parent_context", a.get("parent_context"),
                        "position", a.get("position"),
                        "sposition", a.get("sposition"),
                        "is_complex", a.get("is_complex"),
                        "is_column_reference", a.get("is_column_reference"),
                        "is_function_call", a.get("is_function_call"),
                        "is_constant", a.get("is_constant"),
                        "is_routine_param", a.get("is_routine_param"),
                        "is_routine_var", a.get("is_routine_var"),
                        "table_name", a.get("table_name"),
                        "column_name", a.get("column_name"),
                        "table_geoid", a.get("table_geoid"),
                        "status", a.get("status"),
                        "warning", atomWarning1,
                        "merge_clause", a.get("merge_clause"),
                        "output_column_sequence", a.get("output_column_sequence"),
                        "nested_atoms_count", a.get("nested_atoms_count")
                ));
            }
        }

        // 11. DaliJoin
        for (var e : str.getStatements().entrySet()) {
            int joinIdx = 0;
            for (JoinInfo j : e.getValue().getJoins()) {
                String joinExtId = e.getKey() + ":JOIN:" + j.lineStart() + ":" + joinIdx;
                b.appendVertex("DaliJoin", joinExtId, mapOf(
                        "session_id", sid,
                        "statement_geoid", e.getKey(),
                        "join_type", j.joinType(),
                        "source_table_geoid", j.sourceTableGeoid(),
                        "source_alias", j.sourceTableAlias(),
                        "source_type", j.sourceType(),
                        "target_table_geoid", j.targetTableGeoid(),
                        "target_alias", j.targetTableAlias(),
                        "target_type", j.targetType(),
                        "conditions", j.conditions(),
                        "line_start", j.lineStart()
                ));
                joinIdx++;
            }
        }

        // 12. DaliSnippet — NOT written here (v28+: VERTEX written via rcmd() post-batch in
        //     RemoteWriter so that the HAS_SNIPPET edge can be created in the same transaction).


        // ─────────────────────────────────────────────────────
        // Phase 2: Edges (strictly after all vertices)
        // ─────────────────────────────────────────────────────

        // Structural: CONTAINS_TABLE — only for tables NOT pre-inserted via rcmd().
        // If the table is in canonicalRids it was pre-inserted before this batch;
        // its CONTAINS_TABLE edge is created by RemoteWriter Phase 3 (namespace) or
        // ad-hoc Phase 3 — NOT here. This avoids duplicate edges on repeated parse runs.
        for (var e : str.getTables().entrySet()) {
            if (b.canonicalRids.containsKey(e.getKey())) continue;
            String sg = e.getValue().schemaGeoid();
            if (sg != null)
                b.appendEdge("CONTAINS_TABLE", sg, e.getKey(), sidProps);
        }

        // Structural: CONTAINS_ROUTINE (schema → package)
        for (var e : str.getPackages().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = (Map<String, Object>) e.getValue();
            String sg = (String) pkg.get("schema_geoid");
            if (sg != null && !sg.isEmpty())
                b.appendEdge("CONTAINS_ROUTINE", sg, e.getKey(), sidProps);
        }

        // Structural: CONTAINS_ROUTINE (package/schema → routine)
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            if (packageGeoids.contains(e.getKey())) continue; // skip package-as-routine
            String fromGeoid = null;
            if (r.getPackageGeoid() != null)           fromGeoid = r.getPackageGeoid();
            else if (r.getSchemaGeoid() != null)       fromGeoid = r.getSchemaGeoid();
            if (fromGeoid != null)
                b.appendEdge("CONTAINS_ROUTINE", fromGeoid, e.getKey(), sidProps);
        }

        // Structural: NESTED_IN (parent routine → child routine)
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            if (r.getParentRoutineGeoid() != null) {
                b.appendEdge("NESTED_IN", r.getParentRoutineGeoid(), e.getKey(), sidProps);
            }
        }

        // Structural: HAS_COLUMN — only for columns NOT pre-inserted via rcmd().
        // Pre-inserted columns (in canonicalRids) get HAS_COLUMN from RemoteWriter Phase 3.
        for (var e : str.getColumns().entrySet()) {
            if (b.canonicalRids.containsKey(e.getKey())) continue;
            b.appendEdge("HAS_COLUMN", e.getValue().getTableGeoid(), e.getKey(), sidProps);
        }

        // Structural: CONTAINS_STMT (routine → statement)
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getRoutineGeoid() != null)
                b.appendEdge("CONTAINS_STMT", e.getValue().getRoutineGeoid(), e.getKey(), sidProps);
        }

        // Structural: CHILD_OF (statement → parent statement)
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getParentStatementGeoid() != null)
                b.appendEdge("CHILD_OF", e.getKey(), e.getValue().getParentStatementGeoid(), sidProps);
        }

        // Structural: HAS_OUTPUT_COL
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                String ocExtId = e.getKey() + ":OC:" + oc.getKey();
                b.appendEdge("HAS_OUTPUT_COL", e.getKey(), ocExtId,
                        mapOf("session_id", sid, "statement_geoid", e.getKey()));
            }
        }

        // Structural: HAS_AFFECTED_COL
        for (var e : str.getStatements().entrySet()) {
            int acIdx = 0;
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String acExtId = e.getKey() + ":AC:" + acIdx;
                b.appendEdge("HAS_AFFECTED_COL", e.getKey(), acExtId, sidProps);
                // AFFECTED_COL_REF_TABLE
                String tg = (String) ac.get("table_geoid");
                if (tg != null)
                    b.appendEdge("AFFECTED_COL_REF_TABLE", acExtId, tg, sidProps);
                acIdx++;
            }
        }

        // Structural: BULK_COLLECTS_INTO, HAS_RECORD_FIELD, RECORD_USED_IN (G6: DaliRecord edges)
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            String recGeoid = rec.getGeoid();
            // DaliStatement(cursor SELECT) → BULK_COLLECTS_INTO → DaliRecord
            if (rec.getSourceStatementGeoid() != null)
                b.appendEdge("BULK_COLLECTS_INTO", rec.getSourceStatementGeoid(), recGeoid, sidProps);
            // DaliRecord → HAS_RECORD_FIELD → DaliRecordField (mirrors RemoteWriter.java:1448-1453)
            for (RecordInfo.FieldInfo fi : rec.getFieldInfos()) {
                String fieldGeoid = recGeoid + ":FIELD:" + fi.name();
                b.appendEdge("HAS_RECORD_FIELD", recGeoid, fieldGeoid, sidProps);
            }
            // DaliRecord → RECORD_USED_IN → DaliStatement(INSERT)
            for (var stmtEntry : str.getStatements().entrySet()) {
                if (!"INSERT".equals(stmtEntry.getValue().getType())) continue;
                boolean usesRecord = stmtEntry.getValue().getAffectedColumns().stream().anyMatch(
                        ac -> rec.getVarName().equals(ac.get("dataset_alias")));
                if (usesRecord)
                    b.appendEdge("RECORD_USED_IN", recGeoid, stmtEntry.getKey(), sidProps);
            }
        }

        // Structural: RETURNS_INTO (KI-RETURN-1: mirrors RemoteWriter.java:1372-1389)
        // targetGeoid is the external ID of the target vertex (field_geoid / record_geoid /
        // variable geoid / parameter geoid) — same formula used by the semantic listener.
        for (var e : str.getStatements().entrySet()) {
            StatementInfo retSi = e.getValue();
            if (retSi.getReturningTargets().isEmpty()) continue;
            for (StatementInfo.ReturningTarget rt : retSi.getReturningTargets()) {
                Map<String, Object> retProps = new java.util.HashMap<>(sidProps);
                retProps.put("returning_exprs", String.join(",", rt.expressions()));
                b.appendEdge("RETURNS_INTO", e.getKey(), rt.targetGeoid(), retProps);
            }
        }

        // Structural: HAS_JOIN + JOIN_SOURCE_TABLE + JOIN_TARGET_TABLE
        for (var e : str.getStatements().entrySet()) {
            int joinIdx = 0;
            for (JoinInfo j : e.getValue().getJoins()) {
                String joinExtId = e.getKey() + ":JOIN:" + j.lineStart() + ":" + joinIdx;
                b.appendEdge("HAS_JOIN", e.getKey(), joinExtId,
                        mapOf("session_id", sid, "statement_geoid", e.getKey()));
                if (j.sourceTableGeoid() != null)
                    b.appendEdge("JOIN_SOURCE_TABLE", joinExtId, j.sourceTableGeoid(), sidProps);
                if (j.targetTableGeoid() != null)
                    b.appendEdge("JOIN_TARGET_TABLE", joinExtId, j.targetTableGeoid(), sidProps);
                joinIdx++;
            }
        }

        // Structural: HAS_PARAMETER, HAS_VARIABLE
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            int pIdx = 0;
            for (RoutineInfo.ParameterInfo ignored : r.getTypedParameters()) {
                b.appendEdge("HAS_PARAMETER", e.getKey(), e.getKey() + ":PARAM:" + pIdx, sidProps);
                pIdx++;
            }
            int vIdx = 0;
            for (RoutineInfo.VariableInfo ignored : r.getTypedVariables()) {
                b.appendEdge("HAS_VARIABLE", e.getKey(), e.getKey() + ":VAR:" + vIdx, sidProps);
                vIdx++;
            }
        }

        // Usage: BELONGS_TO_SESSION
        for (String tg : str.getTables().keySet())
            b.appendEdge("BELONGS_TO_SESSION", sid, tg, sidProps);
        for (String rg : str.getRoutines().keySet())
            b.appendEdge("BELONGS_TO_SESSION", sid, rg, sidProps);

        // Usage: READS_FROM, WRITES_TO
        for (var e : str.getStatements().entrySet()) {
            for (var st : e.getValue().getSourceTables().entrySet()) {
                @SuppressWarnings("unchecked")
                List<String> stAl = st.getValue().get("table_alias") instanceof List
                        ? (List<String>) st.getValue().get("table_alias") : List.of();
                Map<String, Object> rfProps = new java.util.HashMap<>(sidProps);
                rfProps.put("aliases", new ArrayList<>(stAl));
                b.appendEdge("READS_FROM", e.getKey(), st.getKey(), rfProps);
            }
            for (var tt : e.getValue().getTargetTables().entrySet()) {
                @SuppressWarnings("unchecked")
                List<String> ttAl = tt.getValue().get("table_alias") instanceof List
                        ? (List<String>) tt.getValue().get("table_alias") : List.of();
                Map<String, Object> wtProps = new java.util.HashMap<>(sidProps);
                wtProps.put("aliases", new ArrayList<>(ttAl));
                b.appendEdge("WRITES_TO", e.getKey(), tt.getKey(), wtProps);
            }
        }

        // Usage: USES_SUBQUERY
        for (var e : str.getStatements().entrySet())
            for (var sq : e.getValue().getSourceSubqueries().entrySet())
                b.appendEdge("USES_SUBQUERY", e.getKey(), sq.getKey(), mapOf(
                        "session_id",    sid,
                        "aliases",       sq.getValue().subqueryAliases(),
                        "subquery_type", sq.getValue().subqueryType()));

        // Atoms: HAS_ATOM, ATOM_REF_TABLE, ATOM_REF_COLUMN, ATOM_REF_STMT, ATOM_REF_OUTPUT_COL
        // ATOM_PRODUCES, DATA_FLOW, FILTER_FLOW
        Set<String> tableGeoids = str.getTables().keySet();
        // Build OC lookup: "stmtGeoid:COL_NAME_UPPER" → ocExtId for ATOM_REF_OUTPUT_COL
        Map<String, String> ocByStmtAndName = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                ocByStmtAndName.put(e.getKey() + ":" + oc.getKey().toUpperCase(),
                        e.getKey() + ":OC:" + oc.getKey());
            }
        }
        // Build OC lookup by order: "stmtGeoid:order" → ocExtId for ATOM_PRODUCES / DATA_FLOW
        Map<String, String> ocByOrder = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                Object order = oc.getValue().get("order");
                if (order != null) ocByOrder.put(e.getKey() + ":" + order, e.getKey() + ":OC:" + oc.getKey());
            }
        }
        // Build AffectedColumn lookup by ref: "stmtGeoid:column_ref" → acExtId
        Map<String, String> affColByRef = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            int acIdx = 0;
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String colRef = (String) ac.get("column_ref");
                if (colRef != null)
                    affColByRef.put(e.getKey() + ":" + colRef, e.getKey() + ":AC:" + acIdx);
                acIdx++;
            }
        }

        for (var container : result.getAtoms().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cont = (Map<String, Object>) container.getValue();
            String stmtGeoid = (String) cont.get("source_geoid");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) cont.get("atoms");
            if (atoms == null || stmtGeoid == null) continue;
            // Guard: source_geoid must be a DaliStatement, not a DaliRoutine.
            // Routine-level atom containers have routine_geoid as source_geoid; creating
            // HAS_ATOM/FILTER_FLOW from a DaliRoutine vertex is semantically incorrect.
            if (!str.getStatements().containsKey(stmtGeoid)) continue;

            for (var at : atoms.entrySet()) {
                Map<String, Object> a = at.getValue();
                String atomId = atomIdMap.get(stmtGeoid + ":" + at.getKey());
                if (atomId == null) continue;

                // HAS_ATOM: statement → atom
                b.appendEdge("HAS_ATOM", stmtGeoid, atomId, sidProps);

                String atomTableGeoid = (String) a.get("table_geoid");
                if (atomTableGeoid != null) {
                    if (tableGeoids.contains(atomTableGeoid)) {
                        // Physical table source
                        b.appendEdge("ATOM_REF_TABLE", atomId, atomTableGeoid, sidProps);
                        String colName = (String) a.get("column_name");
                        if (colName != null) {
                            String colGeoid = atomTableGeoid + "." + colName.toUpperCase();
                            if (str.getColumns().containsKey(colGeoid))
                                b.appendEdge("ATOM_REF_COLUMN", atomId, colGeoid, sidProps);
                        }
                    } else {
                        // Subquery/CTE source: table_geoid is a statement geoid
                        if (str.getStatements().containsKey(atomTableGeoid)) {
                            b.appendEdge("ATOM_REF_STMT", atomId, atomTableGeoid, sidProps);
                            String colName = (String) a.get("column_name");
                            if (colName != null) {
                                String ocExtId = ocByStmtAndName.get(
                                        atomTableGeoid + ":" + colName.toUpperCase());
                                if (ocExtId != null)
                                    b.appendEdge("ATOM_REF_OUTPUT_COL", atomId, ocExtId, sidProps);
                            }
                        }
                    }
                }

                // ATOM_PRODUCES: atom → output column
                Object outSeq = a.get("output_column_sequence");
                if (outSeq != null) {
                    String ocExtId = ocByOrder.get(stmtGeoid + ":" + outSeq);
                    if (ocExtId != null)
                        b.appendEdge("ATOM_PRODUCES", atomId, ocExtId, sidProps);
                }
                // ATOM_PRODUCES: atom → affected column (DML target)
                String dmlTargetRefProd1 = (String) a.get("dml_target_ref");
                if (dmlTargetRefProd1 != null) {
                    String acExtId = affColByRef.get(stmtGeoid + ":" + dmlTargetRefProd1);
                    if (acExtId != null) {
                        String mergeClause1 = (String) a.get("merge_clause");
                        Map<String, Object> apProps1 = mergeClause1 != null
                                ? mapOf("session_id", sid, "merge_clause", mergeClause1) : sidProps;
                        b.appendEdge("ATOM_PRODUCES", atomId, acExtId, apProps1);
                    }
                }

                // DATA_FLOW: column → output column (resolved atoms)
                if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && outSeq != null) {
                    String colName = (String) a.get("column_name");
                    if (atomTableGeoid != null && colName != null) {
                        String srcColGeoid = atomTableGeoid + "." + colName.toUpperCase();
                        String ocExtId = ocByOrder.get(stmtGeoid + ":" + outSeq);
                        if (str.getColumns().containsKey(srcColGeoid) && ocExtId != null) {
                            b.appendEdge("DATA_FLOW", srcColGeoid, ocExtId, mapOf(
                                    "session_id", sid,
                                    "statement_geoid", stmtGeoid,
                                    "atom_id", atomId,
                                    "flow_type", resolveFlowType(a, str.getStatements().get(stmtGeoid))
                            ));
                        }
                    }
                }

                // DATA_FLOW: column → affected column (UPDATE SET / MERGE)
                String dmlTargetRef = (String) a.get("dml_target_ref");
                if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && dmlTargetRef != null
                        && atomTableGeoid != null) {
                    String colName = (String) a.get("column_name");
                    if (colName != null) {
                        String srcColGeoid = atomTableGeoid + "." + colName.toUpperCase();
                        String acExtId = affColByRef.get(stmtGeoid + ":" + dmlTargetRef);
                        if (str.getColumns().containsKey(srcColGeoid) && acExtId != null) {
                            b.appendEdge("DATA_FLOW", srcColGeoid, acExtId, mapOf(
                                    "session_id", sid,
                                    "statement_geoid", stmtGeoid,
                                    "atom_id", atomId,
                                    "flow_type", resolveDmlFlowType(str.getStatements().get(stmtGeoid))
                            ));
                        }
                    }
                }

                // FILTER_FLOW: column → statement
                String parentCtx = (String) a.get("parent_context");
                if (("WHERE".equals(parentCtx) || "HAVING".equals(parentCtx) || "JOIN".equals(parentCtx))
                        && AtomInfo.STATUS_RESOLVED.equals(a.get("status"))
                        && atomTableGeoid != null) {
                    String colName = (String) a.get("column_name");
                    if (colName != null) {
                        String colGeoid = atomTableGeoid + "." + colName.toUpperCase();
                        if (str.getColumns().containsKey(colGeoid))
                            b.appendEdge("FILTER_FLOW", colGeoid, stmtGeoid, mapOf(
                                    "filter_type", parentCtx,
                                    "session_id", sid,
                                    "statement_geoid", stmtGeoid,
                                    "via_atom", at.getKey()
                            ));
                    }
                }
            }
        }

        // CALLS: routine → routine
        Map<String, String> routineBySimpleName = new HashMap<>();
        for (String rg : str.getRoutines().keySet()) {
            String simple = rg.contains(":") ? rg.substring(rg.lastIndexOf(':') + 1) : rg;
            routineBySimpleName.put(simple.toUpperCase(), rg);
        }
        for (var callerEntry : result.getCalledRoutines().entrySet()) {
            String callerGeoid = callerEntry.getKey();
            for (Map<String, String> call : callerEntry.getValue()) {
                String calleeName = call.get("name");
                if (calleeName == null) continue;
                String calleeGeoid = routineBySimpleName.get(calleeName.toUpperCase());
                if (calleeGeoid != null)
                    b.appendEdge("CALLS", callerGeoid, calleeGeoid, mapOf(
                            "session_id", sid,
                            "caller_geoid", callerGeoid,
                            "callee_name", calleeName,
                            "line_start", call.get("line")
                    ));
            }
        }

        // NOTE: Lineage edges intentionally omitted.
        // result.getLineage() uses READS_FROM/WRITES_TO in the *opposite* direction to structural
        // edges (lineage: table→stmt; structural: stmt→table) with different field sets.
        // saveRemote() also does not write lineage edges, so omitting here keeps REMOTE vs BATCH parity.

        return b;
    }

    // ═══════════════════════════════════════════════════════════════
    // Factory: namespace/pool mode — canonical objects pre-inserted
    // ═══════════════════════════════════════════════════════════════

    /**
     * Namespace/pool mode variant of {@link #buildFromResult(String, SemanticResult)}.
     *
     * <p>Used in REMOTE_BATCH namespace mode when canonical objects (DaliSchema, DaliTable,
     * DaliColumn, DaliDatabase) have already been inserted into the DB via individual REMOTE
     * {@code rcmd()} calls. This method:
     * <ul>
     *   <li>Merges {@code tableRids + colRids + schemaRids} into {@link #canonicalRids} so that
     *       edges referencing canonical vertices resolve to actual DB RIDs.
     *   <li>Skips DaliDatabase, DaliSchema, DaliTable, DaliColumn vertex sections
     *       (already in DB — re-inserting would hit UNIQUE index → DuplicatedKeyException).
     *   <li>Skips CONTAINS_TABLE and HAS_COLUMN edge sections
     *       (created by REMOTE in ArcadeDBSemanticWriter.saveRemoteBatch).
     *   <li>All other sections (DaliRoutine, DaliStatement, atoms, edges, etc.) work identically
     *       to the single-argument overload, with canonical endpoints resolved via canonicalRids.
     * </ul>
     *
     * @param sid        Session ID
     * @param result     Semantic result
     * @param tableRids  geoid → actual DB RID for DaliTable (from RidCache)
     * @param colRids    geoid → actual DB RID for DaliColumn (from RidCache)
     * @param schemaRids geoid → actual DB RID for DaliSchema (from RidCache)
     */
    public static JsonlBatchBuilder buildFromResult(
            String sid, SemanticResult result,
            Map<String, String> tableRids,
            Map<String, String> colRids,
            Map<String, String> schemaRids) {

        JsonlBatchBuilder b = new JsonlBatchBuilder();
        // Populate canonical RID map — edges to canonical vertices will use actual RIDs
        if (tableRids  != null) b.canonicalRids.putAll(tableRids);
        if (colRids    != null) b.canonicalRids.putAll(colRids);
        if (schemaRids != null) b.canonicalRids.putAll(schemaRids);

        Structure str = result.getStructure();
        if (str == null) return b;

        Map<String, Object> sidProps = Map.of("session_id", sid);

        // ─────────────────────────────────────────────────────
        // Phase 1: Vertices (canonical types SKIPPED — already in DB)
        // ─────────────────────────────────────────────────────

        // 1. DaliSession
        b.appendVertex("DaliSession", sid, mapOf(
                "session_id", sid,
                "file_path", nvl(result.getFilePath()),
                "dialect", nvl(result.getDialect()),
                "processing_time_ms", result.getProcessingTimeMs(),
                "created_at", System.currentTimeMillis()
        ));

        // 2. DaliDatabase — SKIPPED (canonical, already in DB)
        // 3. DaliSchema   — SKIPPED (canonical, already in DB)
        // 4a. DaliTable   — SKIPPED (canonical, already in DB)
        // 4b. DaliColumn  — SKIPPED (canonical, already in DB)

        // 4. DaliPackage (session-specific)
        for (var e : str.getPackages().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = (Map<String, Object>) e.getValue();
            b.appendVertex("DaliPackage", e.getKey(), mapOf(
                    "session_id", sid,
                    "package_geoid", e.getKey(),
                    "package_name", pkg.get("package_name"),
                    "schema_geoid", pkg.get("schema_geoid"),
                    "routine_geoid", e.getKey(),
                    "routine_name", pkg.get("package_name"),
                    "routine_type", "PACKAGE"
            ));
        }

        // 5. DaliRoutine (session-specific)
        Set<String> packageGeoids = str.getPackages().keySet();
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            if (packageGeoids.contains(e.getKey())) continue;
            b.appendVertex("DaliRoutine", e.getKey(), mapOf(
                    "session_id", sid,
                    "routine_geoid", e.getKey(),
                    "routine_name", r.getName(),
                    "routine_type", r.getRoutineType(),
                    "package_geoid", r.getPackageGeoid(),
                    "schema_geoid", r.getSchemaGeoid(),
                    "is_pipelined", r.isPipelined() ? true : null,  // KI-PIPE-1
                    "autonomous_transaction", r.isAutonomousTransaction() ? true : null  // KI-PRAGMA-1
            ));
        }

        // 5b. DaliParameter, DaliVariable
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            int pIdx = 0;
            for (RoutineInfo.ParameterInfo p : r.getTypedParameters()) {
                b.appendVertex("DaliParameter", e.getKey() + ":PARAM:" + pIdx, mapOf(
                        "session_id", sid,
                        "routine_geoid", e.getKey(),
                        "param_name", p.name(),
                        "param_type", p.type(),
                        "param_mode", p.mode()
                ));
                pIdx++;
            }
            int vIdx = 0;
            for (RoutineInfo.VariableInfo v : r.getTypedVariables()) {
                b.appendVertex("DaliVariable", e.getKey() + ":VAR:" + vIdx, mapOf(
                        "session_id", sid,
                        "routine_geoid", e.getKey(),
                        "var_name", v.name(),
                        "var_type", v.type()
                ));
                vIdx++;
            }
        }

        // 6. DaliStatement — DDL excluded (go to DaliDDLStatement only)
        for (var e : str.getStatements().entrySet()) {
            StatementInfo s = e.getValue();
            if (isDdl(s.getType())) continue;  // DDL → DaliDDLStatement, not DaliStatement
            b.appendVertex("DaliStatement", e.getKey(), mapOf(
                    "session_id", sid,
                    "stmt_geoid", e.getKey(),
                    "type", s.getType(),
                    "subtype", s.getSubtype(),
                    "line_start", s.getLineStart(),
                    "line_end", s.getLineEnd(),
                    "parent_statement", s.getParentStatementGeoid(),
                    "parent_statement_type", parentType(s.getParentStatementGeoid(), str),
                    "routine_geoid", s.getRoutineGeoid(),
                    "short_name", s.getShortName(),
                    "is_union", s.isUnion(),
                    "is_dml", isDml(s.getType()),
                    "is_ddl", false,
                    "has_aggregation", s.isHasAggregation(),
                    "has_window", s.isHasWindow(),
                    "has_cte", hasCte(s, str.getStatements()),
                    "join_count", s.getJoins().size(),
                    "col_count_output", s.getColumnsOutput().size(),
                    "col_count_input", countInputColumns(s),
                    "depth", computeDepth(s.getParentStatementGeoid(), str.getStatements()),
                    "quality", computeStatementQuality(s)
            ));
        }

        // 7. DaliOutputColumn
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                Map<String, Object> col = oc.getValue();
                String ocExtId = e.getKey() + ":OC:" + oc.getKey();
                b.appendVertex("DaliOutputColumn", ocExtId, mapOf(
                        "session_id", sid,
                        "statement_geoid", e.getKey(),
                        "col_key", oc.getKey(),
                        "name", col.get("name"),
                        "expression", col.get("expression"),
                        "alias", col.get("alias"),
                        "col_order", col.get("order"),
                        "source_type", col.get("source_type"),
                        "table_ref", col.get("table_ref")
                ));
            }
        }

        // 7b. DaliAffectedColumn
        for (var e : str.getStatements().entrySet()) {
            int acIdx = 0;
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String typeAffect = null;
                Integer orderAffect = null;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> poliage = (List<Map<String, Object>>) ac.get("poliage_update");
                if (poliage != null && !poliage.isEmpty()) {
                    typeAffect  = (String)  poliage.get(0).get("type_affect");
                    Object oa   = poliage.get(0).get("order_affect");
                    orderAffect = oa instanceof Number n ? n.intValue() : null;
                }
                String acExtId = e.getKey() + ":AC:" + acIdx;
                b.appendVertex("DaliAffectedColumn", acExtId, mapOf(
                        "session_id", sid,
                        "statement_geoid", e.getKey(),
                        "column_ref", ac.get("column_ref"),
                        "column_name", ac.get("column_name"),
                        "table_geoid", ac.get("table_geoid"),
                        "dataset_alias", ac.get("dataset_alias"),
                        "source_type", ac.get("source_type"),
                        "resolution_status", ac.get("resolution_status"),
                        "type_affect", typeAffect,
                        "order_affect", orderAffect
                ));
                acIdx++;
            }
        }

        // 7c. DaliRecord (G6: BULK COLLECT targets)
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            b.appendVertex("DaliRecord", rec.getGeoid(), mapOf(
                    "session_id", sid,
                    "record_geoid", rec.getGeoid(),
                    "record_name", rec.getVarName(),
                    "routine_geoid", rec.getRoutineGeoid(),
                    "source_stmt_geoid", rec.getSourceStatementGeoid(),
                    "fields", String.join(",", rec.getFields())
            ));
        }

        // 8. DaliAtom — skip "routine" containers (same atoms as statement containers, duplicate view)
        Map<String, String> atomIdMap = new LinkedHashMap<>();
        for (var container : result.getAtoms().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cont = (Map<String, Object>) container.getValue();
            String sourceType = (String) cont.get("source_type");
            if ("routine".equals(sourceType)) continue; // duplicate view — skip
            String stmtGeoid = (String) cont.get("source_geoid");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) cont.get("atoms");
            if (atoms == null || stmtGeoid == null) continue;

            for (var at : atoms.entrySet()) {
                Map<String, Object> a = at.getValue();
                String atomId = md5(stmtGeoid + ":" + at.getKey());
                atomIdMap.put(stmtGeoid + ":" + at.getKey(), atomId);
                // Detect AtomInfo.STATUS_UNBOUND for column-reference atoms whose DaliColumn is absent from schema.
                // Only applies to physical-table atoms: SubQuery/CTE/MERGE-USING SELECT atoms resolve via
                // ATOM_REF_OUTPUT_COL (not DaliColumn), so absence from str.getColumns() is expected.
                String atomColForWarn2 = (String) a.get("column_name");
                String atomTblForWarn2 = (String) a.get("table_geoid");
                boolean isColRef2 = Boolean.TRUE.equals(a.get("is_column_reference"));
                String atomWarning2 = (String) a.get("warning");
                if (atomWarning2 == null && AtomInfo.STATUS_RESOLVED.equals(a.get("status"))
                        && isColRef2 && atomTblForWarn2 != null && atomColForWarn2 != null
                        && str.getTables().containsKey(atomTblForWarn2)
                        && !str.getColumns().containsKey(atomTblForWarn2 + "." + atomColForWarn2.toUpperCase())) {
                    atomWarning2 = AtomInfo.STATUS_UNBOUND;
                }

                b.appendVertex("DaliAtom", atomId, mapOf(
                        "session_id", sid,
                        "statement_geoid", stmtGeoid,
                        "atom_id", atomId,
                        "atom_text", at.getKey(),
                        "atom_context", a.get("atom_context"),
                        "parent_context", a.get("parent_context"),
                        "position", a.get("position"),
                        "sposition", a.get("sposition"),
                        "is_complex", a.get("is_complex"),
                        "is_column_reference", a.get("is_column_reference"),
                        "is_function_call", a.get("is_function_call"),
                        "is_constant", a.get("is_constant"),
                        "is_routine_param", a.get("is_routine_param"),
                        "is_routine_var", a.get("is_routine_var"),
                        "table_name", a.get("table_name"),
                        "column_name", a.get("column_name"),
                        "table_geoid", a.get("table_geoid"),
                        "status", a.get("status"),
                        "warning", atomWarning2,
                        "merge_clause", a.get("merge_clause"),
                        "output_column_sequence", a.get("output_column_sequence"),
                        "nested_atoms_count", a.get("nested_atoms_count")
                ));
            }
        }

        // 9. DaliJoin
        for (var e : str.getStatements().entrySet()) {
            int joinIdx = 0;
            for (JoinInfo j : e.getValue().getJoins()) {
                String joinExtId = e.getKey() + ":JOIN:" + j.lineStart() + ":" + joinIdx;
                b.appendVertex("DaliJoin", joinExtId, mapOf(
                        "session_id", sid,
                        "statement_geoid", e.getKey(),
                        "join_type", j.joinType(),
                        "source_table_geoid", j.sourceTableGeoid(),
                        "source_alias", j.sourceTableAlias(),
                        "source_type", j.sourceType(),
                        "target_table_geoid", j.targetTableGeoid(),
                        "target_alias", j.targetTableAlias(),
                        "target_type", j.targetType(),
                        "conditions", j.conditions(),
                        "line_start", j.lineStart()
                ));
                joinIdx++;
            }
        }

        // DaliSnippet — NOT written here (v28+: VERTEX written via rcmd() post-batch in
        //     RemoteWriter so that the HAS_SNIPPET edge can be created in the same transaction).

        // ─────────────────────────────────────────────────────
        // Phase 2: Edges
        // ─────────────────────────────────────────────────────
        // CONTAINS_TABLE and HAS_COLUMN are SKIPPED here — created by REMOTE in saveRemoteBatch.

        // CONTAINS_ROUTINE (schema → package) — schema endpoint resolved via canonicalRids
        for (var e : str.getPackages().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = (Map<String, Object>) e.getValue();
            String sg = (String) pkg.get("schema_geoid");
            if (sg != null && !sg.isEmpty())
                b.appendEdge("CONTAINS_ROUTINE", sg, e.getKey(), sidProps);
        }

        // CONTAINS_ROUTINE (package/schema → routine)
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            if (packageGeoids.contains(e.getKey())) continue;
            String fromGeoid = null;
            if (r.getPackageGeoid() != null)      fromGeoid = r.getPackageGeoid();
            else if (r.getSchemaGeoid() != null)  fromGeoid = r.getSchemaGeoid();
            if (fromGeoid != null)
                b.appendEdge("CONTAINS_ROUTINE", fromGeoid, e.getKey(), sidProps);
        }

        // NESTED_IN (parent routine → child routine)
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            if (r.getParentRoutineGeoid() != null)
                b.appendEdge("NESTED_IN", r.getParentRoutineGeoid(), e.getKey(), sidProps);
        }

        // CONTAINS_STMT (routine → statement)
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getRoutineGeoid() != null)
                b.appendEdge("CONTAINS_STMT", e.getValue().getRoutineGeoid(), e.getKey(), sidProps);
        }

        // CHILD_OF (statement → parent statement)
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getParentStatementGeoid() != null)
                b.appendEdge("CHILD_OF", e.getKey(), e.getValue().getParentStatementGeoid(), sidProps);
        }

        // HAS_OUTPUT_COL
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                String ocExtId = e.getKey() + ":OC:" + oc.getKey();
                b.appendEdge("HAS_OUTPUT_COL", e.getKey(), ocExtId,
                        mapOf("session_id", sid, "statement_geoid", e.getKey()));
            }
        }

        // HAS_AFFECTED_COL + AFFECTED_COL_REF_TABLE
        for (var e : str.getStatements().entrySet()) {
            int acIdx = 0;
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String acExtId = e.getKey() + ":AC:" + acIdx;
                b.appendEdge("HAS_AFFECTED_COL", e.getKey(), acExtId, sidProps);
                String tg = (String) ac.get("table_geoid");
                if (tg != null)
                    b.appendEdge("AFFECTED_COL_REF_TABLE", acExtId, tg, sidProps);
                acIdx++;
            }
        }

        // BULK_COLLECTS_INTO, RECORD_USED_IN (G6: DaliRecord edges)
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            String recGeoid = rec.getGeoid();
            if (rec.getSourceStatementGeoid() != null)
                b.appendEdge("BULK_COLLECTS_INTO", rec.getSourceStatementGeoid(), recGeoid, sidProps);
            for (var stmtEntry : str.getStatements().entrySet()) {
                if (!"INSERT".equals(stmtEntry.getValue().getType())) continue;
                boolean usesRecord = stmtEntry.getValue().getAffectedColumns().stream().anyMatch(
                        ac -> rec.getVarName().equals(ac.get("dataset_alias")));
                if (usesRecord)
                    b.appendEdge("RECORD_USED_IN", recGeoid, stmtEntry.getKey(), sidProps);
            }
        }

        // HAS_JOIN + JOIN_SOURCE_TABLE + JOIN_TARGET_TABLE
        for (var e : str.getStatements().entrySet()) {
            int joinIdx = 0;
            for (JoinInfo j : e.getValue().getJoins()) {
                String joinExtId = e.getKey() + ":JOIN:" + j.lineStart() + ":" + joinIdx;
                b.appendEdge("HAS_JOIN", e.getKey(), joinExtId,
                        mapOf("session_id", sid, "statement_geoid", e.getKey()));
                if (j.sourceTableGeoid() != null)
                    b.appendEdge("JOIN_SOURCE_TABLE", joinExtId, j.sourceTableGeoid(), sidProps);
                if (j.targetTableGeoid() != null)
                    b.appendEdge("JOIN_TARGET_TABLE", joinExtId, j.targetTableGeoid(), sidProps);
                joinIdx++;
            }
        }

        // HAS_PARAMETER, HAS_VARIABLE
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            int pIdx = 0;
            for (RoutineInfo.ParameterInfo ignored : r.getTypedParameters()) {
                b.appendEdge("HAS_PARAMETER", e.getKey(), e.getKey() + ":PARAM:" + pIdx, sidProps);
                pIdx++;
            }
            int vIdx = 0;
            for (RoutineInfo.VariableInfo ignored : r.getTypedVariables()) {
                b.appendEdge("HAS_VARIABLE", e.getKey(), e.getKey() + ":VAR:" + vIdx, sidProps);
                vIdx++;
            }
        }

        // BELONGS_TO_SESSION — canonical tables/routines (tables resolve via canonicalRids)
        for (String tg : str.getTables().keySet())
            b.appendEdge("BELONGS_TO_SESSION", sid, tg, sidProps);
        for (String rg : str.getRoutines().keySet())
            b.appendEdge("BELONGS_TO_SESSION", sid, rg, sidProps);

        // READS_FROM, WRITES_TO
        for (var e : str.getStatements().entrySet()) {
            for (var st : e.getValue().getSourceTables().entrySet()) {
                @SuppressWarnings("unchecked")
                List<String> stAl = st.getValue().get("table_alias") instanceof List
                        ? (List<String>) st.getValue().get("table_alias") : List.of();
                Map<String, Object> rfProps = new java.util.HashMap<>(sidProps);
                rfProps.put("aliases", new ArrayList<>(stAl));
                b.appendEdge("READS_FROM", e.getKey(), st.getKey(), rfProps);
            }
            for (var tt : e.getValue().getTargetTables().entrySet()) {
                @SuppressWarnings("unchecked")
                List<String> ttAl = tt.getValue().get("table_alias") instanceof List
                        ? (List<String>) tt.getValue().get("table_alias") : List.of();
                Map<String, Object> wtProps = new java.util.HashMap<>(sidProps);
                wtProps.put("aliases", new ArrayList<>(ttAl));
                b.appendEdge("WRITES_TO", e.getKey(), tt.getKey(), wtProps);
            }
        }

        // USES_SUBQUERY
        for (var e : str.getStatements().entrySet())
            for (var sq : e.getValue().getSourceSubqueries().entrySet())
                b.appendEdge("USES_SUBQUERY", e.getKey(), sq.getKey(), mapOf(
                        "session_id",    sid,
                        "aliases",       sq.getValue().subqueryAliases(),
                        "subquery_type", sq.getValue().subqueryType()));

        // Atom edges: HAS_ATOM, ATOM_REF_TABLE, ATOM_REF_COLUMN, ATOM_REF_STMT,
        //             ATOM_REF_OUTPUT_COL, ATOM_PRODUCES, DATA_FLOW, FILTER_FLOW
        Set<String> tableGeoids = str.getTables().keySet();
        Map<String, String> ocByStmtAndName = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                ocByStmtAndName.put(e.getKey() + ":" + oc.getKey().toUpperCase(),
                        e.getKey() + ":OC:" + oc.getKey());
            }
        }
        Map<String, String> ocByOrder = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                Object order = oc.getValue().get("order");
                if (order != null) ocByOrder.put(e.getKey() + ":" + order, e.getKey() + ":OC:" + oc.getKey());
            }
        }
        Map<String, String> affColByRef = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            int acIdx = 0;
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String colRef = (String) ac.get("column_ref");
                if (colRef != null)
                    affColByRef.put(e.getKey() + ":" + colRef, e.getKey() + ":AC:" + acIdx);
                acIdx++;
            }
        }

        for (var container : result.getAtoms().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cont = (Map<String, Object>) container.getValue();
            String stmtGeoid = (String) cont.get("source_geoid");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) cont.get("atoms");
            if (atoms == null || stmtGeoid == null) continue;
            // Guard: source_geoid must be a DaliStatement, not a DaliRoutine.
            if (!str.getStatements().containsKey(stmtGeoid)) continue;

            for (var at : atoms.entrySet()) {
                Map<String, Object> a = at.getValue();
                String atomId = atomIdMap.get(stmtGeoid + ":" + at.getKey());
                if (atomId == null) continue;

                b.appendEdge("HAS_ATOM", stmtGeoid, atomId, sidProps);

                String atomTableGeoid = (String) a.get("table_geoid");
                if (atomTableGeoid != null) {
                    if (tableGeoids.contains(atomTableGeoid)
                            || b.canonicalRids.containsKey(atomTableGeoid)) {
                        // Physical table source (batch-local or canonical)
                        b.appendEdge("ATOM_REF_TABLE", atomId, atomTableGeoid, sidProps);
                        String colName = (String) a.get("column_name");
                        if (colName != null) {
                            String colGeoid = atomTableGeoid + "." + colName.toUpperCase();
                            if (str.getColumns().containsKey(colGeoid)
                                    || b.canonicalRids.containsKey(colGeoid))
                                b.appendEdge("ATOM_REF_COLUMN", atomId, colGeoid, sidProps);
                        }
                    } else {
                        // Subquery/CTE source
                        if (str.getStatements().containsKey(atomTableGeoid)) {
                            b.appendEdge("ATOM_REF_STMT", atomId, atomTableGeoid, sidProps);
                            String colName = (String) a.get("column_name");
                            if (colName != null) {
                                String ocExtId = ocByStmtAndName.get(
                                        atomTableGeoid + ":" + colName.toUpperCase());
                                if (ocExtId != null)
                                    b.appendEdge("ATOM_REF_OUTPUT_COL", atomId, ocExtId, sidProps);
                            }
                        }
                    }
                }

                Object outSeq = a.get("output_column_sequence");
                if (outSeq != null) {
                    String ocExtId = ocByOrder.get(stmtGeoid + ":" + outSeq);
                    if (ocExtId != null)
                        b.appendEdge("ATOM_PRODUCES", atomId, ocExtId, sidProps);
                }
                // ATOM_PRODUCES: atom → affected column (DML target)
                String dmlTargetRefProd2 = (String) a.get("dml_target_ref");
                if (dmlTargetRefProd2 != null) {
                    String acExtId2 = affColByRef.get(stmtGeoid + ":" + dmlTargetRefProd2);
                    if (acExtId2 != null) {
                        String mergeClause2 = (String) a.get("merge_clause");
                        Map<String, Object> apProps2 = mergeClause2 != null
                                ? mapOf("session_id", sid, "merge_clause", mergeClause2) : sidProps;
                        b.appendEdge("ATOM_PRODUCES", atomId, acExtId2, apProps2);
                    }
                }

                if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && outSeq != null) {
                    String colName = (String) a.get("column_name");
                    if (atomTableGeoid != null && colName != null) {
                        String srcColGeoid = atomTableGeoid + "." + colName.toUpperCase();
                        String ocExtId = ocByOrder.get(stmtGeoid + ":" + outSeq);
                        if ((str.getColumns().containsKey(srcColGeoid)
                                || b.canonicalRids.containsKey(srcColGeoid))
                                && ocExtId != null) {
                            b.appendEdge("DATA_FLOW", srcColGeoid, ocExtId, mapOf(
                                    "session_id", sid,
                                    "statement_geoid", stmtGeoid,
                                    "atom_id", atomId,
                                    "flow_type", resolveFlowType(a, str.getStatements().get(stmtGeoid))
                            ));
                        }
                    }
                }

                String dmlTargetRef = (String) a.get("dml_target_ref");
                if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && dmlTargetRef != null
                        && atomTableGeoid != null) {
                    String colName = (String) a.get("column_name");
                    if (colName != null) {
                        String srcColGeoid = atomTableGeoid + "." + colName.toUpperCase();
                        String acExtId = affColByRef.get(stmtGeoid + ":" + dmlTargetRef);
                        if ((str.getColumns().containsKey(srcColGeoid)
                                || b.canonicalRids.containsKey(srcColGeoid))
                                && acExtId != null) {
                            b.appendEdge("DATA_FLOW", srcColGeoid, acExtId, mapOf(
                                    "session_id", sid,
                                    "statement_geoid", stmtGeoid,
                                    "atom_id", atomId,
                                    "flow_type", resolveDmlFlowType(str.getStatements().get(stmtGeoid))
                            ));
                        }
                    }
                }

                String parentCtx = (String) a.get("parent_context");
                if (("WHERE".equals(parentCtx) || "HAVING".equals(parentCtx) || "JOIN".equals(parentCtx))
                        && AtomInfo.STATUS_RESOLVED.equals(a.get("status"))
                        && atomTableGeoid != null) {
                    String colName = (String) a.get("column_name");
                    if (colName != null) {
                        String colGeoid = atomTableGeoid + "." + colName.toUpperCase();
                        if (str.getColumns().containsKey(colGeoid)
                                || b.canonicalRids.containsKey(colGeoid))
                            b.appendEdge("FILTER_FLOW", colGeoid, stmtGeoid, mapOf(
                                    "filter_type", parentCtx,
                                    "session_id", sid,
                                    "statement_geoid", stmtGeoid,
                                    "via_atom", at.getKey()
                            ));
                    }
                }
            }
        }

        // CALLS: routine → routine
        for (var callerEntry : result.getCalledRoutines().entrySet()) {
            String callerGeoid = callerEntry.getKey();
            for (Map<String, String> call : callerEntry.getValue()) {
                String calleeName = call.get("name");
                if (calleeName == null) continue;
                String calleeGeoid = null;
                for (String rg : str.getRoutines().keySet()) {
                    String upper = rg.toUpperCase();
                    if (upper.endsWith(":" + calleeName.toUpperCase())
                            || upper.equals(calleeName.toUpperCase())) {
                        calleeGeoid = rg;
                        break;
                    }
                }
                if (calleeGeoid != null)
                    b.appendEdge("CALLS", callerGeoid, calleeGeoid, mapOf(
                            "session_id", sid,
                            "caller_geoid", callerGeoid,
                            "callee_name", calleeName,
                            "line_start", call.get("line")
                    ));
            }
        }

        return b;
    }

    // ═══════════════════════════════════════════════════════════════
    // JSON serialization (no external dependencies)
    // ═══════════════════════════════════════════════════════════════

    private static String toJsonLine(String metaType, String className, String extId,
                                      String from, String to, Map<String, Object> props) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{\"@type\":\"").append(metaType)
          .append("\",\"@class\":\"").append(className).append('"');

        if (extId != null) sb.append(",\"@id\":\"").append(escapeJson(extId)).append('"');
        if (from != null)  sb.append(",\"@from\":\"").append(escapeJson(from)).append('"');
        if (to != null)    sb.append(",\"@to\":\"").append(escapeJson(to)).append('"');

        if (props != null) {
            for (var entry : props.entrySet()) {
                Object v = entry.getValue();
                if (v == null) continue; // skip null values
                sb.append(",\"").append(escapeJson(entry.getKey())).append("\":");
                appendJsonValue(sb, v);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static void appendJsonValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String s) {
            sb.append('"').append(escapeJson(s)).append('"');
        } else if (v instanceof Integer || v instanceof Long) {
            sb.append(v);
        } else if (v instanceof Number n) {
            // Double, Float — write as number
            sb.append(n);
        } else if (v instanceof Boolean b) {
            sb.append(b ? "true" : "false");
        } else {
            // Fallback: toString() escaped as string
            sb.append('"').append(escapeJson(v.toString())).append('"');
        }
    }

    static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Helpers (mirrored from ArcadeDBSemanticWriter)
    // ═══════════════════════════════════════════════════════════════

    static String md5(String s) {
        if (s == null) return "";
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    private static Object nvl(Object v) { return v != null ? v : ""; }

    private static String parentType(String parentGeoid, Structure str) {
        if (parentGeoid == null) return null;
        StatementInfo p = str.getStatements().get(parentGeoid);
        return p != null ? p.getType() : null;
    }

    private static boolean isDml(String type) {
        return type != null && switch (type) {
            case "INSERT", "UPDATE", "DELETE", "MERGE" -> true;
            default -> false;
        };
    }

    private static boolean isDdl(String type) {
        if (type == null) return false;
        return type.startsWith("CREATE") || type.startsWith("ALTER") || type.startsWith("DROP");
    }

    private static int computeDepth(String parentGeoid, Map<String, StatementInfo> allStatements) {
        int depth = 0;
        String current = parentGeoid;
        while (current != null && depth < 50) {
            StatementInfo parent = allStatements.get(current);
            if (parent == null) break;
            depth++;
            current = parent.getParentStatementGeoid();
        }
        return depth;
    }

    private static double computeStatementQuality(StatementInfo s) {
        Map<String, Map<String, Object>> atoms = s.getAtoms();
        if (atoms.isEmpty()) return 0.0;
        int total = atoms.size();
        long resolved  = atoms.values().stream().filter(a -> AtomInfo.STATUS_RESOLVED.equals(a.get("status"))).count();
        long constants = atoms.values().stream().filter(a -> Boolean.TRUE.equals(a.get("is_constant"))).count();
        long functions = atoms.values().stream().filter(a -> Boolean.TRUE.equals(a.get("is_function_call"))).count();
        return (resolved + constants + functions) / (double) total;
    }

    private static boolean hasCte(StatementInfo s, Map<String, StatementInfo> allStatements) {
        for (String childGeoid : s.getChildStatements()) {
            StatementInfo child = allStatements.get(childGeoid);
            if (child != null && "CTE".equals(child.getType())) return true;
        }
        return false;
    }

    private static int countInputColumns(StatementInfo s) {
        return (int) s.getAtoms().values().stream()
                .filter(a -> Boolean.TRUE.equals(a.get("is_column_reference")))
                .count();
    }

    private static String resolveFlowType(Map<String, Object> atom, StatementInfo stmt) {
        if (stmt != null && stmt.isHasAggregation()
                && "GROUP BY".equals(atom.get("parent_context")))
            return "AGGREGATE";
        return "DIRECT";
    }

    private static String resolveDmlFlowType(StatementInfo stmt) {
        if (stmt == null) return "DIRECT";
        return switch (stmt.getType()) {
            case "UPDATE" -> "UPDATE";
            case "MERGE"  -> "MERGE";
            case "INSERT" -> "INSERT";
            default       -> "DIRECT";
        };
    }

    /** Creates a mutable LinkedHashMap that allows null values (unlike Map.of). */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
