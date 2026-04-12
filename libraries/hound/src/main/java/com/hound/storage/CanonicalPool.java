// src/main/java/com/hound/storage/CanonicalPool.java
package com.hound.storage;

import com.arcadedb.graph.MutableVertex;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe canonical vertex pool for DaliSchema, DaliTable and DaliColumn vertices
 * within a single database namespace (one pool per DaliDatabase per run).
 *
 * <p>Solves per-session duplication: instead of creating a new schema/table/column vertex
 * for every file that references the same object, the pool ensures each unique object
 * has exactly one canonical vertex per database.
 *
 * <p>Canonical GEOID formula: {@code DB_NAME + "::" + SESSION_GEOID}
 * <ul>
 *   <li>Schema:  {@code DWH::BUDM_RMS}
 *   <li>Table:   {@code DWH::BUDM_RMS.ACCOUNTS}
 *   <li>Column:  {@code DWH::BUDM_RMS.ACCOUNTS.ID}
 *   <li>Ad-hoc (null db): session geoid used as-is (old behavior)
 * </ul>
 *
 * <p>Graph hierarchy maintained:
 * <pre>
 *   DaliApplication ←── BELONGS_TO_APP ──── DaliDatabase
 *   DaliDatabase    ←── CONTAINS_SCHEMA ─── DaliSchema
 *   DaliSchema      ←── CONTAINS_TABLE ──── DaliTable
 *   DaliTable       ←── HAS_COLUMN ──────── DaliColumn
 * </pre>
 *
 * <p>Usage lifecycle:
 * <ol>
 *   <li>Created once per database batch ({@code new CanonicalPool("DWH")})
 *   <li>Passed to {@code ArcadeDBSemanticWriter.saveResult()} for each file in the batch
 *   <li>Discarded after batch completes (data already persisted in ArcadeDB)
 * </ol>
 *
 * <p>Thread safety: all maps are ConcurrentHashMap. Phase 2 (write) is single-threaded
 * by design, so concurrent access is not expected — ConcurrentHashMap is used defensively.
 *
 * @see com.hound.storage.ArcadeDBSemanticWriter
 * @see com.hound.HoundApplication.DatabaseBatch
 */
public class CanonicalPool {

    private final String dbName;

    // ── Canonical DaliDatabase vertex (set by ArcadeDBSemanticWriter.ensureCanonicalPool) ──
    // Used to create CONTAINS_SCHEMA edges from DaliDatabase → DaliSchema.
    private MutableVertex databaseVtx; // embedded mode
    private String        databaseRid; // remote mode ("#cluster:pos")

    // ── Canonical DaliApplication vertex (set once if application context is known) ──
    private MutableVertex applicationVtx; // embedded mode
    private String        applicationRid; // remote mode

    // ── Embedded mode ── MutableVertex kept after .save()
    private final ConcurrentHashMap<String, MutableVertex> schemaVtx = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MutableVertex> tableVtx  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MutableVertex> columnVtx = new ConcurrentHashMap<>();

    // ── Remote mode ── RID string "#cluster:position"
    private final ConcurrentHashMap<String, String> schemaRids = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> tableRids  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> columnRids = new ConcurrentHashMap<>();

    /**
     * @param dbName  Database name (folder name). Null = ad-hoc mode (pool is inactive).
     */
    public CanonicalPool(String dbName) {
        this.dbName = dbName;
    }

    // ═══════════════════════════════════════════════════════════════
    // Canonical DaliDatabase vertex reference
    // ═══════════════════════════════════════════════════════════════

    /**
     * Stores the canonical DaliDatabase vertex (embedded mode).
     * Called once by {@code ArcadeDBSemanticWriter.ensureCanonicalPool()} after
     * the DaliDatabase vertex is created. Used to attach CONTAINS_SCHEMA edges.
     */
    public void setDatabaseVtx(MutableVertex v) { this.databaseVtx = v; }

    /** Returns the canonical DaliDatabase vertex, or null if not yet set (ad-hoc mode). */
    public MutableVertex getDatabaseVtx() { return databaseVtx; }

    /**
     * Stores the canonical DaliDatabase RID (remote mode).
     * Called once by {@code ArcadeDBSemanticWriter.ensureCanonicalPool()}.
     */
    public void setDatabaseRid(String rid) { this.databaseRid = rid; }

    /** Returns the canonical DaliDatabase RID, or null if not yet set. */
    public String getDatabaseRid() { return databaseRid; }

    // ═══════════════════════════════════════════════════════════════
    // Canonical DaliApplication vertex reference
    // ═══════════════════════════════════════════════════════════════

    /** Stores the canonical DaliApplication vertex (embedded mode). */
    public void setApplicationVtx(MutableVertex v) { this.applicationVtx = v; }

    /** Returns the canonical DaliApplication vertex, or null if not set. */
    public MutableVertex getApplicationVtx() { return applicationVtx; }

    /** Stores the canonical DaliApplication RID (remote mode). */
    public void setApplicationRid(String rid) { this.applicationRid = rid; }

    /** Returns the canonical DaliApplication RID, or null if not set. */
    public String getApplicationRid() { return applicationRid; }

    // ═══════════════════════════════════════════════════════════════
    // Canonical geoid
    // ═══════════════════════════════════════════════════════════════

    /**
     * Pool key for a schema geoid.
     * The pool is already DB-scoped (one pool per DaliDatabase), so the key is the
     * bare {@code schema_geoid} — no {@code DB_NAME::} prefix needed.
     * <p>Example: {@code "BUDM_RMS"}
     */
    public String canonicalSchema(String sessionSchemaGeoid) {
        return sessionSchemaGeoid;
    }

    /**
     * Pool key for a table geoid.
     * <p>Example: {@code "BUDM_RMS.ACCOUNTS"}
     */
    public String canonical(String sessionGeoid) {
        return sessionGeoid;
    }

    /**
     * Pool key for a column, derived from its table geoid and column name.
     * <p>Example: {@code "BUDM_RMS.ACCOUNTS.ID"}
     */
    public String canonicalCol(String sessionTableGeoid, String columnName) {
        return sessionTableGeoid + "." + columnName.toUpperCase();
    }

    public String getDbName() { return dbName; }

    // ═══════════════════════════════════════════════════════════════
    // Embedded mode — MutableVertex
    // ═══════════════════════════════════════════════════════════════

    /** Returns the canonical DaliSchema vertex, or null if not yet created. */
    public MutableVertex getSchemaVtx(String canonicalGeoid) {
        return schemaVtx.get(canonicalGeoid);
    }

    /** Registers a newly created DaliSchema vertex under its canonical geoid. */
    public void putSchemaVtx(String canonicalGeoid, MutableVertex v) {
        schemaVtx.put(canonicalGeoid, v);
    }

    public boolean hasSchema(String canonicalGeoid) { return schemaVtx.containsKey(canonicalGeoid); }

    /** Returns the canonical DaliTable vertex, or null if not yet created. */
    public MutableVertex getTableVtx(String canonicalGeoid) {
        return tableVtx.get(canonicalGeoid);
    }

    /** Returns the canonical DaliColumn vertex, or null if not yet created. */
    public MutableVertex getColumnVtx(String canonicalGeoid) {
        return columnVtx.get(canonicalGeoid);
    }

    /** Registers a newly created DaliTable vertex under its canonical geoid. */
    public void putTableVtx(String canonicalGeoid, MutableVertex v) {
        tableVtx.put(canonicalGeoid, v);
    }

    /** Registers a newly created DaliColumn vertex under its canonical geoid. */
    public void putColumnVtx(String canonicalGeoid, MutableVertex v) {
        columnVtx.put(canonicalGeoid, v);
    }

    public boolean hasTable(String canonicalGeoid)  { return tableVtx.containsKey(canonicalGeoid); }
    public boolean hasColumn(String canonicalGeoid) { return columnVtx.containsKey(canonicalGeoid); }

    // ═══════════════════════════════════════════════════════════════
    // Remote mode — RID strings
    // ═══════════════════════════════════════════════════════════════

    /** Returns the RID of the canonical DaliSchema, or null if not yet created. */
    public String getSchemaRid(String canonicalGeoid) {
        return schemaRids.get(canonicalGeoid);
    }

    public void putSchemaRid(String canonicalGeoid, String rid) {
        schemaRids.put(canonicalGeoid, rid);
    }

    public boolean hasSchemaRid(String canonicalGeoid) { return schemaRids.containsKey(canonicalGeoid); }

    /** Returns the RID of the canonical DaliTable, or null if not yet created. */
    public String getTableRid(String canonicalGeoid) {
        return tableRids.get(canonicalGeoid);
    }

    /** Returns the RID of the canonical DaliColumn, or null if not yet created. */
    public String getColumnRid(String canonicalGeoid) {
        return columnRids.get(canonicalGeoid);
    }

    public void putTableRid(String canonicalGeoid, String rid) {
        tableRids.put(canonicalGeoid, rid);
    }

    public void putColumnRid(String canonicalGeoid, String rid) {
        columnRids.put(canonicalGeoid, rid);
    }

    public boolean hasTableRid(String canonicalGeoid)  { return tableRids.containsKey(canonicalGeoid); }
    public boolean hasColumnRid(String canonicalGeoid) { return columnRids.containsKey(canonicalGeoid); }

    // ═══════════════════════════════════════════════════════════════
    // Stats
    // ═══════════════════════════════════════════════════════════════

    /** Number of canonical schema vertices registered (embedded or remote). */
    public int schemaCount() {
        return schemaVtx.isEmpty() ? schemaRids.size() : schemaVtx.size();
    }

    /** Number of canonical table vertices registered (embedded or remote). */
    public int tableCount()  {
        return tableVtx.isEmpty() ? tableRids.size()  : tableVtx.size();
    }

    /** Number of canonical column vertices registered (embedded or remote). */
    public int columnCount() {
        return columnVtx.isEmpty() ? columnRids.size() : columnVtx.size();
    }

    @Override
    public String toString() {
        return "CanonicalPool[db=" + dbName +
               ", schemas=" + schemaCount() +
               ", tables=" + tableCount() +
               ", columns=" + columnCount() + "]";
    }
}
