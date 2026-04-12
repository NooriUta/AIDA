// File: src/main/java/com/hound/storage/RemoteSchemaCommands.java
package com.hound.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * DDL commands for remote ArcadeDB schema initialisation.
 *
 * Split into three logical groups so that SchemaInitializer.remoteSchemaCommands()
 * stays small and each group can be evolved independently.
 *
 *   typeCommands()     — CREATE VERTEX / EDGE / DOCUMENT TYPE
 *   propertyCommands() — CREATE PROPERTY
 *   indexCommands()    — CREATE INDEX (UNIQUE_HASH, NOTUNIQUE, FULLTEXT)
 *   all()              — ordered concatenation used by the writer
 */
final class RemoteSchemaCommands {

    static final String FT_ANALYZER =
            "org.apache.lucene.analysis.core.KeywordAnalyzer";
    static final String FT_METADATA =
            " METADATA {\"analyzer\": \"" + FT_ANALYZER + "\"}";

    private RemoteSchemaCommands() {}

    // ── Vertex / Edge / Document types ──────────────────────────────────────

    static String[] typeCommands() {
        return new String[]{
                // Vertex types
                "CREATE VERTEX TYPE DaliApplication IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliDatabase IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliSchema IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliTable IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliColumn IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliRoutine IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliPackage IF NOT EXISTS EXTENDS DaliRoutine",
                "CREATE VERTEX TYPE DaliSession IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliStatement IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliAtom IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliOutputColumn IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliJoin IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliParameter IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliVariable IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliAffectedColumn IF NOT EXISTS",

                // Edge types — namespace hierarchy
                "CREATE EDGE TYPE BELONGS_TO_APP IF NOT EXISTS",
                "CREATE EDGE TYPE CONTAINS_SCHEMA IF NOT EXISTS",
                "CREATE EDGE TYPE CONTAINS_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE HAS_COLUMN IF NOT EXISTS",
                "CREATE EDGE TYPE CONTAINS_ROUTINE IF NOT EXISTS",
                "CREATE EDGE TYPE BELONGS_TO_SESSION IF NOT EXISTS",
                "CREATE EDGE TYPE CONTAINS_STMT IF NOT EXISTS",
                "CREATE EDGE TYPE HAS_PARAMETER IF NOT EXISTS",
                "CREATE EDGE TYPE HAS_VARIABLE IF NOT EXISTS",
                "CREATE EDGE TYPE CHILD_OF IF NOT EXISTS",
                // Edge types — statement structure
                "CREATE EDGE TYPE HAS_OUTPUT_COL IF NOT EXISTS",
                "CREATE EDGE TYPE HAS_ATOM IF NOT EXISTS",
                "CREATE EDGE TYPE HAS_JOIN IF NOT EXISTS",
                "CREATE EDGE TYPE READS_FROM IF NOT EXISTS",
                "CREATE EDGE TYPE WRITES_TO IF NOT EXISTS",
                "CREATE EDGE TYPE USES_SUBQUERY IF NOT EXISTS",
                "CREATE EDGE TYPE NESTED_IN IF NOT EXISTS",
                "CREATE EDGE TYPE ROUTINE_USES_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE CALLS IF NOT EXISTS",
                // Edge types — atom resolution
                "CREATE EDGE TYPE ATOM_REF_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_COLUMN IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_STMT IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_OUTPUT_COL IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_PRODUCES IF NOT EXISTS",
                // Edge types — data flow / lineage
                "CREATE EDGE TYPE DATA_FLOW IF NOT EXISTS",
                "CREATE EDGE TYPE FILTER_FLOW IF NOT EXISTS",
                "CREATE EDGE TYPE JOIN_FLOW IF NOT EXISTS",
                "CREATE EDGE TYPE UNION_FLOW IF NOT EXISTS",
                // Edge types — affected columns + join sources
                "CREATE EDGE TYPE HAS_AFFECTED_COL IF NOT EXISTS",
                "CREATE EDGE TYPE AFFECTED_COL_REF_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE JOIN_SOURCE_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE JOIN_TARGET_TABLE IF NOT EXISTS",

                // Document types
                "CREATE DOCUMENT TYPE DaliSnippet IF NOT EXISTS",
                "CREATE DOCUMENT TYPE DaliSnippetScript IF NOT EXISTS",
                "CREATE DOCUMENT TYPE DaliPerfStats IF NOT EXISTS",
                "CREATE DOCUMENT TYPE DaliResolutionLog IF NOT EXISTS",
                "CREATE DOCUMENT TYPE DaliSchemaLog IF NOT EXISTS",
                "CREATE DOCUMENT TYPE DaliMeta IF NOT EXISTS",
        };
    }

    // ── Property declarations ────────────────────────────────────────────────

    static String[] propertyCommands() {
        return new String[]{
                // DaliApplication
                "CREATE PROPERTY DaliApplication.app_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliApplication.app_name IF NOT EXISTS STRING",
                // DaliDatabase
                "CREATE PROPERTY DaliDatabase.db_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDatabase.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDatabase.created_at IF NOT EXISTS LONG",
                // DaliSchema
                "CREATE PROPERTY DaliSchema.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSchema.db_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSchema.schema_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSchema.schema_name IF NOT EXISTS STRING",
                // DaliTable
                "CREATE PROPERTY DaliTable.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliTable.table_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliTable.table_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliTable.table_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliTable.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliTable.data_source IF NOT EXISTS STRING",  // v24
                // DaliColumn
                "CREATE PROPERTY DaliColumn.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.column_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.column_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.expression IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.alias IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.data_source IF NOT EXISTS STRING",  // v24
                "CREATE PROPERTY DaliColumn.ordinal_position IF NOT EXISTS INTEGER", // T13
                // DaliRoutine (v23: +return_type, +line_start; v24: +data_source)
                "CREATE PROPERTY DaliRoutine.routine_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.routine_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.routine_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.return_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.line_start IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliRoutine.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.data_source IF NOT EXISTS STRING",  // v24
                // DaliPackage
                "CREATE PROPERTY DaliPackage.package_name IF NOT EXISTS STRING",
                // DaliSession
                "CREATE PROPERTY DaliSession.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSession.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSession.file_path IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSession.dialect IF NOT EXISTS STRING",
                // DaliStatement
                "CREATE PROPERTY DaliStatement.stmt_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliStatement.type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliStatement.short_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliStatement.session_id IF NOT EXISTS STRING",
                // DaliAtom
                "CREATE PROPERTY DaliAtom.atom_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.atom_text IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.atom_context IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.parent_context IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.status IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.warning IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.merge_clause IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.session_id IF NOT EXISTS STRING",
                // DaliOutputColumn
                "CREATE PROPERTY DaliOutputColumn.name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliOutputColumn.expression IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliOutputColumn.alias IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliOutputColumn.session_id IF NOT EXISTS STRING",
                // DaliJoin
                "CREATE PROPERTY DaliJoin.join_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliJoin.session_id IF NOT EXISTS STRING",
                // DaliParameter
                "CREATE PROPERTY DaliParameter.param_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliParameter.param_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliParameter.param_mode IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliParameter.session_id IF NOT EXISTS STRING",
                // DaliVariable
                "CREATE PROPERTY DaliVariable.var_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliVariable.var_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliVariable.session_id IF NOT EXISTS STRING",
                // DaliAffectedColumn
                "CREATE PROPERTY DaliAffectedColumn.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.statement_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.column_ref IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.column_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.table_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.dataset_alias IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.source_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.resolution_status IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.type_affect IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAffectedColumn.order_affect IF NOT EXISTS INTEGER",
                // DaliSnippet (v22: +line_start, +line_end)
                "CREATE PROPERTY DaliSnippet.stmt_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.snippet IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.line_start IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliSnippet.line_end IF NOT EXISTS INTEGER",
                // DaliSnippetScript (v22)
                "CREATE PROPERTY DaliSnippetScript.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.file_path IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.script IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.script_hash IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.line_count IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliSnippetScript.char_count IF NOT EXISTS INTEGER",
                // DaliPerfStats
                "CREATE PROPERTY DaliPerfStats.db_name IF NOT EXISTS STRING",
        };
    }

    // ── Index declarations ───────────────────────────────────────────────────

    static String[] indexCommands() {
        return new String[]{
                // UNIQUE_HASH — canonical deduplication
                "CREATE INDEX IF NOT EXISTS ON DaliApplication (app_geoid) UNIQUE_HASH NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliDatabase (db_geoid) UNIQUE_HASH NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliSchema (db_name, schema_geoid) UNIQUE_HASH NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliTable (db_name, table_geoid) UNIQUE_HASH NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliColumn (db_name, column_geoid) UNIQUE_HASH NULL_STRATEGY SKIP",

                // NOTUNIQUE LSM_TREE — session_id lookups (high cardinality)
                "CREATE INDEX IF NOT EXISTS ON DaliStatement (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliRoutine (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliAtom (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliJoin (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliTable (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliColumn (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliParameter (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliVariable (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliOutputColumn (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliAffectedColumn (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliStatement (short_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliSnippetScript (session_id) NOTUNIQUE NULL_STRATEGY SKIP",

                // v26 migration: drop old FULL_TEXT indexes so IF NOT EXISTS creates the new LSM_TREE ones.
                // Safe to remove in v27 once all remote DBs have been upgraded.
                "DROP INDEX IF EXISTS `DaliApplication[app_name]`",
                "DROP INDEX IF EXISTS `DaliDatabase[db_name]`",
                "DROP INDEX IF EXISTS `DaliSchema[schema_name]`",
                "DROP INDEX IF EXISTS `DaliTable[table_name]`",
                "DROP INDEX IF EXISTS `DaliColumn[column_name]`",
                "DROP INDEX IF EXISTS `DaliRoutine[routine_name]`",
                "DROP INDEX IF EXISTS `DaliPackage[package_name]`",
                "DROP INDEX IF EXISTS `DaliSession[file_path]`",
                "DROP INDEX IF EXISTS `DaliStatement[stmt_geoid]`",
                "DROP INDEX IF EXISTS `DaliAtom[atom_text]`",
                "DROP INDEX IF EXISTS `DaliParameter[param_name]`",
                "DROP INDEX IF EXISTS `DaliVariable[var_name]`",
                "DROP INDEX IF EXISTS `DaliOutputColumn[name]`",
                // NOTUNIQUE LSM_TREE on name fields (v26: equality lookup support)
                "CREATE INDEX IF NOT EXISTS ON DaliApplication (app_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliDatabase (db_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliSchema (schema_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliTable (table_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliColumn (column_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliRoutine (routine_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliPackage (package_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliSession (file_path) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliStatement (stmt_geoid) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliAtom (atom_text) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliParameter (param_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliVariable (var_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliOutputColumn (name) NOTUNIQUE NULL_STRATEGY SKIP",
                // FULLTEXT — per-statement SQL text search (kept intentionally)
                "CREATE INDEX IF NOT EXISTS ON DaliSnippet (snippet) FULL_TEXT" + FT_METADATA,
                // DaliSnippetScript.script is intentionally NOT indexed — whole-file field
                // (up to hundreds of KB) exceeds ArcadeDB's 255 KB page limit for FULL_TEXT
                // indexes, causing index corruption and multi-GB on-disk bloat (v25 fix).
        };
    }

    // ── Combined — ordered: types → properties → indexes ────────────────────

    static String[] all() {
        List<String> result = new ArrayList<>();
        for (String s : typeCommands())     result.add(s);
        for (String s : propertyCommands()) result.add(s);
        for (String s : indexCommands())    result.add(s);
        return result.toArray(new String[0]);
    }
}
