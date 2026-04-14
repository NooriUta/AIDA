// File: src/main/java/com/hound/storage/RemoteWriter.java
package com.hound.storage;

import com.arcadedb.remote.RemoteDatabase;
import com.hound.metrics.PipelineTimer;
import com.hound.semantic.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.hound.storage.WriteHelpers.*;

/**
 * Writes one SemanticResult into a remote ArcadeDB instance via SQL commands.
 *
 * Package-private — used only by {@link ArcadeDBSemanticWriter}.
 */
class RemoteWriter {

    private static final Logger logger = LoggerFactory.getLogger(RemoteWriter.class);

    private static final int  RCMD_MAX_RETRIES   = 3;
    private static final long RCMD_RETRY_BASE_MS = 200;

    private final RemoteDatabase db;

    RemoteWriter(RemoteDatabase db) {
        this.db = db;
    }

    // ═══════════════════════════════════════════════════════════════
    // RID cache — keyed by entity geoid → ArcadeDB RID string
    // ═══════════════════════════════════════════════════════════════

    static class RidCache {
        Map<String, String> tables     = new HashMap<>();
        Map<String, String> columns    = new HashMap<>();
        Map<String, String> statements = new HashMap<>();
        Map<String, String> routines   = new HashMap<>();
        Map<String, String> packages   = new HashMap<>();
        Map<String, String> schemas    = new HashMap<>();
        Map<String, String> atoms      = new HashMap<>();
        Map<String, String> outputCols = new HashMap<>();
        /** key = "stmtGeoid:col_order" → RID  (ATOM_PRODUCES / DATA_FLOW) */
        Map<String, String> ocByOrder  = new HashMap<>();
        /** key = "stmtGeoid:column_ref" → RID  (DATA_FLOW → DaliAffectedColumn) */
        Map<String, String> affCols    = new HashMap<>();
        /** key = record_geoid → RID (BULK_COLLECTS_INTO / RECORD_USED_IN) */
        Map<String, String> records = new HashMap<>();
        /** key = field_geoid → RID (HAS_RECORD_FIELD) */
        Map<String, String> recordFields = new HashMap<>();
        /** constraint_geoid → RID  (DaliPrimaryKey / DaliForeignKey) */
        Map<String, String> constraints = new HashMap<>();
        /** KI-DDL-1: stmt_geoid → RID for DaliDDLStatement vertices */
        Map<String, String> ddlStatements = new HashMap<>();
        /** KI-RETURN-1: routineGeoid+":PARAM:"+param_name → RID */
        Map<String, String> parameters = new HashMap<>();
        /** KI-RETURN-1: routineGeoid+":VAR:"+var_name → RID */
        Map<String, String> variables = new HashMap<>();
        /** RID of the DaliSession vertex (BELONGS_TO_SESSION edges) */
        String sessionRid = null;
    }

    private RidCache buildRidCache(String sid) {
        return buildRidCache(sid, null, null);
    }

    private RidCache buildRidCache(String sid, CanonicalPool pool, String dbName) {
        RidCache cache = new RidCache();
        if (pool != null && dbName != null) {
            cache.schemas = buildRidMapByField("DaliSchema", "schema_geoid", "db_name", dbName);
            cache.tables  = buildRidMapByField("DaliTable",  "table_geoid",  "db_name", dbName);
            cache.columns = buildRidMapByField("DaliColumn", "column_geoid", "db_name", dbName);
        } else {
            cache.schemas = buildRidMap("DaliSchema", "schema_geoid", sid);
            cache.tables  = buildRidMap("DaliTable",  "table_geoid",  sid);
            cache.columns = buildRidMap("DaliColumn", "column_geoid", sid);
        }
        cache.statements = buildRidMap("DaliStatement",   "stmt_geoid",    sid);
        cache.routines   = buildRidMap("DaliRoutine",     "routine_geoid", sid);
        cache.packages   = buildRidMap("DaliPackage",     "package_geoid", sid);
        cache.atoms      = buildRidMap("DaliAtom",        "atom_id",       sid);
        cache.outputCols = buildRidMap("DaliOutputColumn","col_key",       sid);
        cache.records      = buildRidMap("DaliRecord",      "record_geoid",  sid);
        cache.recordFields = buildRidMap("DaliRecordField", "field_geoid",   sid);
        cache.constraints  = buildConstraintRidMap(sid);
        // KI-DDL-1: DaliDDLStatement — pool mode uses db_name filter, non-pool uses session_id
        if (pool != null && dbName != null) {
            cache.ddlStatements = buildRidMapByField("DaliDDLStatement", "stmt_geoid", "db_name", dbName);
        } else {
            cache.ddlStatements = buildRidMap("DaliDDLStatement", "stmt_geoid", sid);
        }
        cache.ocByOrder  = buildOcByOrderMap(sid);
        cache.affCols    = buildAffColMap(sid);
        cache.sessionRid = buildRidMap("DaliSession", "session_id", sid)
                .values().stream().findFirst().orElse(null);

        // KI-RETURN-1: build compound-key maps for parameters + variables
        cache.parameters = buildCompoundRidMap("DaliParameter", "routine_geoid", "param_name", ":PARAM:", sid);
        cache.variables  = buildCompoundRidMap("DaliVariable",  "routine_geoid", "var_name",   ":VAR:",  sid);

        logger.info("RID cache [db={}, sid={}]: T:{} C:{} S:{} R:{} P:{} A:{} OC:{} OCo:{} AC:{} Rec:{}",
                dbName != null ? dbName : "ad-hoc", sid,
                cache.tables.size(), cache.columns.size(),
                cache.statements.size(), cache.routines.size(),
                cache.packages.size(), cache.atoms.size(),
                cache.outputCols.size(), cache.ocByOrder.size(), cache.affCols.size(),
                cache.records.size());
        return cache;
    }

    private Map<String, String> buildRidMap(String type, String keyField, String sid) {
        Map<String, String> map = new HashMap<>();
        try {
            var rs = db.query("sql",
                    "SELECT @rid AS rid, " + keyField + " FROM " + type + " WHERE session_id = :sid",
                    Map.of("sid", sid));
            while (rs.hasNext()) {
                var doc = rs.next().toMap();
                String key = (String) doc.get(keyField);
                String rid = (String) doc.get("rid");
                if (key != null && rid != null) map.put(key, rid);
            }
        } catch (Exception e) {
            logger.warn("RID map failed for {}: {}", type, e.getMessage());
        }
        return map;
    }

    /**
     * KI-RETURN-1: builds a RID map keyed by compound geoid = prefixField + separator + nameField.
     * Used for DaliParameter (key = routineGeoid + ":PARAM:" + param_name)
     * and DaliVariable  (key = routineGeoid + ":VAR:"   + var_name).
     */
    private Map<String, String> buildCompoundRidMap(String type, String prefixField,
                                                     String nameField, String separator, String sid) {
        Map<String, String> map = new HashMap<>();
        try {
            var rs = db.query("sql",
                    "SELECT @rid AS rid, " + prefixField + ", " + nameField
                    + " FROM " + type + " WHERE session_id = :sid",
                    Map.of("sid", sid));
            while (rs.hasNext()) {
                var doc = rs.next().toMap();
                String prefix = (String) doc.get(prefixField);
                String name   = (String) doc.get(nameField);
                String rid    = (String) doc.get("rid");
                if (prefix != null && name != null && rid != null)
                    map.put(prefix + separator + name, rid);
            }
        } catch (Exception e) {
            logger.warn("Compound RID map failed for {}: {}", type, e.getMessage());
        }
        return map;
    }

    private Map<String, String> buildRidMapByField(String type, String keyField,
                                                    String filterField, String filterVal) {
        Map<String, String> map = new HashMap<>();
        try {
            var rs = db.query("sql",
                    "SELECT @rid AS rid, " + keyField + " FROM " + type
                    + " WHERE " + filterField + " = :val", Map.of("val", filterVal));
            while (rs.hasNext()) {
                var doc = rs.next().toMap();
                String key = (String) doc.get(keyField);
                String rid = (String) doc.get("rid");
                if (key != null && rid != null) map.put(key, rid);
            }
        } catch (Exception e) {
            logger.warn("RID map by field failed for {}: {}", type, e.getMessage());
        }
        return map;
    }

    /**
     * Builds constraint_geoid → RID map by querying both DaliPrimaryKey and DaliForeignKey.
     * DaliConstraint is an abstract supertype — ArcadeDB stores concrete subtypes only.
     */
    private Map<String, String> buildConstraintRidMap(String sid) {
        Map<String, String> map = new HashMap<>();
        for (String type : new String[]{"DaliPrimaryKey", "DaliForeignKey", "DaliUniqueConstraint", "DaliCheckConstraint"}) {
            try {
                var rs = db.query("sql",
                        "SELECT @rid AS rid, constraint_geoid FROM " + type + " WHERE session_id = :sid",
                        Map.of("sid", sid));
                while (rs.hasNext()) {
                    var doc = rs.next().toMap();
                    String key = (String) doc.get("constraint_geoid");
                    String rid = (String) doc.get("rid");
                    if (key != null && rid != null) map.put(key, rid);
                }
            } catch (Exception e) {
                logger.warn("Constraint RID map failed for {}: {}", type, e.getMessage());
            }
        }
        return map;
    }

    /**
     * Ad-hoc mode: resolves canonical vertex deduplication before the NDJSON batch.
     *
     * <p>Strategy:
     * <ol>
     *   <li>DaliSchema — pre-inserted via individual rcmd() with DuplicatedKeyException handling.
     *       Then all DaliSchema RIDs are queried and returned.
     *   <li>DaliTable / DaliColumn — queried for pre-existing records (those from prior files
     *       in the same batch). Existing geoids are returned so the batch builder can skip them.
     *       New tables/columns (not yet in DB) are still included in the batch payload.
     * </ol>
     *
     * <p>The returned map (geoid → actual DB RID) is merged into {@code canonicalRids} of the
     * {@link JsonlBatchBuilder}, causing those vertices to be skipped from the batch payload
     * and edges to use actual DB RIDs instead of batch-local temporary IDs.
     *
     * @return geoid → actual DB RID for all pre-existing canonical vertices.
     *         Empty map if {@code str} has no canonical vertices or queries fail.
     */
    /**
     * Result of ad-hoc canonical pre-insertion: RID map + which schema/table/column geoids
     * are brand-new (inserted this session, not pre-existing).
     */
    private record AdHocInsertResult(Map<String, String> rids, Set<String> newSchemaGeoids,
                                     Set<String> newTableGeoids, Set<String> newColumnGeoids) {}

    /**
     * Ad-hoc mode: pre-inserts canonical objects (DaliSchema, DaliTable, DaliColumn) before
     * the NDJSON batch to ensure idempotency across sessions.
     *
     * <p>Strategy per type:
     * <ol>
     *   <li>Try INSERT for each object.
     *   <li>On DuplicatedKeyException → already exists; log debug.
     *       If the current session defines the table/column as 'master' (DDL), UPDATE data_source.
     *   <li>Query actual RIDs of all wanted objects and return them in the result map.
     *       The batch builder registers these in canonicalRids → skips those vertices and uses
     *       their actual RIDs for edge resolution.
     * </ol>
     *
     * <p>This prevents DuplicatedKeyException in the batch when the same column/table appears
     * in multiple concurrent sessions.
     */
    @SuppressWarnings("unchecked")
    /**
     * Builds a SQL IN-list literal from a set of string geoids, with single-quote escaping.
     * Returns an empty string for an empty/null input — callers must check before composing
     * the SQL to avoid emitting an invalid {@code IN []} clause.
     */
    private static String sqlInList(Set<String> values) {
        if (values == null || values.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(values.size() * 32);
        for (String v : values) {
            if (v == null) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append('\'').append(v.replace("'", "''")).append('\'');
        }
        return sb.toString();
    }

    private AdHocInsertResult preInsertAdHocSchemas(String sid, Structure str) {
        if (str == null) return new AdHocInsertResult(Map.of(), Set.of(), Set.of(), Set.of());
        Map<String, String> result = new HashMap<>();
        Set<String> newSchemaGeoids  = new LinkedHashSet<>();
        Set<String> newTableGeoids   = new LinkedHashSet<>();
        Set<String> newColumnGeoids  = new LinkedHashSet<>();

        // ── DaliSchema: pre-insert individually ──────────────────────────────────
        if (str.getSchemas() != null && !str.getSchemas().isEmpty()) {
            for (var e : str.getSchemas().entrySet()) {
                Map<String, Object> sc = (Map<String, Object>) e.getValue();
                try {
                    rcmd("INSERT INTO DaliSchema SET session_id=?, schema_geoid=?, schema_name=?, db_name=?, db_geoid=?",
                            sid, e.getKey(), sc.get("name"), null, null);
                    newSchemaGeoids.add(e.getKey());
                } catch (RuntimeException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                        logger.debug("[ad-hoc] DaliSchema '{}' already exists — reusing", e.getKey());
                    } else {
                        throw ex;
                    }
                }
            }
            try {
                // BOUNDED lookup: filter by IN [list] + explicit LIMIT to bypass ArcadeDB's
                // default page cap on unbounded SELECTs (which otherwise truncates the
                // result set and leaves geoids out of `result`, causing JsonlBatchBuilder
                // to re-INSERT them and hit DuplicatedKeyException on (db_name,*_geoid)).
                Set<String> wanted = str.getSchemas().keySet();
                if (!wanted.isEmpty()) {
                    var rs = db.query("sql",
                            "SELECT @rid AS rid, schema_geoid FROM DaliSchema " +
                            "WHERE db_name IS NULL AND schema_geoid IN [" + sqlInList(wanted) + "] " +
                            "LIMIT " + (wanted.size() * 2 + 100),
                            Map.of());
                    while (rs.hasNext()) {
                        var doc = rs.next().toMap();
                        String geoid = (String) doc.get("schema_geoid");
                        String rid   = (String) doc.get("rid");
                        if (geoid != null && rid != null && wanted.contains(geoid)) {
                            result.put(geoid, rid);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("[ad-hoc] Failed to query DaliSchema RIDs: {}", ex.getMessage());
            }
        }

        // ── DaliTable: pre-insert individually with duplicate-key resilience ─────
        // This prevents DuplicatedKeyException in the NDJSON batch when the same table
        // is referenced by multiple concurrent sessions.
        if (str.getTables() != null && !str.getTables().isEmpty()) {
            for (var e : str.getTables().entrySet()) {
                TableInfo t = e.getValue();
                boolean tblMaster = isMasterTable(e.getKey(), str);
                boolean isView    = isViewTable(e.getKey(), str);
                String effectiveType = isView ? "VIEW" : t.tableType();
                String ds = tblMaster ? MASTER : RECONSTRUCTED;
                try {
                    rcmd("INSERT INTO DaliTable SET session_id=?, table_geoid=?, table_name=?, schema_geoid=?, table_type=?, aliases=?, column_count=?, data_source=?",
                            sid, e.getKey(), t.tableName(), t.schemaGeoid(), effectiveType,
                            toJson(new ArrayList<>(t.aliases())), t.columnCount(), ds);
                    newTableGeoids.add(e.getKey());
                } catch (RuntimeException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                        logger.debug("[ad-hoc] DaliTable '{}' already exists — reusing", e.getKey());
                        // Upgrade reconstructed → master if DDL now defines this table
                        if (tblMaster) {
                            try {
                                rcmd("UPDATE DaliTable SET data_source=? WHERE db_name IS NULL AND table_geoid=? AND (data_source IS NULL OR data_source <> ?)",
                                        MASTER, e.getKey(), MASTER);
                            } catch (Exception upEx) {
                                logger.warn("[ad-hoc] Failed to upgrade DaliTable '{}' to master: {}", e.getKey(), upEx.getMessage());
                            }
                        }
                    } else {
                        throw ex;
                    }
                }
            }
            try {
                // BOUNDED lookup (see DaliSchema block above for rationale).
                Set<String> wanted = str.getTables().keySet();
                if (!wanted.isEmpty()) {
                    var rs = db.query("sql",
                            "SELECT @rid AS rid, table_geoid FROM DaliTable " +
                            "WHERE db_name IS NULL AND table_geoid IN [" + sqlInList(wanted) + "] " +
                            "LIMIT " + (wanted.size() * 2 + 100),
                            Map.of());
                    while (rs.hasNext()) {
                        var doc = rs.next().toMap();
                        String geoid = (String) doc.get("table_geoid");
                        String rid   = (String) doc.get("rid");
                        if (geoid != null && rid != null && wanted.contains(geoid)) {
                            result.put(geoid, rid);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("[ad-hoc] Failed to query DaliTable RIDs: {}", ex.getMessage());
            }
        }

        // ── DaliColumn: pre-insert individually with duplicate-key resilience ────
        if (str.getColumns() != null && !str.getColumns().isEmpty()) {
            for (var e : str.getColumns().entrySet()) {
                ColumnInfo c = e.getValue();
                boolean colMaster = isMasterTable(c.getTableGeoid(), str);
                String ds = colMaster ? MASTER : RECONSTRUCTED;
                try {
                    rcmd("INSERT INTO DaliColumn SET session_id=?, column_geoid=?, table_geoid=?, column_name=?, expression=?, alias=?, is_output=?, col_order=?, ordinal_position=?, used_in_statements=?, data_source=?, data_type=?, is_required=?, default_value=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=?",
                            sid, e.getKey(), c.getTableGeoid(), c.getColumnName(),
                            c.getExpression(), c.getAlias(), c.isOutput(), c.getOrder(),
                            c.getOrdinalPosition(),
                            toJson(new ArrayList<>(c.getUsedInStatements())),
                            ds, c.getDataType(), c.isRequired(), c.getDefaultValue(),
                            c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn());
                    newColumnGeoids.add(e.getKey());
                } catch (RuntimeException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                        logger.debug("[ad-hoc] DaliColumn '{}' already exists — reusing", e.getKey());
                        // Upgrade reconstructed → master if DDL now defines this column's table
                        try {
                            String upd = colMaster
                                ? "UPDATE DaliColumn SET data_source=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=? WHERE db_name IS NULL AND column_geoid=?"
                                : "UPDATE DaliColumn SET is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=? WHERE db_name IS NULL AND column_geoid=? AND (is_pk = false AND is_fk = false)";
                            if (colMaster) {
                                rcmd(upd, MASTER, c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn(), e.getKey());
                            } else if (c.isPk() || c.isFk()) {
                                rcmd(upd, c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn(), e.getKey());
                            }
                        } catch (Exception upEx) {
                            logger.warn("[ad-hoc] Failed to upgrade DaliColumn '{}': {}", e.getKey(), upEx.getMessage());
                        }
                    } else {
                        throw ex;
                    }
                }
            }
            try {
                // BOUNDED lookup (see DaliSchema block above for rationale). This is the
                // critical fix for "Duplicated key [null, X.Y] on DaliColumn[db_name,column_geoid]"
                // — without the IN-list filter, the SELECT hit ArcadeDB's default page cap
                // and dropped most existing column RIDs from `result`.
                Set<String> wanted = str.getColumns().keySet();
                if (!wanted.isEmpty()) {
                    var rs = db.query("sql",
                            "SELECT @rid AS rid, column_geoid FROM DaliColumn " +
                            "WHERE db_name IS NULL AND column_geoid IN [" + sqlInList(wanted) + "] " +
                            "LIMIT " + (wanted.size() * 2 + 100),
                            Map.of());
                    while (rs.hasNext()) {
                        var doc = rs.next().toMap();
                        String geoid = (String) doc.get("column_geoid");
                        String rid   = (String) doc.get("rid");
                        if (geoid != null && rid != null && wanted.contains(geoid)) {
                            result.put(geoid, rid);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("[ad-hoc] Failed to query DaliColumn RIDs: {}", ex.getMessage());
            }
        }

        logger.debug("[ad-hoc] preInsertAdHocCanonical: {} canonical RIDs resolved (schemas:{} tables:{} columns:{}), new: S:{} T:{} C:{}",
                result.size(),
                str.getSchemas() != null ? str.getSchemas().size() : 0,
                str.getTables()  != null ? str.getTables().size()  : 0,
                str.getColumns() != null ? str.getColumns().size() : 0,
                newSchemaGeoids.size(), newTableGeoids.size(), newColumnGeoids.size());
        return new AdHocInsertResult(result,
                Collections.unmodifiableSet(newSchemaGeoids),
                Collections.unmodifiableSet(newTableGeoids),
                Collections.unmodifiableSet(newColumnGeoids));
    }

    private Map<String, String> buildOcByOrderMap(String sid) {
        Map<String, String> map = new HashMap<>();
        try {
            var rs = db.query("sql",
                    "SELECT @rid AS rid, statement_geoid, col_order FROM DaliOutputColumn WHERE session_id = :sid",
                    Map.of("sid", sid));
            while (rs.hasNext()) {
                var doc = rs.next().toMap();
                String stmtG  = (String) doc.get("statement_geoid");
                Object colOrd = doc.get("col_order");
                String rid    = (String) doc.get("rid");
                if (stmtG != null && colOrd != null && rid != null)
                    map.put(stmtG + ":" + colOrd, rid);
            }
        } catch (Exception e) {
            logger.warn("ocByOrder map failed: {}", e.getMessage());
        }
        return map;
    }

    private Map<String, String> buildAffColMap(String sid) {
        Map<String, String> map = new HashMap<>();
        try {
            var rs = db.query("sql",
                    "SELECT @rid AS rid, statement_geoid, column_ref FROM DaliAffectedColumn WHERE session_id = :sid",
                    Map.of("sid", sid));
            while (rs.hasNext()) {
                var doc = rs.next().toMap();
                String stmtG  = (String) doc.get("statement_geoid");
                String colRef = (String) doc.get("column_ref");
                String rid    = (String) doc.get("rid");
                if (stmtG != null && colRef != null && rid != null)
                    map.put(stmtG + ":" + colRef, rid);
            }
        } catch (Exception e) {
            logger.warn("affCol RID map failed: {}", e.getMessage());
        }
        return map;
    }

    // ═══════════════════════════════════════════════════════════════
    // ensureCanonicalPool — remote half
    // ═══════════════════════════════════════════════════════════════

    CanonicalPool ensurePool(String dbName, String appName, String appGeoid) {
        CanonicalPool pool = new CanonicalPool(dbName);
        String resolvedAppGeoid = (appGeoid != null) ? appGeoid : appName;
        try {
            if (appName != null) {
                try {
                    var rs = db.query("sql",
                            "SELECT @rid AS rid FROM DaliApplication WHERE app_geoid = :g LIMIT 1",
                            Map.of("g", resolvedAppGeoid));
                    if (rs.hasNext()) {
                        String rid = (String) rs.next().toMap().get("rid");
                        if (rid != null) pool.setApplicationRid(rid);
                    }
                } catch (Exception ex) {
                    logger.debug("DaliApplication lookup failed for '{}': {}", appName, ex.getMessage());
                }
                if (pool.getApplicationRid() == null) {
                    rcmd("INSERT INTO DaliApplication SET app_name=?, app_geoid=?, created_at=?",
                            appName, resolvedAppGeoid, System.currentTimeMillis());
                    try {
                        var rs = db.query("sql",
                                "SELECT @rid AS rid FROM DaliApplication WHERE app_geoid = :g LIMIT 1",
                                Map.of("g", resolvedAppGeoid));
                        if (rs.hasNext()) {
                            String rid = (String) rs.next().toMap().get("rid");
                            if (rid != null) pool.setApplicationRid(rid);
                        }
                    } catch (Exception ex) {
                        logger.debug("DaliApplication RID fetch failed for '{}': {}", appName, ex.getMessage());
                    }
                }
            }
            boolean dbIsNew = false;
            try {
                var rs = db.query("sql",
                        "SELECT @rid AS rid FROM DaliDatabase WHERE db_geoid = :g LIMIT 1",
                        Map.of("g", dbName));
                if (rs.hasNext()) {
                    String rid = (String) rs.next().toMap().get("rid");
                    if (rid != null) pool.setDatabaseRid(rid);
                }
            } catch (Exception ex) {
                logger.debug("DaliDatabase lookup failed for '{}': {}", dbName, ex.getMessage());
            }
            if (pool.getDatabaseRid() == null) {
                rcmd("INSERT INTO DaliDatabase SET db_name=?, db_geoid=?, created_at=?",
                        dbName, dbName, System.currentTimeMillis());
                dbIsNew = true;
                try {
                    var rs = db.query("sql",
                            "SELECT @rid AS rid FROM DaliDatabase WHERE db_geoid = :g LIMIT 1",
                            Map.of("g", dbName));
                    if (rs.hasNext()) {
                        String rid = (String) rs.next().toMap().get("rid");
                        if (rid != null) pool.setDatabaseRid(rid);
                    }
                } catch (Exception ex) {
                    logger.debug("DaliDatabase RID fetch failed for '{}': {}", dbName, ex.getMessage());
                }
            }
            if (dbIsNew && pool.getDatabaseRid() != null && pool.getApplicationRid() != null)
                edgeByRid("BELONGS_TO_APP", pool.getDatabaseRid(), pool.getApplicationRid(), dbName);
        } catch (Exception e) {
            logger.warn("ensurePool (remote) failed for '{}': {}", dbName, e.getMessage());
        }
        logger.info("CanonicalPool created: db='{}' app='{}'", dbName, appName != null ? appName : "—");
        return pool;
    }

    // ═══════════════════════════════════════════════════════════════
    // Main write
    // ═══════════════════════════════════════════════════════════════

    void write(String sid, SemanticResult result, PipelineTimer timer,
               CanonicalPool pool, String dbName) {
        Structure str = result.getStructure();
        if (str == null) return;

        timer.start("write.vtx");

        rcmd("INSERT INTO DaliSession SET session_id=?, db_name=?, file_path=?, dialect=?, processing_time_ms=?, created_at=?",
                sid, dbName, result.getFilePath(), result.getDialect(),
                result.getProcessingTimeMs(), System.currentTimeMillis());

        String rawScript = result.getRawScript();
        if (rawScript != null && !rawScript.isBlank()) {
            int lineCount = (int) rawScript.lines().count();
            rcmd("INSERT INTO DaliSnippetScript SET session_id=?, file_path=?, script=?, script_hash=?, line_count=?, char_count=?",
                    sid, result.getFilePath(), rawScript, md5(rawScript), lineCount, rawScript.length());
        }

        // ── DaliDatabase ──
        if (pool == null) {
            for (var e : str.getDatabases().entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> d = (Map<String, Object>) e.getValue();
                boolean dbExists = false;
                try {
                    var rs = db.query("sql",
                            "SELECT @rid FROM DaliDatabase WHERE db_geoid = :g LIMIT 1",
                            Map.of("g", e.getKey()));
                    dbExists = rs.hasNext();
                } catch (Exception ex) {
                    logger.debug("DaliDatabase lookup failed for '{}': {}", e.getKey(), ex.getMessage());
                }
                if (!dbExists)
                    rcmd("INSERT INTO DaliDatabase SET db_geoid=?, db_name=?, created_at=?",
                            e.getKey(), d.get("name"), System.currentTimeMillis());
            }
        }

        // ── DaliSchema ──
        Set<String> newSchemaGeoids = new LinkedHashSet<>();
        Set<String> newTableGeoids  = new LinkedHashSet<>();
        Set<String> newColumnGeoids = new LinkedHashSet<>();

        for (var e : str.getSchemas().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sc = (Map<String, Object>) e.getValue();
            if (pool != null) {
                String cg = pool.canonicalSchema(e.getKey());
                if (!pool.hasSchemaRid(cg)) {
                    try {
                        rcmd("INSERT INTO DaliSchema SET db_name=?, db_geoid=?, schema_geoid=?, schema_name=?",
                                dbName, dbName, e.getKey(), sc.get("name"));
                        pool.putSchemaRid(cg, cg);
                        newSchemaGeoids.add(e.getKey());
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "";
                        if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                            pool.putSchemaRid(cg, cg);
                            logger.debug("[write/pool] DaliSchema '{}' already exists — reusing", e.getKey());
                        } else throw ex;
                    }
                }
            } else {
                String schDbGeoid = (String) sc.get("db");
                @SuppressWarnings("unchecked")
                String schDbName = (schDbGeoid != null && str.getDatabases().containsKey(schDbGeoid))
                        ? (String) ((Map<String, Object>) str.getDatabases().get(schDbGeoid)).get("name")
                        : dbName;
                try {
                    rcmd("INSERT INTO DaliSchema SET session_id=?, schema_geoid=?, schema_name=?, db_name=?, db_geoid=?",
                            sid, e.getKey(), sc.get("name"),
                            schDbName, schDbGeoid != null ? schDbGeoid : schDbName);
                    newSchemaGeoids.add(e.getKey());
                } catch (RuntimeException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                        logger.debug("[write/non-pool] DaliSchema '{}' already exists — reusing", e.getKey());
                    } else throw ex;
                }
            }
        }

        // ── DaliPackage ──
        for (var e : str.getPackages().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = (Map<String, Object>) e.getValue();
            rcmd("INSERT INTO DaliPackage SET session_id=?, package_geoid=?, package_name=?, schema_geoid=?, " +
                            "routine_geoid=?, routine_name=?, routine_type=?",
                    sid, e.getKey(), pkg.get("package_name"), pkg.get("schema_geoid"),
                    e.getKey(), pkg.get("package_name"), "PACKAGE");
        }

        // ── DaliTable ──
        for (var e : str.getTables().entrySet()) {
            TableInfo t = e.getValue();
            boolean tblMaster  = isMasterTable(e.getKey(), str);
            boolean isView     = isViewTable(e.getKey(), str);
            // CREATE VIEW overrides any default 'TABLE' type set during reconstruction
            String  effectiveType = isView ? "VIEW" : t.tableType();
            if (pool != null) {
                String cg = pool.canonical(e.getKey());
                if (!pool.hasTableRid(cg)) {
                    try {
                        rcmd("INSERT INTO DaliTable SET db_name=?, table_geoid=?, table_name=?, schema_geoid=?, table_type=?, aliases=?, column_count=?, data_source=?",
                                dbName, e.getKey(), t.tableName(), t.schemaGeoid(), effectiveType,
                                toJson(new ArrayList<>(t.aliases())), t.columnCount(),
                                tblMaster ? MASTER : RECONSTRUCTED);
                        pool.putTableRid(cg, cg);
                        newTableGeoids.add(e.getKey());
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "";
                        if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                            pool.putTableRid(cg, cg);
                            logger.debug("[write/pool] DaliTable '{}' already exists — reusing", e.getKey());
                        } else throw ex;
                    }
                } else if (tblMaster) {
                    if (isView) {
                        // reconstructed TABLE → master VIEW: update both data_source AND table_type
                        rcmd("UPDATE DaliTable SET data_source=?, table_type=? WHERE db_name=? AND table_geoid=?",
                                MASTER, "VIEW", dbName, e.getKey());
                    } else {
                        // upgrade reconstructed → master, keep table_type as is
                        rcmd("UPDATE DaliTable SET data_source=? WHERE db_name=? AND table_geoid=? AND (data_source IS NULL OR data_source <> ?)",
                                MASTER, dbName, e.getKey(), MASTER);
                    }
                }
            } else {
                // Non-pool (ad-hoc) REMOTE path: handle duplicate gracefully
                try {
                    rcmd("INSERT INTO DaliTable SET session_id=?, table_geoid=?, table_name=?, schema_geoid=?, table_type=?, aliases=?, column_count=?, data_source=?",
                            sid, e.getKey(), t.tableName(), t.schemaGeoid(), effectiveType,
                            toJson(new ArrayList<>(t.aliases())), t.columnCount(),
                            tblMaster ? MASTER : RECONSTRUCTED);
                } catch (RuntimeException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                        logger.debug("[ad-hoc] DaliTable '{}' already exists — reusing", e.getKey());
                        if (tblMaster) {
                            rcmd("UPDATE DaliTable SET data_source=? WHERE db_name IS NULL AND table_geoid=? AND (data_source IS NULL OR data_source <> ?)",
                                    MASTER, e.getKey(), MASTER);
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        }

        // ── DaliColumn ──
        for (var e : str.getColumns().entrySet()) {
            ColumnInfo c = e.getValue();
            boolean colMaster = isMasterTable(c.getTableGeoid(), str);
            if (pool != null) {
                String cg = pool.canonicalCol(c.getTableGeoid(), c.getColumnName());
                if (!pool.hasColumnRid(cg)) {
                    rcmd("INSERT INTO DaliColumn SET db_name=?, column_geoid=?, table_geoid=?, column_name=?, expression=?, alias=?, is_output=?, col_order=?, ordinal_position=?, used_in_statements=?, data_source=?, data_type=?, is_required=?, default_value=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=?",
                            dbName, e.getKey(), c.getTableGeoid(), c.getColumnName(),
                            c.getExpression(), c.getAlias(), c.isOutput(), c.getOrder(),
                            c.getOrdinalPosition(),
                            toJson(new ArrayList<>(c.getUsedInStatements())),
                            colMaster ? MASTER : RECONSTRUCTED,
                            c.getDataType(), c.isRequired(), c.getDefaultValue(),
                            c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn());
                    pool.putColumnRid(cg, cg);
                    newColumnGeoids.add(e.getKey());
                } else if (colMaster) {
                    rcmd("UPDATE DaliColumn SET data_source=? WHERE db_name=? AND column_geoid=? AND (data_source IS NULL OR data_source <> ?)",
                            MASTER, dbName, e.getKey(), MASTER);
                }
            } else {
                // Non-pool (ad-hoc) REMOTE path: handle duplicate gracefully
                try {
                    rcmd("INSERT INTO DaliColumn SET session_id=?, column_geoid=?, table_geoid=?, column_name=?, expression=?, alias=?, is_output=?, col_order=?, ordinal_position=?, used_in_statements=?, data_source=?, data_type=?, is_required=?, default_value=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=?",
                            sid, e.getKey(), c.getTableGeoid(), c.getColumnName(), c.getExpression(), c.getAlias(),
                            c.isOutput(), c.getOrder(), c.getOrdinalPosition(),
                            toJson(new ArrayList<>(c.getUsedInStatements())),
                            colMaster ? MASTER : RECONSTRUCTED,
                            c.getDataType(), c.isRequired(), c.getDefaultValue(),
                            c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn());
                } catch (RuntimeException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                        logger.debug("[ad-hoc] DaliColumn '{}' already exists — reusing", e.getKey());
                        try {
                            String upd = colMaster
                                ? "UPDATE DaliColumn SET data_source=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=? WHERE db_name IS NULL AND column_geoid=?"
                                : "UPDATE DaliColumn SET is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=? WHERE db_name IS NULL AND column_geoid=? AND (is_pk = false AND is_fk = false)";
                            if (colMaster) {
                                rcmd(upd, MASTER, c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn(), e.getKey());
                            } else if (c.isPk() || c.isFk()) {
                                rcmd(upd, c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn(), e.getKey());
                            }
                        } catch (Exception upEx) {
                            logger.warn("[ad-hoc] Failed to upgrade DaliColumn '{}': {}", e.getKey(), upEx.getMessage());
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        }

        // ── DaliRoutine ──
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            rcmd("INSERT INTO DaliRoutine SET session_id=?, routine_geoid=?, routine_name=?, routine_type=?, return_type=?, line_start=?, package_geoid=?, schema_geoid=?, data_source=?, is_pipelined=?, autonomous_transaction=?",
                    sid, e.getKey(), r.getName(), r.getRoutineType(), r.getReturnType(),
                    r.getLineStart() > 0 ? r.getLineStart() : null,
                    r.getPackageGeoid(), r.getSchemaGeoid(), MASTER,
                    r.isPipelined() ? true : null,
                    r.isAutonomousTransaction() ? true : null);
        }
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            for (RoutineInfo.ParameterInfo p : r.getTypedParameters())
                rcmd("INSERT INTO DaliParameter SET session_id=?, routine_geoid=?, param_name=?, param_type=?, param_mode=?",
                        sid, e.getKey(), p.name(), p.type(), p.mode());
            for (RoutineInfo.VariableInfo v : r.getTypedVariables())
                rcmd("INSERT INTO DaliVariable SET session_id=?, routine_geoid=?, var_name=?, var_type=?",
                        sid, e.getKey(), v.name(), v.type());
        }

        // ── DaliStatement ──
        for (var e : str.getStatements().entrySet()) {
            StatementInfo s = e.getValue();
            rcmd("INSERT INTO DaliStatement SET " +
                    "session_id=?, stmt_geoid=?, type=?, subtype=?, " +
                    "line_start=?, line_end=?, " +
                    "parent_statement=?, parent_statement_type=?, " +
                    "routine_geoid=?, short_name=?, " +
                    "aliases=?, child_statements=?, " +
                    "source_table_geoids=?, target_table_geoids=?, source_subquery_geoids=?, " +
                    "source_tables_json=?, target_tables_json=?, " +
                    "is_union=?, is_dml=?, is_ddl=?, " +
                    "has_aggregation=?, has_window=?, has_cte=?, " +
                    "join_count=?, col_count_output=?, col_count_input=?, " +
                    "depth=?, quality=?",
                sid, e.getKey(), s.getType(), s.getSubtype(),
                s.getLineStart(), s.getLineEnd(),
                s.getParentStatementGeoid(), parentType(s.getParentStatementGeoid(), str.getStatements()),
                s.getRoutineGeoid(), s.getShortName(),
                toJson(new ArrayList<>(s.getAliases())),
                toJson(new ArrayList<>(s.getChildStatements())),
                toJson(new ArrayList<>(s.getSourceTables().keySet())),
                toJson(new ArrayList<>(s.getTargetTables().keySet())),
                toJson(new ArrayList<>(s.getSourceSubqueries().keySet())),
                toJson(new ArrayList<>(s.getSourceTables().values())),
                toJson(new ArrayList<>(s.getTargetTables().values())),
                s.isUnion(), isDml(s.getType()), isDdl(s.getType()),
                s.isHasAggregation(), s.isHasWindow(), hasCte(s, str.getStatements()),
                s.getJoins().size(), s.getColumnsOutput().size(), countInputColumns(s),
                computeDepth(s.getParentStatementGeoid(), str.getStatements()),
                computeStatementQuality(s));
        }

        // ── DaliDDLStatement (v27) — separate vertex for schema-mutating DDL ──────
        // Stores ALTER / CREATE / DROP statements independently of DaliStatement.
        // KNOWN ISSUE: edges DaliDDLStatement → DaliTable / DaliColumn are not yet
        // created (ALTER TABLE column change tracking deferred — see known-issues.md).
        for (var e : str.getStatements().entrySet()) {
            StatementInfo s = e.getValue();
            if (!isDdl(s.getType())) continue;
            try {
                if (pool != null) {
                    rcmd("INSERT INTO DaliDDLStatement SET db_name=?, stmt_geoid=?, type=?, line_start=?, line_end=?, target_table_geoids=?, short_name=?",
                            dbName, e.getKey(), s.getType(), s.getLineStart(), s.getLineEnd(),
                            toJson(new ArrayList<>(s.getTargetTables().keySet())), s.getShortName());
                } else {
                    rcmd("INSERT INTO DaliDDLStatement SET session_id=?, stmt_geoid=?, type=?, line_start=?, line_end=?, target_table_geoids=?, short_name=?",
                            sid, e.getKey(), s.getType(), s.getLineStart(), s.getLineEnd(),
                            toJson(new ArrayList<>(s.getTargetTables().keySet())), s.getShortName());
                }
            } catch (RuntimeException ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : "";
                if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                    logger.debug("[ddl] DaliDDLStatement '{}' already exists — skipping", e.getKey());
                } else throw ex;
            }
        }

        // ── DaliPrimaryKey / DaliForeignKey (extends DaliConstraint) ──────────────
        for (var e : str.getConstraints().entrySet()) {
            ConstraintInfo c = e.getValue();
            String colNamesJson = toJson(c.getColumnNames());
            if (c.isPrimaryKey()) {
                if (pool != null) {
                    rcmd("INSERT INTO DaliPrimaryKey SET db_name=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, column_names=?",
                            dbName, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), colNamesJson);
                } else {
                    rcmd("INSERT INTO DaliPrimaryKey SET session_id=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, column_names=?",
                            sid, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), colNamesJson);
                }
            } else if (c.isForeignKey()) {
                String refColNamesJson = toJson(c.getRefColumnNames());
                if (pool != null) {
                    rcmd("INSERT INTO DaliForeignKey SET db_name=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, column_names=?, ref_table_geoid=?, ref_column_names=?, on_delete=?",
                            dbName, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), colNamesJson,
                            c.getRefTableGeoid(), refColNamesJson, c.getOnDelete());
                } else {
                    rcmd("INSERT INTO DaliForeignKey SET session_id=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, column_names=?, ref_table_geoid=?, ref_column_names=?, on_delete=?",
                            sid, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), colNamesJson,
                            c.getRefTableGeoid(), refColNamesJson, c.getOnDelete());
                }
            } else if (c.isUniqueConstraint()) {
                // KI-005: DaliUniqueConstraint
                if (pool != null) {
                    rcmd("INSERT INTO DaliUniqueConstraint SET db_name=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, column_names=?",
                            dbName, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), colNamesJson);
                } else {
                    rcmd("INSERT INTO DaliUniqueConstraint SET session_id=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, column_names=?",
                            sid, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), colNamesJson);
                }
            } else if (c.isCheckConstraint()) {
                // KI-005: DaliCheckConstraint
                if (pool != null) {
                    rcmd("INSERT INTO DaliCheckConstraint SET db_name=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, check_expression=?",
                            dbName, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), c.getCheckExpression());
                } else {
                    rcmd("INSERT INTO DaliCheckConstraint SET session_id=?, constraint_geoid=?, constraint_type=?, constraint_name=?, table_geoid=?, check_expression=?",
                            sid, e.getKey(), c.getConstraintType(), c.getConstraintName(),
                            c.getHostTableGeoid(), c.getCheckExpression());
                }
            }
        }

        // ── DaliAffectedColumn ──
        for (var e : str.getStatements().entrySet()) {
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String typeAffect  = null;
                Integer orderAffect = null;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> poliage = (List<Map<String, Object>>) ac.get("poliage_update");
                if (poliage != null && !poliage.isEmpty()) {
                    typeAffect  = (String)  poliage.get(0).get("type_affect");
                    Object oa   = poliage.get(0).get("order_affect");
                    orderAffect = oa instanceof Number n ? n.intValue() : null;
                }
                rcmd("INSERT INTO DaliAffectedColumn SET session_id=?, statement_geoid=?, " +
                        "column_ref=?, column_name=?, table_geoid=?, dataset_alias=?, " +
                        "source_type=?, resolution_status=?, type_affect=?, order_affect=?",
                    sid, e.getKey(),
                    ac.get("column_ref"), ac.get("column_name"), ac.get("table_geoid"),
                    ac.get("dataset_alias"), ac.get("source_type"), ac.get("resolution_status"),
                    typeAffect, orderAffect);
            }
        }

        // ── DaliRecord + DaliRecordField (G6: BULK COLLECT / KI-RETURN-1) ──
        Set<String> insertedFieldGeoids = new HashSet<>(); // dedup within batch
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            String fieldsJson = String.join(",", rec.getFields());
            rcmd("INSERT INTO DaliRecord SET session_id=?, record_geoid=?, record_name=?, " +
                    "routine_geoid=?, source_stmt_geoid=?, fields=?",
                sid, rec.getGeoid(), rec.getVarName(),
                rec.getRoutineGeoid(), rec.getSourceStatementGeoid(), fieldsJson);
            // DaliRecordField — one vertex per named field (dedup across batch)
            for (RecordInfo.FieldInfo fi : rec.getFieldInfos()) {
                String fieldGeoid = rec.getGeoid() + ":FIELD:" + fi.name();
                if (!insertedFieldGeoids.add(fieldGeoid)) continue; // already inserted
                rcmd("INSERT INTO DaliRecordField SET session_id=?, field_geoid=?, field_name=?, " +
                        "field_order=?, record_geoid=?, data_type=?, ordinal_position=?, source_column_geoid=?",
                    sid, fieldGeoid, fi.name(), fi.ordinalPosition(), rec.getGeoid(),
                    fi.dataType(), fi.ordinalPosition(), fi.sourceColumnGeoid());
            }
        }

        // ── DaliSnippet ──
        for (var e : str.getStatements().entrySet()) {
            String raw = truncate(e.getValue().getSnippet(), SNIPPET_MAX);
            if (raw == null) continue;
            rcmd("INSERT INTO DaliSnippet SET session_id=?, stmt_geoid=?, snippet=?, snippet_hash=?, line_start=?, line_end=?",
                    sid, e.getKey(), raw, md5(raw),
                    e.getValue().getLineStart(), e.getValue().getLineEnd());
        }

        // ── DaliOutputColumn ──
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                Map<String, Object> col = oc.getValue();
                rcmd("INSERT INTO DaliOutputColumn SET session_id=?, statement_geoid=?, col_key=?, " +
                        "name=?, expression=?, alias=?, col_order=?, source_type=?, table_ref=?",
                    sid, e.getKey(), oc.getKey(), col.get("name"), col.get("expression"),
                    col.get("alias"), col.get("order"), col.get("source_type"), col.get("table_ref"));
            }
        }

        // ── DaliAtom ── skip "routine" containers (duplicate view of statement atoms)
        for (var container : result.getAtoms().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cont = (Map<String, Object>) container.getValue();
            String sourceTypeAtom = (String) cont.get("source_type");
            if ("routine".equals(sourceTypeAtom)) continue;
            String stmtGeoid = (String) cont.get("source_geoid");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) cont.get("atoms");
            if (atoms == null || stmtGeoid == null) continue;
            for (var at : atoms.entrySet()) {
                Map<String, Object> a = at.getValue();
                String atomId = md5(stmtGeoid + ":" + at.getKey());
                // Detect AtomInfo.STATUS_UNBOUND for column-reference atoms whose DaliColumn is absent from schema
                String atomColForWarn = (String) a.get("column_name");
                String atomTblForWarn = (String) a.get("table_geoid");
                boolean isColRefRw = Boolean.TRUE.equals(a.get("is_column_reference"));
                if (a.get("warning") == null && AtomInfo.STATUS_RESOLVED.equals(a.get("status"))
                        && isColRefRw && atomTblForWarn != null && atomColForWarn != null
                        && !str.getColumns().containsKey(atomTblForWarn + "." + atomColForWarn.toUpperCase())) {
                    a.put("warning", AtomInfo.STATUS_UNBOUND);
                }

                rcmd("INSERT INTO DaliAtom SET session_id=?, statement_geoid=?, atom_id=?, atom_text=?, " +
                        "atom_context=?, parent_context=?, position=?, sposition=?, " +
                        "is_complex=?, is_column_reference=?, is_function_call=?, is_constant=?, " +
                        "is_routine_param=?, is_routine_var=?, table_name=?, column_name=?, " +
                        "table_geoid=?, status=?, warning=?, merge_clause=?, output_column_sequence=?, nested_atoms_count=?",
                    sid, stmtGeoid, atomId, at.getKey(),
                    a.get("atom_context"), a.get("parent_context"), a.get("position"), a.get("sposition"),
                    a.get("is_complex"), a.get("is_column_reference"), a.get("is_function_call"),
                    a.get("is_constant"), a.get("is_routine_param"), a.get("is_routine_var"),
                    a.get("table_name"), a.get("column_name"),
                    a.get("table_geoid"), a.get("status"), a.get("warning"), a.get("merge_clause"),
                    a.get("output_column_sequence"), a.get("nested_atoms_count"));
            }
        }

        // ── DaliJoin ──
        for (var e : str.getStatements().entrySet()) {
            for (JoinInfo j : e.getValue().getJoins()) {
                rcmd("INSERT INTO DaliJoin SET session_id=?, statement_geoid=?, join_type=?, " +
                        "source_table_geoid=?, source_alias=?, source_type=?, " +
                        "target_table_geoid=?, target_alias=?, target_type=?, " +
                        "conditions=?, line_start=?",
                    sid, e.getKey(), j.joinType(),
                    j.sourceTableGeoid(), j.sourceTableAlias(), j.sourceType(),
                    j.targetTableGeoid(), j.targetTableAlias(), j.targetType(),
                    j.conditions(), j.lineStart());
            }
        }

        timer.stop("write.vtx");
        timer.start("write.edge");

        // ── RID cache (namespace-aware) ──
        RidCache rid = buildRidCache(sid, pool, dbName);

        // ── Hierarchy edges ──
        if (pool != null && pool.getDatabaseRid() != null) {
            for (String schGeoid : newSchemaGeoids) {
                String schRid = rid.schemas.get(schGeoid.toUpperCase());
                if (schRid != null) edgeByRid("CONTAINS_SCHEMA", pool.getDatabaseRid(), schRid, sid);
            }
        }
        if (pool != null) {
            for (String tblGeoid : newTableGeoids) {
                TableInfo t = str.getTables().get(tblGeoid);
                if (t == null || t.schemaGeoid() == null) continue;
                String schRid = rid.schemas.get(t.schemaGeoid().toUpperCase());
                String tblRid = rid.tables.get(tblGeoid);
                if (schRid != null && tblRid != null) edgeByRid("CONTAINS_TABLE", schRid, tblRid, sid);
            }
        }
        for (var e : str.getPackages().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = (Map<String, Object>) e.getValue();
            String sg = (String) pkg.get("schema_geoid");
            if (sg != null && !sg.isEmpty()) {
                String fromRid = rid.schemas.get(sg.toUpperCase());
                String toRid   = rid.packages.get(e.getKey());
                if (fromRid != null && toRid != null) edgeByRid("CONTAINS_ROUTINE", fromRid, toRid, sid);
            }
        }
        if (pool == null) {
            for (var e : str.getTables().entrySet()) {
                String sg = e.getValue().schemaGeoid();
                if (sg != null) {
                    String fromRid = rid.schemas.get(sg.toUpperCase());
                    String toRid   = rid.tables.get(e.getKey());
                    if (fromRid != null && toRid != null) edgeByRid("CONTAINS_TABLE", fromRid, toRid, sid);
                }
            }
        }
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            String fromRid = r.getPackageGeoid() != null
                    ? rid.packages.get(r.getPackageGeoid().toUpperCase())
                    : (r.getSchemaGeoid() != null ? rid.schemas.get(r.getSchemaGeoid().toUpperCase()) : null);
            String toRid = rid.routines.get(e.getKey());
            if (fromRid != null && toRid != null) edgeByRid("CONTAINS_ROUTINE", fromRid, toRid, sid);
        }
        for (var e : str.getRoutines().entrySet()) {
            RoutineInfo r = e.getValue();
            if (r.getParentRoutineGeoid() != null) {
                String fromRid = rid.routines.get(r.getParentRoutineGeoid());
                String toRid   = rid.routines.get(e.getKey());
                if (fromRid != null && toRid != null) edgeByRid("NESTED_IN", fromRid, toRid, sid);
            }
        }

        // ── HAS_COLUMN ──
        if (pool != null) {
            // 1. New columns — standard path.
            for (String colGeoid : newColumnGeoids) {
                ColumnInfo c = str.getColumns().get(colGeoid);
                if (c == null) continue;
                String fromRid = rid.tables.get(c.getTableGeoid());
                String toRid   = rid.columns.get(colGeoid);
                if (fromRid != null && toRid != null) edgeByRid("HAS_COLUMN", fromRid, toRid, sid);
            }
            // 2. All existing columns of newly-created tables.
            // Handles geoid-corrected tables (e.g. double-schema fix) where DaliTable is new
            // but DaliColumn records already exist in the DB and are absent from newColumnGeoids.
            // rid.columns covers ALL columns for db_name — sweep by column_geoid prefix.
            if (!newTableGeoids.isEmpty()) {
                Set<String> skipGeoids = new HashSet<>(newColumnGeoids); // avoid duplicates
                for (String tblGeoid : newTableGeoids) {
                    String fromRid = rid.tables.get(tblGeoid);
                    if (fromRid == null) continue;
                    String prefix = tblGeoid + ".";
                    for (var e : rid.columns.entrySet()) {
                        if (!skipGeoids.contains(e.getKey()) && e.getKey().startsWith(prefix))
                            edgeByRid("HAS_COLUMN", fromRid, e.getValue(), sid);
                    }
                }
            }
        } else {
            for (var e : str.getColumns().entrySet()) {
                String fromRid = rid.tables.get(e.getValue().getTableGeoid());
                String toRid   = rid.columns.get(e.getKey());
                if (fromRid != null && toRid != null) edgeByRid("HAS_COLUMN", fromRid, toRid, sid);
            }
        }

        // ── Statement hierarchy ──
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getParentStatementGeoid() != null) {
                String fromRid = rid.statements.get(e.getKey());
                String toRid   = rid.statements.get(e.getValue().getParentStatementGeoid());
                if (fromRid != null && toRid != null) edgeByRid("CHILD_OF", fromRid, toRid, sid);
            }
        }
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getRoutineGeoid() != null) {
                String fromRid = rid.routines.get(e.getValue().getRoutineGeoid());
                String toRid   = rid.statements.get(e.getKey());
                if (fromRid != null && toRid != null) edgeByRid("CONTAINS_STMT", fromRid, toRid, sid);
            }
        }
        for (var e : str.getStatements().entrySet()) {
            String stmtRid = rid.statements.get(e.getKey());
            if (stmtRid == null) continue;
            for (var st : e.getValue().getSourceTables().entrySet()) {
                String toRid = rid.tables.get(st.getKey());
                if (toRid == null) continue;
                @SuppressWarnings("unchecked")
                List<String> stAliases = st.getValue().get("table_alias") instanceof List
                        ? (List<String>) st.getValue().get("table_alias") : List.of();
                edgeByRid("READS_FROM", stmtRid, toRid, sid, "aliases", new ArrayList<>(stAliases));
            }
            for (var tt : e.getValue().getTargetTables().entrySet()) {
                String toRid = rid.tables.get(tt.getKey());
                if (toRid == null) continue;
                @SuppressWarnings("unchecked")
                List<String> ttAliases = tt.getValue().get("table_alias") instanceof List
                        ? (List<String>) tt.getValue().get("table_alias") : List.of();
                edgeByRid("WRITES_TO", stmtRid, toRid, sid, "aliases", new ArrayList<>(ttAliases));
            }
        }

        // ── BELONGS_TO_SESSION ──
        for (String rg : str.getRoutines().keySet())
            edgeRemote("BELONGS_TO_SESSION", "DaliSession", "session_id", sid,
                    "DaliRoutine", "routine_geoid", rg, sid);
        if (rid.sessionRid != null) {
            for (String entityRid : rid.schemas.values())
                edgeByRid("BELONGS_TO_SESSION", rid.sessionRid, entityRid, sid);
            for (String entityRid : rid.statements.values())
                edgeByRid("BELONGS_TO_SESSION", rid.sessionRid, entityRid, sid);
        }

        // ── HAS_PARAMETER / HAS_VARIABLE ──
        for (var e : str.getRoutines().entrySet()) {
            if (!e.getValue().getTypedParameters().isEmpty())
                edgeRemote("HAS_PARAMETER", "DaliRoutine", "routine_geoid", e.getKey(),
                        "DaliParameter", "routine_geoid", e.getKey(), sid);
            if (!e.getValue().getTypedVariables().isEmpty())
                edgeRemote("HAS_VARIABLE", "DaliRoutine", "routine_geoid", e.getKey(),
                        "DaliVariable", "routine_geoid", e.getKey(), sid);
        }

        // ── HAS_OUTPUT_COL ──
        for (var e : str.getStatements().entrySet()) {
            String stmtRid = rid.statements.get(e.getKey());
            if (stmtRid == null) continue;
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                String ocRid = rid.outputCols.get(oc.getKey());
                if (ocRid != null) edgeByRid("HAS_OUTPUT_COL", stmtRid, ocRid, sid, "statement_geoid", e.getKey());
            }
        }

        // ── Atom resolution edges ──
        Map<String, String> ocByStmtAndName = new HashMap<>();
        for (var e : str.getStatements().entrySet()) {
            for (var oc : e.getValue().getColumnsOutput().entrySet()) {
                String ocRid = rid.outputCols.get(oc.getKey());
                if (ocRid != null)
                    ocByStmtAndName.put(e.getKey() + ":" + oc.getKey().toUpperCase(), ocRid);
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
            String stmtRid = rid.statements.get(stmtGeoid);
            if (stmtRid == null) continue;

            for (var at : atoms.entrySet()) {
                Map<String, Object> a = at.getValue();
                String atomId  = md5(stmtGeoid + ":" + at.getKey());
                String atomRid = rid.atoms.get(atomId);
                if (atomRid != null) edgeByRid("HAS_ATOM", stmtRid, atomRid, sid);

                String tableGeoid = (String) a.get("table_geoid");
                if (tableGeoid != null && atomRid != null) {
                    String tblRid = rid.tables.get(tableGeoid);
                    if (tblRid != null) {
                        edgeByRid("ATOM_REF_TABLE", atomRid, tblRid, sid);
                        String colName = (String) a.get("column_name");
                        if (colName != null) {
                            String colRid = rid.columns.get(tableGeoid + "." + colName.toUpperCase());
                            if (colRid != null) edgeByRid("ATOM_REF_COLUMN", atomRid, colRid, sid);
                        }
                    } else {
                        String sqStmtRid = rid.statements.get(tableGeoid);
                        if (sqStmtRid != null) {
                            edgeByRid("ATOM_REF_STMT", atomRid, sqStmtRid, sid);
                            String colName = (String) a.get("column_name");
                            if (colName != null) {
                                String ocRid = ocByStmtAndName.get(tableGeoid + ":" + colName.toUpperCase());
                                if (ocRid != null) edgeByRid("ATOM_REF_OUTPUT_COL", atomRid, ocRid, sid);
                            }
                        }
                    }
                }

                Object outSeq = a.get("output_column_sequence");
                if (outSeq != null && atomRid != null) {
                    String ocRid = rid.ocByOrder.get(stmtGeoid + ":" + outSeq);
                    if (ocRid != null) edgeByRid("ATOM_PRODUCES", atomRid, ocRid, sid);
                }
                // ATOM_PRODUCES for DML target (UPDATE SET / MERGE / INSERT)
                String dmlTargetRefProd = (String) a.get("dml_target_ref");
                if (dmlTargetRefProd != null && atomRid != null) {
                    String acRidProd = rid.affCols.get(stmtGeoid + ":" + dmlTargetRefProd);
                    String mergeClause = (String) a.get("merge_clause");
                    if (acRidProd != null) {
                        if (mergeClause != null)
                            edgeByRid("ATOM_PRODUCES", atomRid, acRidProd, sid, "merge_clause", mergeClause);
                        else
                            edgeByRid("ATOM_PRODUCES", atomRid, acRidProd, sid);
                    }
                }
                if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && outSeq != null && tableGeoid != null && atomRid != null) {
                    String colName = (String) a.get("column_name");
                    if (colName != null) {
                        String colRid = rid.columns.get(tableGeoid + "." + colName.toUpperCase());
                        String ocRid  = rid.ocByOrder.get(stmtGeoid + ":" + outSeq);
                        if (colRid != null && ocRid != null)
                            edgeByRid("DATA_FLOW", colRid, ocRid, sid,
                                    "statement_geoid", stmtGeoid, "atom_id", atomId,
                                    "flow_type", resolveFlowType(a, str.getStatements().get(stmtGeoid)));
                    }
                }
                String dmlTargetRef = (String) a.get("dml_target_ref");
                if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && dmlTargetRef != null
                        && tableGeoid != null && atomRid != null) {
                    String colName2 = (String) a.get("column_name");
                    if (colName2 != null) {
                        String colRid2 = rid.columns.get(tableGeoid + "." + colName2.toUpperCase());
                        String acRid   = rid.affCols.get(stmtGeoid + ":" + dmlTargetRef);
                        if (colRid2 != null && acRid != null)
                            edgeByRid("DATA_FLOW", colRid2, acRid, sid,
                                    "statement_geoid", stmtGeoid, "atom_id", atomId,
                                    "flow_type", resolveDmlFlowType(str.getStatements().get(stmtGeoid)));
                    }
                }
                String parentCtx = (String) a.get("parent_context");
                if (("WHERE".equals(parentCtx) || "HAVING".equals(parentCtx) || "JOIN".equals(parentCtx))
                        && AtomInfo.STATUS_RESOLVED.equals(a.get("status")) && tableGeoid != null && atomRid != null) {
                    String colName = (String) a.get("column_name");
                    if (colName != null) {
                        String colRid = rid.columns.get(tableGeoid + "." + colName.toUpperCase());
                        if (colRid != null) edgeByRid("FILTER_FLOW", colRid, stmtRid, sid,
                                "filter_type",     parentCtx,
                                "statement_geoid", stmtGeoid,
                                "via_atom",        at.getKey());
                    }
                }
            }
        }

        // ── HAS_JOIN ──
        for (var e : str.getStatements().entrySet()) {
            for (JoinInfo j : e.getValue().getJoins()) {
                if (j.targetTableGeoid() != null)
                    edgeRemote("HAS_JOIN", "DaliStatement", "stmt_geoid", e.getKey(),
                            "DaliJoin", "statement_geoid", e.getKey(), sid, "statement_geoid", e.getKey());
            }
        }

        // ── USES_SUBQUERY ──
        for (var e : str.getStatements().entrySet()) {
            String fromRid = rid.statements.get(e.getKey());
            if (fromRid == null) continue;
            for (var sq : e.getValue().getSourceSubqueries().entrySet()) {
                String toRid = rid.statements.get(sq.getKey());
                if (toRid == null) continue;
                edgeByRid("USES_SUBQUERY", fromRid, toRid, sid,
                        "aliases",       sq.getValue().subqueryAliases(),
                        "subquery_type", sq.getValue().subqueryType());
            }
        }

        // ── Constraint edges (HAS_PRIMARY_KEY, HAS_FOREIGN_KEY, HAS_UNIQUE_KEY, HAS_CHECK,
        //    IS_PK/FK/UNIQUE_COLUMN, REFERENCES_*) ──
        for (var e : str.getConstraints().entrySet()) {
            ConstraintInfo c = e.getValue();
            String constraintRid = rid.constraints.get(e.getKey());
            if (constraintRid == null) continue;

            // Table → Constraint edge
            String hostTableRid = rid.tables.get(c.getHostTableGeoid());
            if (hostTableRid != null) {
                String tableEdge;
                if      (c.isPrimaryKey())       tableEdge = "HAS_PRIMARY_KEY";
                else if (c.isForeignKey())        tableEdge = "HAS_FOREIGN_KEY";
                else if (c.isUniqueConstraint())  tableEdge = "HAS_UNIQUE_KEY";
                else if (c.isCheckConstraint())   tableEdge = "HAS_CHECK";
                else tableEdge = null;
                if (tableEdge != null) edgeByRid(tableEdge, hostTableRid, constraintRid, sid);
            }

            // Constraint → Column edges (PK, FK, UQ only)
            String colEdge;
            if      (c.isPrimaryKey())      colEdge = "IS_PK_COLUMN";
            else if (c.isForeignKey())       colEdge = "IS_FK_COLUMN";
            else if (c.isUniqueConstraint()) colEdge = "IS_UNIQUE_COLUMN";
            else colEdge = null;
            if (colEdge != null) {
                for (int i = 0; i < c.getColumnNames().size(); i++) {
                    String colGeoid = c.getHostTableGeoid() + "." + c.getColumnNames().get(i);
                    String colRid = rid.columns.get(colGeoid);
                    if (colRid != null) edgeByRid(colEdge, constraintRid, colRid, sid, "order_id", i + 1);
                }
            }

            // FK-specific: REFERENCES_TABLE + REFERENCES_COLUMN
            if (c.isForeignKey() && c.getRefTableGeoid() != null) {
                String refTableRid = rid.tables.get(c.getRefTableGeoid());
                if (refTableRid != null) edgeByRid("REFERENCES_TABLE", constraintRid, refTableRid, sid);

                for (int i = 0; i < c.getRefColumnNames().size(); i++) {
                    String refColGeoid = c.getRefTableGeoid() + "." + c.getRefColumnNames().get(i);
                    String refColRid = rid.columns.get(refColGeoid);
                    if (refColRid != null) edgeByRid("REFERENCES_COLUMN", constraintRid, refColRid, sid, "order_id", i + 1);
                }
            }
        }

        // ── KI-DDL-1: DaliDDLModifiesTable / DaliDDLModifiesColumn ──
        for (var e : str.getStatements().entrySet()) {
            StatementInfo ddlSi = e.getValue();
            if (!isDdl(ddlSi.getType())) continue;
            String ddlRid = rid.ddlStatements.get(e.getKey());
            if (ddlRid == null) continue;
            // DaliDDLStatement → DaliTable (target table of ALTER)
            for (String tGeoid : ddlSi.getTargetTables().keySet()) {
                String tRid = rid.tables.get(tGeoid);
                if (tRid != null) edgeByRid("DaliDDLModifiesTable", ddlRid, tRid, sid);
            }
            // DaliDDLStatement → DaliColumn (each ADD/MODIFY/DROP column)
            for (StatementInfo.AffectedColumnGeoid pair : ddlSi.getAffectedColumnGeoids()) {
                String cRid = rid.columns.get(pair.geoid());
                if (cRid != null)
                    edgeByRid("DaliDDLModifiesColumn", ddlRid, cRid, sid,
                              "operation", pair.operation());
            }
        }

        // ── KI-RETURN-1: HAS_RECORD_FIELD ──
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            String recRid = rid.records.get(e.getKey());
            if (recRid == null) continue;
            for (RecordInfo.FieldInfo fi : rec.getFieldInfos()) {
                String fieldGeoid = rec.getGeoid() + ":FIELD:" + fi.name();
                String rfRid = rid.recordFields.get(fieldGeoid);
                if (rfRid != null) edgeByRid("HAS_RECORD_FIELD", recRid, rfRid, sid);
            }
        }

        // ── KI-RETURN-1: RETURNS_INTO ──
        for (var e : str.getStatements().entrySet()) {
            StatementInfo retSi = e.getValue();
            if (retSi.getReturningTargets().isEmpty()) continue;
            String stmtRid = rid.statements.get(e.getKey());
            if (stmtRid == null) continue;
            for (StatementInfo.ReturningTarget rt : retSi.getReturningTargets()) {
                String targetRid = switch (rt.kind()) {
                    case "PARAMETER"    -> rid.parameters.get(rt.targetGeoid());
                    case "RECORD_FIELD" -> rid.recordFields.get(rt.targetGeoid());
                    case "RECORD"       -> rid.records.get(rt.targetGeoid());
                    default             -> rid.variables.get(rt.targetGeoid()); // VARIABLE
                };
                if (targetRid != null)
                    edgeByRid("RETURNS_INTO", stmtRid, targetRid, sid,
                              "returning_exprs", String.join(",", rt.expressions()));
            }
        }

        // ── HAS_AFFECTED_COL ──
        for (var e : str.getStatements().entrySet()) {
            if (e.getValue().getAffectedColumns().isEmpty()) continue;
            String stmtRid = rid.statements.get(e.getKey());
            if (stmtRid != null)
                edgeFromRidToQuery("HAS_AFFECTED_COL", stmtRid,
                        "DaliAffectedColumn", "statement_geoid", e.getKey(), sid);
        }

        // ── AFFECTED_COL_REF_TABLE ──
        {
            Set<String> seen = new LinkedHashSet<>();
            for (var e : str.getStatements().entrySet()) {
                for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                    String tg = (String) ac.get("table_geoid");
                    if (tg != null && seen.add(tg)) {
                        String tblRid = rid.tables.get(tg);
                        if (tblRid != null)
                            edgeFromQueryToRid("AFFECTED_COL_REF_TABLE",
                                    "DaliAffectedColumn", "table_geoid", tg, sid, tblRid);
                    }
                }
            }
        }

        // ── JOIN_SOURCE_TABLE / JOIN_TARGET_TABLE ──
        {
            Set<String> srcSeen = new LinkedHashSet<>();
            Set<String> tgtSeen = new LinkedHashSet<>();
            for (var e : str.getStatements().entrySet()) {
                for (JoinInfo j : e.getValue().getJoins()) {
                    String src = j.sourceTableGeoid();
                    if (src != null && srcSeen.add(src)) {
                        String tblRid = rid.tables.get(src);
                        if (tblRid != null)
                            edgeFromQueryToRid("JOIN_SOURCE_TABLE", "DaliJoin", "source_table_geoid", src, sid, tblRid);
                    }
                    String tgt = j.targetTableGeoid();
                    if (tgt != null && tgtSeen.add(tgt)) {
                        String tblRid = rid.tables.get(tgt);
                        if (tblRid != null)
                            edgeFromQueryToRid("JOIN_TARGET_TABLE", "DaliJoin", "target_table_geoid", tgt, sid, tblRid);
                    }
                }
            }
        }


        // ── BULK_COLLECTS_INTO / RECORD_USED_IN / HAS_RECORD_FIELD (G6: DaliRecord edges) ──
        for (var e : str.getRecords().entrySet()) {
            RecordInfo rec = e.getValue();
            String recRid = rid.records.get(rec.getGeoid());
            if (recRid == null) continue;
            // DaliStatement(cursor SELECT) → BULK_COLLECTS_INTO → DaliRecord
            String srcStmtRid = rid.statements.get(rec.getSourceStatementGeoid());
            if (srcStmtRid != null)
                edgeByRid("BULK_COLLECTS_INTO", srcStmtRid, recRid, sid);
            // DaliRecord → HAS_RECORD_FIELD → DaliRecordField (one per named field)
            for (String fieldName : rec.getFields()) {
                String fieldGeoid = rec.getGeoid() + ":FIELD:" + fieldName;
                String fieldRid = rid.recordFields.get(fieldGeoid);
                if (fieldRid != null)
                    edgeByRid("HAS_RECORD_FIELD", recRid, fieldRid, sid);
            }
            // DaliRecord → RECORD_USED_IN → DaliStatement(INSERT) for each INSERT
            // that references this record via an AffectedColumn with matching dataset_alias
            for (var stmtEntry : str.getStatements().entrySet()) {
                StatementInfo si = stmtEntry.getValue();
                if (!"INSERT".equals(si.getType())) continue;
                boolean usesRecord = si.getAffectedColumns().stream().anyMatch(
                        ac -> rec.getVarName().equals(ac.get("dataset_alias")))
                    || si.getBulkCollectSources().contains(rec.getVarName().toUpperCase());
                if (usesRecord) {
                    String insertRid = rid.statements.get(stmtEntry.getKey());
                    if (insertRid != null)
                        edgeByRid("RECORD_USED_IN", recRid, insertRid, sid);
                }
            }
        }

        // ── CALLS edges ──
        Map<String, String> ridBySimpleName = new HashMap<>();
        for (var rtEntry : rid.routines.entrySet()) {
            String geoid = rtEntry.getKey();
            String simple = geoid.contains(":") ? geoid.substring(geoid.lastIndexOf(':') + 1) : geoid;
            ridBySimpleName.put(simple.toUpperCase(), rtEntry.getValue());
        }
        for (var callerEntry : result.getCalledRoutines().entrySet()) {
            String callerRid = rid.routines.get(callerEntry.getKey());
            if (callerRid == null) continue;
            for (Map<String, String> call : callerEntry.getValue()) {
                String calleeName = call.get("name");
                if (calleeName == null) continue;
                String calleeRid = ridBySimpleName.get(calleeName.toUpperCase());
                // External call: callee not defined in this session →
                // insert a reconstructed stub so the CALLS edge has a target.
                if (calleeRid == null) {
                    String stubGeoid = "EXT:" + calleeName.toUpperCase();
                    rcmd("INSERT INTO DaliRoutine SET session_id=?, routine_geoid=?, routine_name=?, routine_type=?, data_source=?",
                            sid, stubGeoid, calleeName.toUpperCase(), "UNKNOWN", RECONSTRUCTED);
                    try {
                        var rsStub = db.query("sql",
                                "SELECT @rid AS rid FROM DaliRoutine WHERE routine_geoid = :g AND session_id = :s LIMIT 1",
                                Map.of("g", stubGeoid, "s", sid));
                        if (rsStub.hasNext()) calleeRid = (String) rsStub.next().toMap().get("rid");
                    } catch (Exception ex) {
                        logger.debug("CALLS stub lookup failed: {}", ex.getMessage());
                    }
                    if (calleeRid != null) {
                        rid.routines.put(stubGeoid, calleeRid);
                        ridBySimpleName.put(calleeName.toUpperCase(), calleeRid);
                    }
                }
                if (calleeRid != null) {
                    try {
                        db.command("sql",
                            "CREATE EDGE CALLS FROM " + callerRid + " TO " + calleeRid +
                            " SET session_id = :sid, caller_geoid = :caller, callee_name = :callee, line_start = :line",
                            Map.of("sid", sid, "caller", callerEntry.getKey(),
                                   "callee", calleeName, "line", call.get("line")));
                    } catch (Exception e) {
                        logger.debug("CALLS edge failed: {}", e.getMessage());
                    }
                }
            }
        }

        timer.stop("write.edge");
    }

    // ═══════════════════════════════════════════════════════════════
    // REMOTE_BATCH write path
    // ═══════════════════════════════════════════════════════════════

    /**
     * Namespace-aware REMOTE_BATCH write: canonical phase via individual rcmd() calls,
     * session-specific objects via a single HTTP POST batch.
     */
    WriteStats writeBatch(HttpBatchClient client, String sid, SemanticResult result,
                          PipelineTimer timer, CanonicalPool pool, String dbName) {
        Structure str = result.getStructure();
        if (str == null) return new WriteStats();

        timer.start("write.batch");

        final JsonlBatchBuilder builder;

        if (pool != null) {
            // ── Namespace/pool mode ──────────────────────────────────────────────────
            // Phase 1: Insert canonical objects via individual rcmd() calls, with pool dedup.
            Set<String> newSchemaGeoids  = new LinkedHashSet<>();
            Set<String> newTableGeoids   = new LinkedHashSet<>();
            Set<String> newColumnGeoids  = new LinkedHashSet<>();

            for (var e : str.getSchemas().entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sc = (Map<String, Object>) e.getValue();
                String cg = pool.canonicalSchema(e.getKey());
                if (!pool.hasSchemaRid(cg)) {
                    try {
                        rcmd("INSERT INTO DaliSchema SET db_name=?, db_geoid=?, schema_geoid=?, schema_name=?",
                                dbName, dbName, e.getKey(), sc.get("name"));
                        pool.putSchemaRid(cg, cg);
                        newSchemaGeoids.add(e.getKey());
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "";
                        if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                            pool.putSchemaRid(cg, cg); // register to avoid re-attempting
                            logger.debug("[pool] DaliSchema '{}' already exists in db '{}' — reusing", e.getKey(), dbName);
                            // not in newSchemaGeoids → counted as duplicate below
                        } else throw ex;
                    }
                }
            }

            for (var e : str.getTables().entrySet()) {
                TableInfo t = e.getValue();
                String cg = pool.canonical(e.getKey());
                if (!pool.hasTableRid(cg)) {
                    boolean tblMaster = isMasterTable(e.getKey(), str);
                    boolean isView    = isViewTable(e.getKey(), str);
                    String effectiveType = isView ? "VIEW" : t.tableType();
                    try {
                        rcmd("INSERT INTO DaliTable SET db_name=?, table_geoid=?, table_name=?, schema_geoid=?, table_type=?, aliases=?, column_count=?, data_source=?",
                                dbName, e.getKey(), t.tableName(), t.schemaGeoid(), effectiveType,
                                toJson(new ArrayList<>(t.aliases())), t.columnCount(),
                                tblMaster ? MASTER : RECONSTRUCTED);
                        pool.putTableRid(cg, cg);
                        newTableGeoids.add(e.getKey());
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "";
                        if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                            pool.putTableRid(cg, cg);
                            logger.debug("[pool] DaliTable '{}' already exists in db '{}' — reusing", e.getKey(), dbName);
                        } else throw ex;
                    }
                } else if (isMasterTable(e.getKey(), str)) {
                    // upgrade reconstructed → master if DDL now confirms this table
                    rcmd("UPDATE DaliTable SET data_source=? WHERE db_name=? AND table_geoid=? AND (data_source IS NULL OR data_source <> ?)",
                            MASTER, dbName, e.getKey(), MASTER);
                }
            }

            for (var e : str.getColumns().entrySet()) {
                ColumnInfo c = e.getValue();
                String cg = pool.canonicalCol(c.getTableGeoid(), c.getColumnName());
                if (!pool.hasColumnRid(cg)) {
                    boolean colMaster = isMasterTable(c.getTableGeoid(), str);
                    try {
                        rcmd("INSERT INTO DaliColumn SET db_name=?, column_geoid=?, table_geoid=?, column_name=?, expression=?, alias=?, is_output=?, col_order=?, ordinal_position=?, used_in_statements=?, data_source=?, data_type=?, is_required=?, default_value=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=?",
                                dbName, e.getKey(), c.getTableGeoid(), c.getColumnName(),
                                c.getExpression(), c.getAlias(), c.isOutput(), c.getOrder(),
                                c.getOrdinalPosition(),
                                toJson(new ArrayList<>(c.getUsedInStatements())),
                                colMaster ? MASTER : RECONSTRUCTED,
                                c.getDataType(), c.isRequired(), c.getDefaultValue(),
                                c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn());
                        pool.putColumnRid(cg, cg);
                        newColumnGeoids.add(e.getKey());
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() != null ? ex.getMessage() : "";
                        if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key") || msg.contains("Duplicated key")) {
                            pool.putColumnRid(cg, cg);
                            logger.debug("[pool] DaliColumn '{}' already exists in db '{}' — reusing", e.getKey(), dbName);
                            if (c.isPk() || c.isFk()) {
                                try {
                                    rcmd("UPDATE DaliColumn SET is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=? WHERE db_name=? AND column_geoid=? AND (is_pk = false AND is_fk = false)",
                                            c.isPk(), c.isFk(), c.getFkRefTable(), c.getFkRefColumn(), dbName, e.getKey());
                                } catch (Exception upEx) {
                                    logger.warn("[pool] Failed to update PK/FK for DaliColumn '{}': {}", e.getKey(), upEx.getMessage());
                                }
                            }
                        } else throw ex;
                    }
                } else if (isMasterTable(c.getTableGeoid(), str)) {
                    rcmd("UPDATE DaliColumn SET data_source=? WHERE db_name=? AND column_geoid=? AND (data_source IS NULL OR data_source <> ?)",
                            MASTER, dbName, e.getKey(), MASTER);
                }
            }

            // Phase 2: Build PARTIAL RidCache for schemas/tables/columns (actual DB RIDs)
            RidCache rid = buildRidCache(sid, pool, dbName);

            // Phase 3: Create canonical hierarchical edges via rcmd (new objects only)
            if (pool.getDatabaseRid() != null) {
                for (String schGeoid : newSchemaGeoids) {
                    String schRid = rid.schemas.get(schGeoid.toUpperCase());
                    if (schRid != null) edgeByRid("CONTAINS_SCHEMA", pool.getDatabaseRid(), schRid, sid);
                }
            }
            for (String tblGeoid : newTableGeoids) {
                TableInfo t = str.getTables().get(tblGeoid);
                if (t == null || t.schemaGeoid() == null) continue;
                String schRid = rid.schemas.get(t.schemaGeoid().toUpperCase());
                String tblRid = rid.tables.get(tblGeoid);
                if (schRid != null && tblRid != null)
                    edgeByRid("CONTAINS_TABLE", schRid, tblRid, sid);
            }
            // 1. HAS_COLUMN for new columns — standard path.
            for (String colGeoid : newColumnGeoids) {
                ColumnInfo c = str.getColumns().get(colGeoid);
                if (c == null) continue;
                String fromRid = rid.tables.get(c.getTableGeoid());
                String toRid   = rid.columns.get(colGeoid);
                if (fromRid != null && toRid != null)
                    edgeByRid("HAS_COLUMN", fromRid, toRid, sid);
            }
            // 2. HAS_COLUMN for all existing columns of newly-created tables.
            // rid.columns has ALL columns for db_name — sweep by column_geoid prefix.
            if (!newTableGeoids.isEmpty()) {
                Set<String> skipGeoids = new HashSet<>(newColumnGeoids);
                for (String tblGeoid : newTableGeoids) {
                    String fromRid = rid.tables.get(tblGeoid);
                    if (fromRid == null) continue;
                    String prefix = tblGeoid + ".";
                    for (var e : rid.columns.entrySet()) {
                        if (!skipGeoids.contains(e.getKey()) && e.getKey().startsWith(prefix))
                            edgeByRid("HAS_COLUMN", fromRid, e.getValue(), sid);
                    }
                }
            }

            // Phase 4: Build batch for session-specific objects (DaliStatement, DaliAtom,
            // DaliOutputColumn, DaliAffectedColumn, etc.) and send.
            // NOTE (B2): rid.ocByOrder and rid.affCols were built at Phase 2 BEFORE the batch
            // inserts DaliOutputColumn/DaliAffectedColumn, so they are empty in the pool path.
            // Any post-batch code that needs ocByOrder/affCols must call buildOcByOrderMap(sid)
            // and buildAffColMap(sid) separately after client.send().
            builder = JsonlBatchBuilder.buildFromResult(sid, result,
                    rid.tables, rid.columns, rid.schemas);

            // Register canonical type stats (DaliDatabase/Schema/Table/Column are outside the batch
            // in pool mode; we know new vs duplicate from the rcmd phase above).
            if (str.getDatabases() != null)
                str.getDatabases().keySet().forEach(g -> builder.recordDuplicate("DaliDatabase"));
            for (String geoid : str.getSchemas().keySet()) {
                if (newSchemaGeoids.contains(geoid)) builder.recordInserted("DaliSchema");
                else builder.recordDuplicate("DaliSchema");
            }
            for (String geoid : str.getTables().keySet()) {
                if (newTableGeoids.contains(geoid)) builder.recordInserted("DaliTable");
                else builder.recordDuplicate("DaliTable");
            }
            for (String geoid : str.getColumns().keySet()) {
                if (newColumnGeoids.contains(geoid)) builder.recordInserted("DaliColumn");
                else builder.recordDuplicate("DaliColumn");
            }

        } else {
            // ── Ad-hoc mode ──────────────────────────────────────────────────────────
            // Pre-insert DaliSchema, DaliTable, DaliColumn individually with duplicate-key
            // resilience. Without this, two files referencing the same canonical object
            // (schema, table, column) would both try to INSERT in their respective batches
            // → DuplicatedKeyException on the unique (db_name, geoid) index.
            // For master objects (DDL session), pre-insertion also upgrades data_source.
            AdHocInsertResult adHoc = preInsertAdHocSchemas(sid, str);

            // Ad-hoc Phase 3: Create canonical hierarchy edges via individual rcmd() for
            // newly-inserted objects. The batch factory skips canonical vertices AND their
            // hierarchy edges (all geoids are in canonicalRids → `continue` at line 552/588),
            // so CONTAINS_TABLE and HAS_COLUMN must be created here, outside the batch.
            // Mirrors namespace-mode Phase 3 (lines ~1473–1503) but uses adHoc.rids()
            // instead of buildRidCache(), since ad-hoc objects have db_name IS NULL
            // and cannot be fetched by db_name filter.
            if (!adHoc.newTableGeoids().isEmpty() || !adHoc.newColumnGeoids().isEmpty()) {
                Map<String, String> adHocRids = adHoc.rids();
                for (String tblGeoid : adHoc.newTableGeoids()) {
                    TableInfo t = str.getTables().get(tblGeoid);
                    if (t == null || t.schemaGeoid() == null) continue;
                    String schRid = adHocRids.get(t.schemaGeoid());
                    String tblRid = adHocRids.get(tblGeoid);
                    if (schRid != null && tblRid != null)
                        edgeByRid("CONTAINS_TABLE", schRid, tblRid, sid);
                }
                for (String colGeoid : adHoc.newColumnGeoids()) {
                    ColumnInfo c = str.getColumns().get(colGeoid);
                    if (c == null) continue;
                    String tblRid = adHocRids.get(c.getTableGeoid());
                    String colRid = adHocRids.get(colGeoid);
                    if (tblRid != null && colRid != null)
                        edgeByRid("HAS_COLUMN", tblRid, colRid, sid);
                }
            }

            builder = JsonlBatchBuilder.buildFromResult(sid, result,
                    adHoc.rids().isEmpty() ? null : adHoc.rids());

            // Register canonical type stats (pre-inserted outside the batch).
            for (String geoid : str.getSchemas().keySet()) {
                if (adHoc.newSchemaGeoids().contains(geoid)) builder.recordInserted("DaliSchema");
                else builder.recordDuplicate("DaliSchema");
            }
            for (String geoid : str.getTables().keySet()) {
                if (adHoc.newTableGeoids().contains(geoid)) builder.recordInserted("DaliTable");
                else builder.recordDuplicate("DaliTable");
            }
            for (String geoid : str.getColumns().keySet()) {
                if (adHoc.newColumnGeoids().contains(geoid)) builder.recordInserted("DaliColumn");
                else builder.recordDuplicate("DaliColumn");
            }
        }

        String payload = builder.build();
        logger.debug("Batch payload: {} vertices, {} edges, {} dropped, {} bytes",
                builder.vertexCount(), builder.edgeCount(), builder.droppedEdgeCount(), payload.length());
        client.send(payload, sid);

        // Post-batch: constraint edges (HAS_PRIMARY_KEY, HAS_FOREIGN_KEY, IS_PK_COLUMN,
        // IS_FK_COLUMN, REFERENCES_TABLE, REFERENCES_COLUMN).
        // Must run AFTER client.send() because DaliPrimaryKey/DaliForeignKey vertices
        // are inserted by the batch and their RIDs are only available afterward.
        if (str.getConstraints() != null && !str.getConstraints().isEmpty()) {
            Map<String, String> constraintRids = buildConstraintRidMap(sid);
            Map<String, String> tblRids  = pool != null
                    ? buildRidMapByField("DaliTable",  "table_geoid",  "db_name", dbName)
                    : buildRidMap("DaliTable",  "table_geoid", sid);
            Map<String, String> colRids  = pool != null
                    ? buildRidMapByField("DaliColumn", "column_geoid", "db_name", dbName)
                    : buildRidMap("DaliColumn", "column_geoid", sid);
            for (var e : str.getConstraints().entrySet()) {
                ConstraintInfo c = e.getValue();
                String constraintRid = constraintRids.get(e.getKey());
                if (constraintRid == null) continue;
                // Table → Constraint edge
                String hostRid = tblRids.get(c.getHostTableGeoid());
                if (hostRid != null) {
                    String tableEdge;
                    if      (c.isPrimaryKey())       tableEdge = "HAS_PRIMARY_KEY";
                    else if (c.isForeignKey())        tableEdge = "HAS_FOREIGN_KEY";
                    else if (c.isUniqueConstraint())  tableEdge = "HAS_UNIQUE_KEY";
                    else if (c.isCheckConstraint())   tableEdge = "HAS_CHECK";
                    else tableEdge = null;
                    if (tableEdge != null) edgeByRid(tableEdge, hostRid, constraintRid, sid);
                }
                // Constraint → Column edges (PK, FK, UQ only)
                String colEdge;
                if      (c.isPrimaryKey())      colEdge = "IS_PK_COLUMN";
                else if (c.isForeignKey())       colEdge = "IS_FK_COLUMN";
                else if (c.isUniqueConstraint()) colEdge = "IS_UNIQUE_COLUMN";
                else colEdge = null;
                if (colEdge != null) {
                    for (int i = 0; i < c.getColumnNames().size(); i++) {
                        String colRid = colRids.get(c.getHostTableGeoid() + "." + c.getColumnNames().get(i));
                        if (colRid != null) edgeByRid(colEdge, constraintRid, colRid, sid, "order_id", i + 1);
                    }
                }
                // REFERENCES_TABLE + REFERENCES_COLUMN (FK only)
                if (c.isForeignKey() && c.getRefTableGeoid() != null) {
                    String refTblRid = tblRids.get(c.getRefTableGeoid());
                    if (refTblRid != null) edgeByRid("REFERENCES_TABLE", constraintRid, refTblRid, sid);
                    for (int i = 0; i < c.getRefColumnNames().size(); i++) {
                        String refColRid = colRids.get(c.getRefTableGeoid() + "." + c.getRefColumnNames().get(i));
                        if (refColRid != null) edgeByRid("REFERENCES_COLUMN", constraintRid, refColRid, sid, "order_id", i + 1);
                    }
                }
            }
            logger.debug("Constraint edges: {} constraints processed (PK/FK/UQ/CH)",
                    constraintRids.size());
        }

        // Post-batch: DaliSnippet (document type — batch endpoint rejects @type "document")
        for (var e : str.getStatements().entrySet()) {
            String raw = truncate(e.getValue().getSnippet(), SNIPPET_MAX);
            if (raw == null) continue;
            rcmd("INSERT INTO DaliSnippet SET session_id=?, stmt_geoid=?, snippet=?, snippet_hash=?, line_start=?, line_end=?",
                    sid, e.getKey(), raw, md5(raw),
                    e.getValue().getLineStart(), e.getValue().getLineEnd());
        }

        WriteStats ws = builder.writeStats();
        timer.stop("write.batch");
        logger.info("ArcadeDB REMOTE_BATCH: sid={} db={} V:{} (dup:{}) E:{} dropped:{} atoms:{} raw:{}b [{}]",
                sid, dbName != null ? dbName : "ad-hoc",
                ws.totalInserted(), ws.totalDuplicate(),
                ws.edgeCount(), ws.droppedEdgeCount(),
                ws.atomsInserted(),
                payload.length(), formatTime(timer.ms("write.batch")));
        return ws;
    }

    // ═══════════════════════════════════════════════════════════════
    // writePerfStats — remote half
    // ═══════════════════════════════════════════════════════════════

    void writePerfStats(String sid, SemanticResult result, PipelineTimer timer,
                        String dbName,
                        int cntTables, int cntColumns, int cntStatements, int cntRoutines,
                        int cntAtoms, int cntJoins, int cntOutputCols, int cntLineage,
                        int atomResolved, int atomConst, int atomFunc, int atomFailed) {
        // DaliPerfStats removed from schema — no-op
    }

    // ═══════════════════════════════════════════════════════════════
    // cleanAll — remote half
    // ═══════════════════════════════════════════════════════════════

    void cleanAll(String[] edgeTypes, String[] vtxTypes, String[] docTypes) {
        for (String t : edgeTypes) deleteTypeRemote(t);
        for (String t : vtxTypes)  deleteTypeRemote(t);
        for (String t : docTypes)  deleteTypeRemote(t);
    }

    // ═══════════════════════════════════════════════════════════════
    // repairHierarchyEdges — одноразовая починка отсутствующих рёбер
    // ═══════════════════════════════════════════════════════════════

    /**
     * Создаёт недостающие рёбра CONTAINS_TABLE и HAS_COLUMN для вершин,
     * у которых они отсутствуют (исторический баг ad-hoc режима).
     *
     * <p>Безопасно вызывать повторно — рёбра создаются только когда их нет.
     * Возвращает количество созданных рёбер.
     *
     * @param dryRun true — только считает, false — создаёт рёбра
     */
    public int repairHierarchyEdges(boolean dryRun) {
        int created = 0;

        // ── 1. CONTAINS_TABLE: DaliSchema → DaliTable ────────────────────────
        // Находим все DaliTable у которых нет входящего ребра CONTAINS_TABLE
        try {
            var tablesRs = db.query("sql",
                "SELECT @rid AS trid, schema_geoid, table_geoid " +
                "FROM DaliTable WHERE IN('CONTAINS_TABLE').size() = 0",
                Map.of());
            while (tablesRs.hasNext()) {
                var row = tablesRs.next().toMap();
                String tRid       = (String) row.get("trid");
                String schGeoid   = (String) row.get("schema_geoid");
                if (tRid == null || schGeoid == null) continue;

                // Найти DaliSchema с matching schema_geoid
                try {
                    var schRs = db.query("sql",
                        "SELECT @rid AS srid FROM DaliSchema WHERE schema_geoid = :sg",
                        Map.of("sg", schGeoid));
                    if (schRs.hasNext()) {
                        String sRid = (String) schRs.next().toMap().get("srid");
                        if (sRid != null) {
                            if (!dryRun) {
                                try {
                                    db.command("sql",
                                        "CREATE EDGE CONTAINS_TABLE FROM " + sRid + " TO " + tRid +
                                        " SET session_id = 'repair'",
                                        Map.of());
                                } catch (Exception ex) {
                                    logger.debug("repair CONTAINS_TABLE {} → {}: {}", sRid, tRid, ex.getMessage());
                                }
                            }
                            created++;
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("repair: schema lookup failed for {}: {}", schGeoid, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            logger.warn("repair: DaliTable scan failed: {}", ex.getMessage());
        }

        // ── 2. HAS_COLUMN: DaliTable → DaliColumn ───────────────────────────
        // Находим все DaliColumn у которых нет входящего ребра HAS_COLUMN
        try {
            var colsRs = db.query("sql",
                "SELECT @rid AS crid, table_geoid " +
                "FROM DaliColumn WHERE IN('HAS_COLUMN').size() = 0",
                Map.of());
            while (colsRs.hasNext()) {
                var row = colsRs.next().toMap();
                String cRid     = (String) row.get("crid");
                String tblGeoid = (String) row.get("table_geoid");
                if (cRid == null || tblGeoid == null) continue;

                try {
                    var tblRs = db.query("sql",
                        "SELECT @rid AS trid FROM DaliTable WHERE table_geoid = :tg",
                        Map.of("tg", tblGeoid));
                    if (tblRs.hasNext()) {
                        String tRid = (String) tblRs.next().toMap().get("trid");
                        if (tRid != null) {
                            if (!dryRun) {
                                try {
                                    db.command("sql",
                                        "CREATE EDGE HAS_COLUMN FROM " + tRid + " TO " + cRid +
                                        " SET session_id = 'repair'",
                                        Map.of());
                                } catch (Exception ex) {
                                    logger.debug("repair HAS_COLUMN {} → {}: {}", tRid, cRid, ex.getMessage());
                                }
                            }
                            created++;
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("repair: table lookup failed for {}: {}", tblGeoid, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            logger.warn("repair: DaliColumn scan failed: {}", ex.getMessage());
        }

        logger.info("repairHierarchyEdges(dryRun={}): {} edges {}", dryRun, created,
                    dryRun ? "would be created" : "created");
        return created;
    }

    private void deleteTypeRemote(String typeName) {
        try {
            db.command("sql", "TRUNCATE TYPE `" + typeName + "` UNSAFE");
        } catch (Exception e) {
            logger.warn("TRUNCATE TYPE {} failed: {}", typeName, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SQL helpers
    // ═══════════════════════════════════════════════════════════════

    void rcmd(String sqlTemplate, Object... params) {
        String sqlFinal;
        Map<String, Object> paramMap;
        if (params.length == 0) {
            sqlFinal = sqlTemplate;
            paramMap = Map.of();
        } else {
            StringBuilder sql = new StringBuilder();
            paramMap = new LinkedHashMap<>();
            int paramIdx = 0;
            int lastPos  = 0;
            for (int i = 0; i < sqlTemplate.length(); i++) {
                if (sqlTemplate.charAt(i) == '?') {
                    sql.append(sqlTemplate, lastPos, i);
                    String paramName = "p" + paramIdx;
                    sql.append(':').append(paramName);
                    paramMap.put(paramName, paramIdx < params.length ? params[paramIdx] : null);
                    paramIdx++;
                    lastPos = i + 1;
                }
            }
            sql.append(sqlTemplate, lastPos, sqlTemplate.length());
            sqlFinal = sql.toString();
        }
        Exception lastEx = null;
        for (int attempt = 0; attempt <= RCMD_MAX_RETRIES; attempt++) {
            try {
                if (paramMap.isEmpty()) db.command("sql", sqlFinal);
                else                    db.command("sql", sqlFinal, paramMap);
                return;
            } catch (Exception e) {
                lastEx = e;
                String msg = e.getMessage();
                boolean isTimeout = msg != null && (msg.contains("Timeout") || msg.contains("locking"));
                if (!isTimeout || attempt == RCMD_MAX_RETRIES) break;
                long delay = RCMD_RETRY_BASE_MS * (1L << attempt);
                logger.debug("Remote cmd timeout (attempt {}/{}), retrying in {}ms: {}",
                        attempt + 1, RCMD_MAX_RETRIES, delay,
                        sqlTemplate.substring(0, Math.min(sqlTemplate.length(), 80)));
                try { Thread.sleep(delay); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); break;
                }
            }
        }
        String errMsg = lastEx != null ? lastEx.getMessage() : "unknown";
        boolean isDuplicateKey = errMsg.contains("Duplicated key") || errMsg.contains("DuplicatedKeyException") || errMsg.contains("Found duplicate key");
        if (isDuplicateKey) {
            logger.debug("Remote cmd dup-key: {} — {}",
                    sqlTemplate.substring(0, Math.min(sqlTemplate.length(), 100)), errMsg);
        } else {
            logger.warn("Remote cmd FAILED: {} — {}",
                    sqlTemplate.substring(0, Math.min(sqlTemplate.length(), 100)), errMsg);
        }
        throw new RuntimeException("Remote cmd FAILED: " + errMsg, lastEx);
    }

    private void edgeByRid(String edgeType, String fromRid, String toRid, String sid) {
        try {
            db.command("sql",
                    "CREATE EDGE " + edgeType + " FROM " + fromRid + " TO " + toRid + " SET session_id = :sid",
                    Map.of("sid", sid));
        } catch (Exception e) {
            logger.debug("edgeByRid {} failed {} → {}: {}", edgeType, fromRid, toRid, e.getMessage());
        }
    }

    private void edgeByRid(String edgeType, String fromRid, String toRid, String sid,
                            String extraField, Object extraVal) {
        try {
            db.command("sql",
                    "CREATE EDGE " + edgeType + " FROM " + fromRid + " TO " + toRid
                    + " SET session_id = :sid, " + extraField + " = :ev",
                    Map.of("sid", sid, "ev", extraVal));
        } catch (Exception e) {
            logger.debug("edgeByRid {} failed: {}", edgeType, e.getMessage());
        }
    }

    private void edgeByRid(String edgeType, String fromRid, String toRid, String sid,
                            String f1, Object v1, String f2, Object v2) {
        try {
            db.command("sql",
                    "CREATE EDGE " + edgeType + " FROM " + fromRid + " TO " + toRid
                    + " SET session_id = :sid, " + f1 + " = :v1, " + f2 + " = :v2",
                    Map.of("sid", sid, "v1", v1, "v2", v2));
        } catch (Exception e) {
            logger.debug("edgeByRid {} failed: {}", edgeType, e.getMessage());
        }
    }

    private void edgeByRid(String edgeType, String fromRid, String toRid, String sid,
                            String f1, Object v1, String f2, Object v2, String f3, Object v3) {
        try {
            db.command("sql",
                    "CREATE EDGE " + edgeType + " FROM " + fromRid + " TO " + toRid
                    + " SET session_id = :sid, " + f1 + " = :v1, " + f2 + " = :v2, " + f3 + " = :v3",
                    Map.of("sid", sid, "v1", v1, "v2", v2, "v3", v3));
        } catch (Exception e) {
            logger.debug("edgeByRid {} failed: {}", edgeType, e.getMessage());
        }
    }

    private void edgeRemote(String edgeType, String fromType, String fromField, String fromVal,
                            String toType, String toField, String toVal, String sid) {
        edgeRemote(edgeType, fromType, fromField, fromVal, toType, toField, toVal, sid, null, null);
    }

    private void edgeRemote(String edgeType, String fromType, String fromField, String fromVal,
                            String toType, String toField, String toVal, String sid,
                            String extraField, String extraVal) {
        try {
            StringBuilder sql = new StringBuilder();
            Map<String, Object> params = new LinkedHashMap<>();
            sql.append("CREATE EDGE ").append(edgeType)
                    .append(" FROM (SELECT FROM ").append(fromType)
                    .append(" WHERE ").append(fromField).append(" = :fromVal AND session_id = :sid)")
                    .append(" TO (SELECT FROM ").append(toType)
                    .append(" WHERE ").append(toField).append(" = :toVal AND session_id = :sid");
            params.put("fromVal", fromVal);
            params.put("toVal", toVal);
            params.put("sid", sid);
            if (extraField != null && extraVal != null) {
                sql.append(" AND ").append(extraField).append(" = :extraVal");
                params.put("extraVal", extraVal);
            }
            sql.append(") SET session_id = :sid");
            db.command("sql", sql.toString(), params);
        } catch (Exception e) {
            logger.debug("Edge {} failed: FROM {}[{}={}] TO {}[{}={}] — {}",
                    edgeType, fromType, fromField, fromVal, toType, toField, toVal, e.getMessage());
        }
    }

    private void edgeFromRidToQuery(String edgeType, String fromRid,
                                    String toType, String filterField, String filterVal, String sid) {
        try {
            db.command("sql",
                    "CREATE EDGE " + edgeType + " FROM " + fromRid +
                    " TO (SELECT FROM " + toType + " WHERE session_id = :sid AND " + filterField + " = :fv)" +
                    " SET session_id = :sid",
                    Map.of("sid", sid, "fv", filterVal));
        } catch (Exception e) {
            logger.debug("edgeFromRidToQuery {} failed: {}", edgeType, e.getMessage());
        }
    }

    private void edgeFromQueryToRid(String edgeType,
                                    String fromType, String filterField, String filterVal,
                                    String sid, String toRid) {
        try {
            db.command("sql",
                    "CREATE EDGE " + edgeType +
                    " FROM (SELECT FROM " + fromType + " WHERE session_id = :sid AND " + filterField + " = :fv)" +
                    " TO " + toRid + " SET session_id = :sid",
                    Map.of("sid", sid, "fv", filterVal));
        } catch (Exception e) {
            logger.debug("edgeFromQueryToRid {} failed: {}", edgeType, e.getMessage());
        }
    }
}
