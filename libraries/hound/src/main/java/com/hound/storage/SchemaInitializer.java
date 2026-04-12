// File: src/main/java/com/hound/storage/SchemaInitializer.java
package com.hound.storage;

import com.arcadedb.database.Database;
import com.arcadedb.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the ArcadeDB schema for all Dali vertex/edge/document types.
 * Non-backwards-compatible: always creates the current schema from scratch.
 * No migration logic — drop the database and re-run if upgrading.
 *
 * ── Vertex types (15) ──
 *   DaliApplication, DaliDatabase, DaliSchema, DaliTable, DaliColumn,
 *   DaliRoutine, DaliPackage (EXTENDS DaliRoutine),
 *   DaliSession, DaliStatement, DaliAtom, DaliOutputColumn,
 *   DaliJoin, DaliParameter, DaliVariable, DaliAffectedColumn
 *
 * ── Document types (4) ──
 *   DaliSnippet      — full SQL text for a statement (stmt_geoid + snippet)
 *   DaliPerfStats    — per-session pipeline timing & counts (never cleared by cleanAll)
 *   DaliResolutionLog — atom resolution diagnostics (file_path, table_name, column_name, position)
 *   DaliSchemaLog    — suspicious schema name registrations with backtrace
 *
 * ── Edge types (32) ──
 *   Structural:   BELONGS_TO_APP, CONTAINS_SCHEMA, CONTAINS_TABLE, HAS_COLUMN,
 *                 CONTAINS_ROUTINE, BELONGS_TO_SESSION, CONTAINS_STMT,
 *                 HAS_PARAMETER, HAS_VARIABLE, CHILD_OF
 *   Statement:    HAS_OUTPUT_COL, HAS_ATOM, HAS_JOIN, READS_FROM, WRITES_TO,
 *                 USES_SUBQUERY, NESTED_IN, ROUTINE_USES_TABLE, CALLS
 *   Atom:         ATOM_REF_TABLE, ATOM_REF_COLUMN, ATOM_REF_STMT, ATOM_REF_OUTPUT_COL,
 *                 ATOM_PRODUCES
 *   Flow:         DATA_FLOW (flow_type: DIRECT|AGGREGATE|INSERT|UPDATE|MERGE),
 *                FILTER_FLOW, JOIN_FLOW, UNION_FLOW
 *   AffectedCol:  HAS_AFFECTED_COL, AFFECTED_COL_REF_TABLE
 *   Join:         JOIN_SOURCE_TABLE, JOIN_TARGET_TABLE
 *
 * ── Indexes ──
 *   UNIQUE_HASH:   DaliApplication(app_geoid), DaliDatabase(db_geoid),
 *                  DaliSchema(db_name, schema_geoid), DaliTable(db_name, table_geoid),
 *                  DaliColumn(db_name, column_geoid)
 *   NOTUNIQUE LSM: session_id on all vertex types that carry it; DaliStatement(short_name);
 *                  app_name, db_name, schema_name, table_name, column_name, routine_name,
 *                  package_name, file_path, stmt_geoid, atom_text, param_name, var_name,
 *                  output col name (v26: converted from FULLTEXT — equality lookup support)
 *   FULLTEXT:      DaliSnippet(snippet) only — per-statement SQL text search
 */
public final class SchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SchemaInitializer.class);
    static final int SCHEMA_VERSION = 26;

    // FT_METADATA lives in RemoteSchemaCommands — reuse from there.
    private static final String FT_METADATA = RemoteSchemaCommands.FT_METADATA;

    private SchemaInitializer() {}

    public static void ensureSchema(Database db) {
        Schema schema = db.getSchema();


        int c = 0;

        // ── Vertex types ──
        c += vtx(schema, "DaliApplication");
        c += vtx(schema, "DaliDatabase");
        c += vtx(schema, "DaliSchema");
        c += vtx(schema, "DaliTable");
        c += vtx(schema, "DaliColumn");
        c += vtx(schema, "DaliRoutine");
        c += vtxExtends(schema, "DaliPackage", "DaliRoutine");
        c += vtx(schema, "DaliSession");
        c += vtx(schema, "DaliStatement");
        c += vtx(schema, "DaliAtom");
        c += vtx(schema, "DaliOutputColumn");
        c += vtx(schema, "DaliJoin");
        c += vtx(schema, "DaliParameter");
        c += vtx(schema, "DaliVariable");
        c += vtx(schema, "DaliAffectedColumn");

        // ── Edge types ──
        for (String e : new String[]{
                // namespace hierarchy
                "BELONGS_TO_APP", "CONTAINS_SCHEMA", "CONTAINS_TABLE", "HAS_COLUMN",
                // routine hierarchy
                "CONTAINS_ROUTINE", "BELONGS_TO_SESSION", "CONTAINS_STMT",
                "HAS_PARAMETER", "HAS_VARIABLE", "CHILD_OF",
                // statement structure
                "HAS_OUTPUT_COL", "HAS_ATOM", "HAS_JOIN",
                "READS_FROM", "WRITES_TO", "USES_SUBQUERY", "NESTED_IN",
                "ROUTINE_USES_TABLE", "CALLS",
                // atom resolution
                "ATOM_REF_TABLE", "ATOM_REF_COLUMN", "ATOM_REF_STMT", "ATOM_REF_OUTPUT_COL",
                "ATOM_PRODUCES",
                // data flow / lineage
                "DATA_FLOW", "FILTER_FLOW", "JOIN_FLOW", "UNION_FLOW",
                // affected columns
                "HAS_AFFECTED_COL", "AFFECTED_COL_REF_TABLE",
                // join sources
                "JOIN_SOURCE_TABLE", "JOIN_TARGET_TABLE"
        }) { c += edg(schema, e); }

        // ── Document types ──
        if (!schema.existsType("DaliSnippet"))        schema.createDocumentType("DaliSnippet");
        // v22: DaliSnippetScript — full raw SQL text of the parsed file, one record per session.
        if (!schema.existsType("DaliSnippetScript"))  schema.createDocumentType("DaliSnippetScript");

        // ── String properties (required for FULLTEXT indexes) ──
        // Namespace / canonical
        declareStr(schema, "DaliApplication", "app_geoid");
        declareStr(schema, "DaliApplication", "app_name");
        declareStr(schema, "DaliDatabase",    "db_geoid");
        declareStr(schema, "DaliDatabase",    "db_name");
        declareStr(schema, "DaliSchema",      "db_name");
        declareStr(schema, "DaliSchema",      "db_geoid");
        declareStr(schema, "DaliSchema",      "schema_geoid");
        declareStr(schema, "DaliSchema",      "schema_name");
        declareStr(schema, "DaliTable",       "db_name");
        declareStr(schema, "DaliTable",       "table_geoid");
        declareStr(schema, "DaliTable",       "table_name");
        declareStr(schema, "DaliTable",       "table_type");
        declareStr(schema, "DaliTable",       "session_id");
        declareStr(schema, "DaliTable",       "data_source");   // v24: "master"|"reconstructed"
        declareStr(schema, "DaliColumn",      "db_name");
        declareStr(schema, "DaliColumn",      "column_geoid");
        declareStr(schema, "DaliColumn",      "column_name");
        declareStr(schema, "DaliColumn",      "expression");
        declareStr(schema, "DaliColumn",      "alias");
        declareStr(schema, "DaliColumn",      "session_id");
        declareStr(schema, "DaliColumn",      "data_source");   // v24: "master"|"reconstructed"
        declareProp(schema, "DaliColumn",     "ordinal_position", com.arcadedb.schema.Type.INTEGER); // T13
        // Routine
        declareStr(schema, "DaliRoutine",     "routine_geoid");
        declareStr(schema, "DaliRoutine",     "routine_name");
        declareStr(schema, "DaliRoutine",     "routine_type");
        declareStr(schema, "DaliRoutine",     "return_type");   // v23: FUNCTION return type
        declareStr(schema, "DaliRoutine",     "data_source");   // v24: "master"|"reconstructed"
        declareProp(schema, "DaliRoutine",    "line_start", com.arcadedb.schema.Type.INTEGER); // v23
        declareStr(schema, "DaliRoutine",     "session_id");
        declareStr(schema, "DaliPackage",     "package_name");
        // Session
        declareStr(schema, "DaliSession",     "session_id");
        declareStr(schema, "DaliSession",     "db_name");
        declareStr(schema, "DaliSession",     "file_path");
        declareStr(schema, "DaliSession",     "dialect");
        // Statement — snippet intentionally absent (lives in DaliSnippet document)
        declareStr(schema, "DaliStatement",   "stmt_geoid");
        declareStr(schema, "DaliStatement",   "type");
        declareStr(schema, "DaliStatement",   "short_name");
        declareStr(schema, "DaliStatement",   "session_id");
        // Atom
        declareStr(schema, "DaliAtom",        "atom_id");
        declareStr(schema, "DaliAtom",        "atom_text");
        declareStr(schema, "DaliAtom",        "atom_context");
        declareStr(schema, "DaliAtom",        "parent_context");
        declareStr(schema, "DaliAtom",        "status");
        declareStr(schema, "DaliAtom",        "warning");
        declareStr(schema, "DaliAtom",        "merge_clause"); // "UPDATE" | "INSERT" for MERGE target atoms
        declareStr(schema, "DaliAtom",        "session_id");
        // Output column
        declareStr(schema, "DaliOutputColumn", "name");
        declareStr(schema, "DaliOutputColumn", "expression");
        declareStr(schema, "DaliOutputColumn", "alias");
        declareStr(schema, "DaliOutputColumn", "session_id");
        // Join
        declareStr(schema, "DaliJoin",        "join_type");
        declareStr(schema, "DaliJoin",        "session_id");
        // Parameter / Variable
        declareStr(schema, "DaliParameter",   "param_name");
        declareStr(schema, "DaliParameter",   "param_type");
        declareStr(schema, "DaliParameter",   "param_mode");
        declareStr(schema, "DaliParameter",   "session_id");
        declareStr(schema, "DaliVariable",    "var_name");
        declareStr(schema, "DaliVariable",    "var_type");
        declareStr(schema, "DaliVariable",    "session_id");
        // AffectedColumn
        declareStr(schema, "DaliAffectedColumn", "session_id");
        declareStr(schema, "DaliAffectedColumn", "statement_geoid");
        declareStr(schema, "DaliAffectedColumn", "column_ref");
        declareStr(schema, "DaliAffectedColumn", "column_name");
        declareStr(schema, "DaliAffectedColumn", "table_geoid");
        declareStr(schema, "DaliAffectedColumn", "dataset_alias");
        declareStr(schema, "DaliAffectedColumn", "source_type");
        declareStr(schema, "DaliAffectedColumn", "resolution_status");
        declareStr(schema, "DaliAffectedColumn", "type_affect");
        // DaliSnippet — per-statement SQL text with line range (v22: +line_start, +line_end)
        declareStr(schema, "DaliSnippet",          "stmt_geoid");
        declareStr(schema, "DaliSnippet",          "session_id");
        declareStr(schema, "DaliSnippet",          "snippet");
        declareProp(schema, "DaliSnippet",         "line_start", com.arcadedb.schema.Type.INTEGER);
        declareProp(schema, "DaliSnippet",         "line_end",   com.arcadedb.schema.Type.INTEGER);
        // DaliSnippetScript — full raw file text, one record per session (v22)
        declareStr(schema, "DaliSnippetScript",    "session_id");
        declareStr(schema, "DaliSnippetScript",    "file_path");
        declareStr(schema, "DaliSnippetScript",    "script");
        declareProp(schema, "DaliSnippetScript",   "script_hash",  com.arcadedb.schema.Type.STRING);
        declareProp(schema, "DaliSnippetScript",   "line_count",   com.arcadedb.schema.Type.INTEGER);
        declareProp(schema, "DaliSnippetScript",   "char_count",   com.arcadedb.schema.Type.INTEGER);

        // ── UNIQUE_HASH indexes (canonical deduplication) ──
        idx(db, "CREATE INDEX IF NOT EXISTS ON DaliApplication (app_geoid) UNIQUE_HASH NULL_STRATEGY SKIP");
        idx(db, "CREATE INDEX IF NOT EXISTS ON DaliDatabase (db_geoid) UNIQUE_HASH NULL_STRATEGY SKIP");
        idx(db, "CREATE INDEX IF NOT EXISTS ON DaliSchema (db_name, schema_geoid) UNIQUE_HASH NULL_STRATEGY SKIP");
        idx(db, "CREATE INDEX IF NOT EXISTS ON DaliTable (db_name, table_geoid) UNIQUE_HASH NULL_STRATEGY SKIP");
        idx(db, "CREATE INDEX IF NOT EXISTS ON DaliColumn (db_name, column_geoid) UNIQUE_HASH NULL_STRATEGY SKIP");

        // ── NOTUNIQUE LSM_TREE indexes (session_id — high cardinality per key) ──
        for (String t : new String[]{
                "DaliStatement", "DaliRoutine", "DaliAtom", "DaliJoin",
                "DaliTable", "DaliColumn", "DaliParameter", "DaliVariable",
                "DaliOutputColumn", "DaliAffectedColumn"
        }) {
            idx(db, "CREATE INDEX IF NOT EXISTS ON " + t + " (session_id) NOTUNIQUE NULL_STRATEGY SKIP");
        }
        // DaliStatement(short_name) — LSM_TREE to avoid HASH bucket corruption on timeout
        idx(db, "CREATE INDEX IF NOT EXISTS ON DaliStatement (short_name) NOTUNIQUE NULL_STRATEGY SKIP");

        // ── NOTUNIQUE LSM_TREE indexes on name fields (v26: converted from FULL_TEXT) ──
        // FULL_TEXT indexes don't support equality lookup ({prop: $val}) — only CONTAINSTEXT().
        for (String[] p : new String[][]{
                {"DaliApplication",  "app_name"},
                {"DaliDatabase",     "db_name"},
                {"DaliSchema",       "schema_name"},
                {"DaliTable",        "table_name"},
                {"DaliColumn",       "column_name"},
                {"DaliRoutine",      "routine_name"},
                {"DaliPackage",      "package_name"},
                {"DaliSession",      "file_path"},
                {"DaliStatement",    "stmt_geoid"},
                {"DaliAtom",         "atom_text"},
                {"DaliParameter",    "param_name"},
                {"DaliVariable",     "var_name"},
                {"DaliOutputColumn", "name"},
        }) {
            idx(db, "CREATE INDEX IF NOT EXISTS ON " + p[0] + " (" + p[1] + ") NOTUNIQUE NULL_STRATEGY SKIP");
        }
        // ── FULLTEXT index — per-statement SQL text search (kept intentionally) ──
        ft(db, schema, "DaliSnippet", "snippet");
        // NOTE: DaliSnippetScript.script is intentionally NOT indexed — it stores the full raw
        // file text (up to hundreds of KB) which exceeds ArcadeDB's 255 KB page limit for
        // FULL_TEXT indexes, causing index corruption and multi-GB on-disk bloat (v25 fix).


        logger.info("ArcadeDB schema v{}: {} types created", SCHEMA_VERSION, c);
    }

    // ── Remote schema commands (called once per remote DB connection) ──

    /** Delegates to {@link RemoteSchemaCommands#all()} — types → properties → indexes. */
    public static String[] remoteSchemaCommands() {
        return RemoteSchemaCommands.all();
    }

    // ── Helpers ──

    private static int vtx(Schema schema, String name) {
        if (!schema.existsType(name)) { schema.createVertexType(name); return 1; }
        return 0;
    }

    private static int vtxExtends(Schema schema, String name, String parent) {
        if (!schema.existsType(name)) {
            schema.createVertexType(name).addSuperType(parent); return 1;
        }
        return 0;
    }

    private static int edg(Schema schema, String name) {
        if (!schema.existsType(name)) { schema.createEdgeType(name); return 1; }
        return 0;
    }

    private static void declareStr(Schema schema, String typeName, String propName) {
        declareProp(schema, typeName, propName, com.arcadedb.schema.Type.STRING);
    }

    private static void declareProp(Schema schema, String typeName, String propName,
                                    com.arcadedb.schema.Type type) {
        if (!schema.existsType(typeName)) return;
        var t = schema.getType(typeName);
        if (!t.existsProperty(propName))
            t.createProperty(propName, type);
    }

    private static void idx(Database db, String sql) {
        try { db.command("sql", sql); }
        catch (Exception e) { logger.debug("Index skipped: {}", e.getMessage()); }
    }

    private static void ft(Database db, Schema schema, String typeName, String propName) {
        if (!schema.existsType(typeName)) return;
        idx(db, "CREATE INDEX IF NOT EXISTS ON " + typeName + " (" + propName + ") FULL_TEXT" + FT_METADATA);
    }

    private static void tryDropIndex(Database db, String indexName) {
        try { db.command("sql", "DROP INDEX IF EXISTS `" + indexName + "`"); }
        catch (Exception e) { logger.debug("Index drop skipped for {}: {}", indexName, e.getMessage()); }
    }
}
