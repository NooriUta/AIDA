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
                "CREATE VERTEX TYPE DaliRecord IF NOT EXISTS",
                // v27: dedicated vertex for DDL (ALTER/CREATE/DROP) — schema-mutating statements
                "CREATE VERTEX TYPE DaliDDLStatement IF NOT EXISTS",
                // KI-RETURN-1: named field of a DaliRecord (BULK COLLECT / RETURNING INTO targets)
                "CREATE VERTEX TYPE DaliRecordField IF NOT EXISTS",
                // HND-01: PL/SQL user-defined TYPE templates (RECORD / COLLECTION)
                "CREATE VERTEX TYPE DaliPlType IF NOT EXISTS",
                "CREATE VERTEX TYPE DaliPlTypeField IF NOT EXISTS",
                // HAL3-04 (ADR-HND-011): explicit cursor vertex
                "CREATE VERTEX TYPE DaliCursor IF NOT EXISTS",

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
                "CREATE EDGE TYPE CALLS IF NOT EXISTS",
                // ROUTINE_USES_TABLE removed (Sprint 0.1 SCHEMA_CLEANUP, EDGE_TAXONOMY_ANALYSIS §13.5).
                //   Reason: 0 writer + 0 consumer + не запланирован. Redesign-план в §15.6.
                // Edge types — atom resolution
                "CREATE EDGE TYPE ATOM_REF_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_COLUMN IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_STMT IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_OUTPUT_COL IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_REF_PLTYPE_FIELD IF NOT EXISTS",
                "CREATE EDGE TYPE ATOM_PRODUCES IF NOT EXISTS",
                // Edge types — data flow / lineage
                "CREATE EDGE TYPE DATA_FLOW IF NOT EXISTS",
                "CREATE EDGE TYPE FILTER_FLOW IF NOT EXISTS",
                // JOIN_FLOW, UNION_FLOW removed (Sprint 0.1 SCHEMA_CLEANUP, §13.5).
                //   Reason: 0 writer + 0 consumer + не запланирован. Redesign-план в §15.1, §15.2.
                // Edge types — record (BULK COLLECT)
                "CREATE EDGE TYPE BULK_COLLECTS_INTO IF NOT EXISTS",
                // D-1 (Sprint 1.3): RECORD_USED_IN removed — reverse traversal via inE('BULK_COLLECTS_INTO')
                // KI-RETURN-1: record field membership + RETURNING INTO
                // D-3 (Sprint 1.3): HAS_RECORD_FIELD split → RECORD_HAS_FIELD (DaliRecord→DaliRecordField)
                //                                           + PLTYPE_HAS_FIELD (DaliPlType→DaliPlTypeField)
                "CREATE EDGE TYPE RECORD_HAS_FIELD IF NOT EXISTS",
                "CREATE EDGE TYPE PLTYPE_HAS_FIELD IF NOT EXISTS",
                "CREATE EDGE TYPE RETURNS_INTO IF NOT EXISTS",
                // Edge types — affected columns + join sources
                "CREATE EDGE TYPE HAS_AFFECTED_COL IF NOT EXISTS",
                "CREATE EDGE TYPE AFFECTED_COL_REF_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE JOIN_SOURCE_TABLE IF NOT EXISTS",
                "CREATE EDGE TYPE JOIN_TARGET_TABLE IF NOT EXISTS",
                // KI-DDL-1: DDL modifier edge — F-2 folding (Sprint 0.1, §13.8 F-2).
                //   Was: DaliDDLModifiesTable (DDL→Table), DaliDDLModifiesColumn (DDL→Column).
                //   Now: DDL_MODIFIES with target_kind property: 'table' | 'column'.
                //   Также переименование PascalCase → UPPER_SNAKE (см. §5.5).
                "CREATE EDGE TYPE DDL_MODIFIES IF NOT EXISTS",
                // KI-PIPE-1 removed: PIPES_FROM, READS_PIPELINED (Sprint 0.1 SCHEMA_CLEANUP, §13.5).
                //   Reason: 0 writer + 0 consumer + не запланирован. Redesign-план в §15.3, §15.4.
                // HND-01: PL/SQL TYPE edges (EXTENDS E not supported by ArcadeDB; plain edge type is the base)
                "CREATE EDGE TYPE DECLARES_TYPE IF NOT EXISTS",
                "CREATE EDGE TYPE OF_TYPE IF NOT EXISTS",
                "CREATE EDGE TYPE INSTANTIATES_TYPE IF NOT EXISTS",
                // HND-13 removed: CURSOR_RETURNS (Sprint 0.1 SCHEMA_CLEANUP, §13.5).
                //   Reason: 0 writer + 0 consumer + не запланирован. Redesign-план в §15.5.
                // HND-15: CAST(MULTISET(SELECT...) AS t_list) → DaliPlType
                "CREATE EDGE TYPE MULTISET_INTO IF NOT EXISTS",
                // KI-005: UNIQUE and CHECK constraint vertex + edge types
                "CREATE VERTEX TYPE DaliUniqueConstraint IF NOT EXISTS EXTENDS DaliConstraint",
                "CREATE VERTEX TYPE DaliCheckConstraint IF NOT EXISTS EXTENDS DaliConstraint",
                "CREATE EDGE TYPE HAS_UNIQUE_KEY IF NOT EXISTS",
                "CREATE EDGE TYPE IS_UNIQUE_COLUMN IF NOT EXISTS",
                "CREATE EDGE TYPE HAS_CHECK IF NOT EXISTS",

                // DaliSnippet — DOCUMENT type (large SQL texts; bulk payload risk if promoted to VERTEX)
                // Linked to DaliStatement via stmt_geoid text field + NOTUNIQUE index (fast O(1) lookup).
                // HAS_SNIPPET graph edge deliberately NOT used: loading large snippets as vertices
                // adds memory pressure on TRAVERSE and risks batch-endpoint payload overflow.
                "CREATE DOCUMENT TYPE DaliSnippet IF NOT EXISTS",
                "CREATE DOCUMENT TYPE DaliSnippetScript IF NOT EXISTS",

                // ═══════════════════════════════════════════════════════════════════
                // Sprint 1.1 EDGE_TAXONOMY_V1 (ADR-HND-009) — ABSTRACT hierarchy
                // ═══════════════════════════════════════════════════════════════════
                //
                // Round 9 finding: ArcadeDB 26.4.2 не поддерживает 'EXTENDS E ABSTRACT'
                // и 'ALTER TYPE X ABSTRACT TRUE'. Используем pattern:
                //   1) CREATE EDGE TYPE parent (без ABSTRACT keyword)
                //   2) ALTER TYPE parent CUSTOM abstract = true (logical marker)
                //   3) CREATE EDGE TYPE child EXTENDS parent  (новые типы)
                //   4) ALTER TYPE existing SUPERTYPE +parent  (существующие — добавляем родителя)
                // Convention-level enforcement: writers НИКОГДА не вызывают
                // appendEdge('parentName', ...) — только конкретные подтипы.
                //
                // ─── 8 top-level logical-abstract types ──────────────────────────
                "CREATE EDGE TYPE ATOM_REF IF NOT EXISTS",
                "ALTER TYPE ATOM_REF CUSTOM abstract = true",
                "CREATE EDGE TYPE NAMESPACE IF NOT EXISTS",
                "ALTER TYPE NAMESPACE CUSTOM abstract = true",
                "CREATE EDGE TYPE STMT_HAS IF NOT EXISTS",
                "ALTER TYPE STMT_HAS CUSTOM abstract = true",
                "CREATE EDGE TYPE LINEAGE_FLOW IF NOT EXISTS",  // super-group для всех flow
                "ALTER TYPE LINEAGE_FLOW CUSTOM abstract = true",
                "CREATE EDGE TYPE DDL_OP IF NOT EXISTS",
                "ALTER TYPE DDL_OP CUSTOM abstract = true",
                "CREATE EDGE TYPE JOIN_REF IF NOT EXISTS",
                "ALTER TYPE JOIN_REF CUSTOM abstract = true",
                "CREATE EDGE TYPE PLTYPE_REF IF NOT EXISTS",
                "ALTER TYPE PLTYPE_REF CUSTOM abstract = true",
                "CREATE EDGE TYPE CONSTRAINT_REF IF NOT EXISTS",
                "ALTER TYPE CONSTRAINT_REF CUSTOM abstract = true",

                // ─── 4 nested under LINEAGE_FLOW (multi-level inheritance) ───────
                "CREATE EDGE TYPE FLOW IF NOT EXISTS EXTENDS LINEAGE_FLOW",
                "ALTER TYPE FLOW CUSTOM abstract = true",
                "CREATE EDGE TYPE TABLE_DATA_FLOW IF NOT EXISTS EXTENDS LINEAGE_FLOW",
                "ALTER TYPE TABLE_DATA_FLOW CUSTOM abstract = true",
                "CREATE EDGE TYPE RECORD_FLOW IF NOT EXISTS EXTENDS LINEAGE_FLOW",
                "ALTER TYPE RECORD_FLOW CUSTOM abstract = true",
                "CREATE EDGE TYPE WRITE_SIDE IF NOT EXISTS EXTENDS LINEAGE_FLOW",
                "ALTER TYPE WRITE_SIDE CUSTOM abstract = true",
                // HAL3-01: concrete WRITE_SIDE subtypes (Bucket B)
                "CREATE EDGE TYPE ASSIGNS_TO_VARIABLE IF NOT EXISTS EXTENDS WRITE_SIDE",
                "CREATE EDGE TYPE WRITES_TO_PARAMETER IF NOT EXISTS EXTENDS WRITE_SIDE",
                "CREATE EDGE TYPE READS_FROM_CURSOR IF NOT EXISTS EXTENDS WRITE_SIDE",

                // ─── ALTER existing types: assign supertype ──────────────────────
                // ATOM_REF subtypes (5)
                "ALTER TYPE ATOM_REF_TABLE SUPERTYPE +ATOM_REF",
                "ALTER TYPE ATOM_REF_COLUMN SUPERTYPE +ATOM_REF",
                "ALTER TYPE ATOM_REF_STMT SUPERTYPE +ATOM_REF",
                "ALTER TYPE ATOM_REF_OUTPUT_COL SUPERTYPE +ATOM_REF",
                "ALTER TYPE ATOM_REF_PLTYPE_FIELD SUPERTYPE +ATOM_REF",
                // NAMESPACE subtypes (9)
                "ALTER TYPE BELONGS_TO_APP SUPERTYPE +NAMESPACE",
                "ALTER TYPE CONTAINS_SCHEMA SUPERTYPE +NAMESPACE",
                "ALTER TYPE CONTAINS_TABLE SUPERTYPE +NAMESPACE",
                "ALTER TYPE HAS_COLUMN SUPERTYPE +NAMESPACE",
                "ALTER TYPE CONTAINS_ROUTINE SUPERTYPE +NAMESPACE",
                "ALTER TYPE BELONGS_TO_SESSION SUPERTYPE +NAMESPACE",
                "ALTER TYPE CONTAINS_STMT SUPERTYPE +NAMESPACE",
                "ALTER TYPE HAS_PARAMETER SUPERTYPE +NAMESPACE",
                "ALTER TYPE HAS_VARIABLE SUPERTYPE +NAMESPACE",
                // STMT_HAS subtypes (3)
                "ALTER TYPE HAS_OUTPUT_COL SUPERTYPE +STMT_HAS",
                "ALTER TYPE HAS_JOIN SUPERTYPE +STMT_HAS",
                "ALTER TYPE HAS_AFFECTED_COL SUPERTYPE +STMT_HAS",
                // FLOW subtypes (2 — JOIN_FLOW, UNION_FLOW удалены в Sprint 0.1)
                "ALTER TYPE DATA_FLOW SUPERTYPE +FLOW",
                "ALTER TYPE FILTER_FLOW SUPERTYPE +FLOW",
                // TABLE_DATA_FLOW subtypes (2; READS_FROM direction inverted in Sprint 1.2)
                "ALTER TYPE READS_FROM SUPERTYPE +TABLE_DATA_FLOW",
                "ALTER TYPE WRITES_TO SUPERTYPE +TABLE_DATA_FLOW",
                // RECORD_FLOW subtypes (2 — RECORD_USED_IN/HAS_RECORD_FIELD остаются singletons)
                "ALTER TYPE BULK_COLLECTS_INTO SUPERTYPE +RECORD_FLOW",
                "ALTER TYPE RETURNS_INTO SUPERTYPE +RECORD_FLOW",
                // JOIN_REF subtypes (2)
                "ALTER TYPE JOIN_SOURCE_TABLE SUPERTYPE +JOIN_REF",
                "ALTER TYPE JOIN_TARGET_TABLE SUPERTYPE +JOIN_REF",
                // PLTYPE_REF subtypes (3 + MULTISET_INTO как singleton lineage-skip)
                "ALTER TYPE DECLARES_TYPE SUPERTYPE +PLTYPE_REF",
                "ALTER TYPE OF_TYPE SUPERTYPE +PLTYPE_REF",
                "ALTER TYPE INSTANTIATES_TYPE SUPERTYPE +PLTYPE_REF",
                // DDL_OP subtypes (1 — DDL_MODIFIES после F-2 folding в Sprint 0.1)
                "ALTER TYPE DDL_MODIFIES SUPERTYPE +DDL_OP",
                // CONSTRAINT_REF subtypes assigned in Phase 3 (after reality-check + folding F-1)

                // ─── 4 new ATOM_REF subtypes (DDL only — writers in Phase 2 atom pipeline) ───
                "CREATE EDGE TYPE ATOM_REF_VARIABLE IF NOT EXISTS EXTENDS ATOM_REF",
                "CREATE EDGE TYPE ATOM_REF_PARAMETER IF NOT EXISTS EXTENDS ATOM_REF",
                "CREATE EDGE TYPE ATOM_REF_FUNCTION IF NOT EXISTS EXTENDS ATOM_REF",
                "CREATE EDGE TYPE ATOM_REF_SEQUENCE IF NOT EXISTS EXTENDS ATOM_REF",
                "CREATE PROPERTY ATOM_REF_PARAMETER.param_mode IF NOT EXISTS STRING",  // IN | OUT | INOUT (specific to PARAMETER)
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
                "CREATE PROPERTY DaliTable.dblink IF NOT EXISTS STRING",        // KI-DBLINK-1
                "CREATE PROPERTY DaliTable.pl_type_geoid IF NOT EXISTS STRING",  // HND-04: VTABLE → DaliPlType
                // DaliColumn
                "CREATE PROPERTY DaliColumn.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.column_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.column_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.expression IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.alias IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.data_source IF NOT EXISTS STRING",  // v24
                "CREATE PROPERTY DaliColumn.ordinal_position IF NOT EXISTS INTEGER", // T13
                "CREATE PROPERTY DaliColumn.data_type IF NOT EXISTS STRING",    // T14: DDL-declared type
                "CREATE PROPERTY DaliColumn.is_required IF NOT EXISTS BOOLEAN",  // T14: NOT NULL constraint
                "CREATE PROPERTY DaliColumn.default_value IF NOT EXISTS STRING",  // T14: DEFAULT expression
                "CREATE PROPERTY DaliColumn.is_pk IF NOT EXISTS BOOLEAN",         // T14: participates in PRIMARY KEY
                "CREATE PROPERTY DaliColumn.is_fk IF NOT EXISTS BOOLEAN",         // T14: FOREIGN KEY column
                "CREATE PROPERTY DaliColumn.fk_ref_table IF NOT EXISTS STRING",   // T14: FK referenced table geoid
                "CREATE PROPERTY DaliColumn.fk_ref_column IF NOT EXISTS STRING",  // T14: FK referenced column name
                "CREATE PROPERTY DaliColumn.inferred IF NOT EXISTS BOOLEAN",
                "CREATE PROPERTY DaliColumn.source_pass IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliColumn.suspicious IF NOT EXISTS BOOLEAN",
                // DaliRoutine (v23: +return_type, +line_start; v24: +data_source)
                "CREATE PROPERTY DaliRoutine.routine_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.routine_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.routine_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.return_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.line_start IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliRoutine.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRoutine.data_source IF NOT EXISTS STRING",  // v24
                "CREATE PROPERTY DaliRoutine.has_spec IF NOT EXISTS BOOLEAN",   // spec+body merge
                "CREATE PROPERTY DaliRoutine.has_body IF NOT EXISTS BOOLEAN",   // spec+body merge
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
                // DaliAtom (HAL-01 ADR-HND-002: primary_status + qualifier replace status)
                "CREATE PROPERTY DaliAtom.atom_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.atom_text IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.atom_geoid IF NOT EXISTS STRING",   // composite uniqueness key text~line:col
                "CREATE PROPERTY DaliAtom.atom_context IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.parent_context IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.primary_status IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.qualifier IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.kind IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.confidence IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.resolve_strategy IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.routine_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.pending_verification IF NOT EXISTS BOOLEAN",
                "CREATE PROPERTY DaliAtom.last_verified_session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.pending_kind IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.pending_snapshot IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliAtom.pending_since IF NOT EXISTS LONG",
                "CREATE PROPERTY DaliAtom.status IF NOT EXISTS STRING",       // legacy — kept for migration, removed in V2
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
                // DaliCursor (HAL3-04, ADR-HND-011)
                "CREATE PROPERTY DaliCursor.cursor_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliCursor.cursor_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliCursor.routine_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliCursor.select_stmt_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliCursor.session_id IF NOT EXISTS STRING",
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
                // DaliRecord (G6: BULK COLLECT target)
                "CREATE PROPERTY DaliRecord.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecord.record_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecord.record_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecord.routine_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecord.source_stmt_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecord.fields IF NOT EXISTS STRING",
                // DaliRecordField (KI-RETURN-1)
                "CREATE PROPERTY DaliRecordField.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecordField.field_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecordField.field_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecordField.field_order IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliRecordField.record_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecordField.data_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliRecordField.ordinal_position IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliRecordField.source_column_geoid IF NOT EXISTS STRING",
                // RETURNS_INTO edge — returning_exprs carries the returned column list
                "CREATE PROPERTY RETURNS_INTO.returning_exprs IF NOT EXISTS STRING",
                // DaliDDLStatement (v27: ALTER / CREATE / DROP)
                "CREATE PROPERTY DaliDDLStatement.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDDLStatement.db_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDDLStatement.stmt_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDDLStatement.type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDDLStatement.line_start IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliDDLStatement.line_end IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliDDLStatement.target_table_geoids IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliDDLStatement.short_name IF NOT EXISTS STRING",
                // DaliSnippet (v22: +line_start/end; v28: +element_rid/element_type — direct @rid link)
                "CREATE PROPERTY DaliSnippet.stmt_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.snippet IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.snippet_hash IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.line_start IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliSnippet.line_end IF NOT EXISTS INTEGER",
                // v28: explicit link to the owning element (DaliStatement / DaliDDLStatement / DaliRoutine)
                // element_rid = ArcadeDB @rid of the element, e.g. "#15:42"
                // element_type = "DaliStatement" | "DaliDDLStatement" | "DaliRoutine"
                // No graph edge needed — DOCUMENT stays DOCUMENT; element_rid gives direct @rid lookup
                "CREATE PROPERTY DaliSnippet.element_rid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippet.element_type IF NOT EXISTS STRING",
                // DaliSnippetScript (v22)
                "CREATE PROPERTY DaliSnippetScript.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.file_path IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.script IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.script_hash IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliSnippetScript.line_count IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliSnippetScript.char_count IF NOT EXISTS INTEGER",
                // ── DaliConstraint / DaliPrimaryKey / DaliForeignKey ────────────────────
                // Base constraint vertex type (PK, FK, UQ, CH share these properties)
                "CREATE VERTEX TYPE DaliConstraint IF NOT EXISTS",
                "CREATE PROPERTY DaliConstraint.constraint_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliConstraint.constraint_type IF NOT EXISTS STRING",   // PK | FK | UQ | CH
                "CREATE PROPERTY DaliConstraint.constraint_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliConstraint.table_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliConstraint.column_names IF NOT EXISTS STRING",      // JSON array
                "CREATE PROPERTY DaliConstraint.session_id IF NOT EXISTS STRING",
                // DaliPrimaryKey extends DaliConstraint
                "CREATE VERTEX TYPE DaliPrimaryKey IF NOT EXISTS EXTENDS DaliConstraint",
                // DaliForeignKey extends DaliConstraint — FK-specific fields
                "CREATE VERTEX TYPE DaliForeignKey IF NOT EXISTS EXTENDS DaliConstraint",
                "CREATE PROPERTY DaliForeignKey.ref_table_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliForeignKey.ref_column_names IF NOT EXISTS STRING",  // JSON array
                "CREATE PROPERTY DaliForeignKey.on_delete IF NOT EXISTS STRING",         // CASCADE | SET NULL | null
                // KI-DDL-1: properties on DDL_MODIFIES (folded — Sprint 0.1, §13.8 F-2)
                "CREATE PROPERTY DDL_MODIFIES.target_kind IF NOT EXISTS STRING",  // 'table' | 'column'
                "CREATE PROPERTY DDL_MODIFIES.operation IF NOT EXISTS STRING",     // ADD | MODIFY | DROP (column ops only)
                // ── Constraint edge types ───────────────────────────────────────────────
                // DaliTable ──HAS_PRIMARY_KEY──► DaliPrimaryKey
                "CREATE EDGE TYPE HAS_PRIMARY_KEY IF NOT EXISTS",
                // DaliTable ──HAS_FOREIGN_KEY──► DaliForeignKey
                "CREATE EDGE TYPE HAS_FOREIGN_KEY IF NOT EXISTS",
                // DaliPrimaryKey / DaliForeignKey ──IS_PK_COLUMN──► DaliColumn  (with order_id)
                "CREATE EDGE TYPE IS_PK_COLUMN IF NOT EXISTS",
                "CREATE PROPERTY IS_PK_COLUMN.order_id IF NOT EXISTS INTEGER",
                // DaliForeignKey ──IS_FK_COLUMN──► DaliColumn  (with order_id)
                "CREATE EDGE TYPE IS_FK_COLUMN IF NOT EXISTS",
                "CREATE PROPERTY IS_FK_COLUMN.order_id IF NOT EXISTS INTEGER",
                // DaliForeignKey ──REFERENCES_TABLE──► DaliTable
                "CREATE EDGE TYPE REFERENCES_TABLE IF NOT EXISTS",
                // DaliForeignKey ──REFERENCES_COLUMN──► DaliColumn  (with order_id)
                "CREATE EDGE TYPE REFERENCES_COLUMN IF NOT EXISTS",
                "CREATE PROPERTY REFERENCES_COLUMN.order_id IF NOT EXISTS INTEGER",
                // KI-PIPE-1: pipelined function flag
                "CREATE PROPERTY DaliRoutine.is_pipelined IF NOT EXISTS BOOLEAN",
                // KI-PRAGMA-1: autonomous transaction flag
                "CREATE PROPERTY DaliRoutine.autonomous_transaction IF NOT EXISTS BOOLEAN",
                // HND-01: PL/SQL TYPE vertices
                "CREATE PROPERTY DaliPlType.type_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlType.type_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlType.kind IF NOT EXISTS STRING",          // RECORD | COLLECTION
                "CREATE PROPERTY DaliPlType.element_type_geoid IF NOT EXISTS STRING",  // COLLECTION → OF_TYPE
                "CREATE PROPERTY DaliPlType.scope_geoid IF NOT EXISTS STRING",   // declaring package or routine
                "CREATE PROPERTY DaliPlType.declared_at_line IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliPlType.session_id IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlTypeField.field_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlTypeField.type_geoid IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlTypeField.field_name IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlTypeField.field_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliPlTypeField.position IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliPlTypeField.session_id IF NOT EXISTS STRING",
                // HND-01: DaliRecord back-ref to the PlType template it was instantiated from
                "CREATE PROPERTY DaliRecord.pl_type_geoid IF NOT EXISTS STRING",
                // KI-FLASHBACK-1: AS OF TIMESTAMP/SCN on DaliStatement
                "CREATE PROPERTY DaliStatement.flashback_type IF NOT EXISTS STRING",
                "CREATE PROPERTY DaliStatement.flashback_expr IF NOT EXISTS STRING",
                // KI-DBMSSQL-1: DBMS_SQL dynamic SQL marker on DaliStatement
                "CREATE PROPERTY DaliStatement.contains_dynamic_sql IF NOT EXISTS BOOLEAN",
                // KI-005: UNIQUE / CHECK constraint properties
                "CREATE PROPERTY IS_UNIQUE_COLUMN.order_id IF NOT EXISTS INTEGER",
                "CREATE PROPERTY DaliCheckConstraint.check_expression IF NOT EXISTS STRING",
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
                "CREATE INDEX IF NOT EXISTS ON DaliRecord (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliRecord (record_geoid) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliRecordField (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliRecordField (field_geoid) NOTUNIQUE NULL_STRATEGY SKIP",
                // HND-01: DaliPlType / DaliPlTypeField indexes
                "CREATE INDEX IF NOT EXISTS ON DaliPlType (session_id, type_geoid) UNIQUE_HASH NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliPlTypeField (session_id, type_geoid) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliStatement (short_name) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliSnippetScript (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliDDLStatement (session_id) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliDDLStatement (stmt_geoid) NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliDDLStatement (type) NOTUNIQUE NULL_STRATEGY SKIP",

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
                // DaliSnippet — session_id + stmt_geoid + element_rid lookups
                // (missing indices caused full-table scans on loadSnippets / knotSnippet)
                "CREATE INDEX IF NOT EXISTS ON DaliSnippet (session_id)  NOTUNIQUE NULL_STRATEGY SKIP",
                "CREATE INDEX IF NOT EXISTS ON DaliSnippet (stmt_geoid)  NOTUNIQUE NULL_STRATEGY SKIP",
                // v28: element_rid = ArcadeDB @rid of the element — O(1) lookup by node id
                "CREATE INDEX IF NOT EXISTS ON DaliSnippet (element_rid) NOTUNIQUE NULL_STRATEGY SKIP",
                // FULLTEXT — per-statement SQL text search (kept intentionally)
                "CREATE INDEX IF NOT EXISTS ON DaliSnippet (snippet) FULL_TEXT" + FT_METADATA,
                // DaliSnippetScript.script is intentionally NOT indexed — whole-file field
                // (up to hundreds of KB) exceeds ArcadeDB's 255 KB page limit for FULL_TEXT
                // indexes, causing index corruption and multi-GB on-disk bloat (v25 fix).
        };
    }

    // ── Migration — run ONCE on existing DBs to fix old indexes ─────────────
    //
    // Problem: the canonical UNIQUE indexes on (db_name, …) were created in
    // early deployments WITHOUT "NULL_STRATEGY SKIP".  ArcadeDB with the old
    // definition indexes NULL db_name values, so ad-hoc sessions that share the
    // same column / table geoid across runs hit DuplicatedKeyException:
    //
    //   Duplicated key [null, CRM.CS_ASSET_SERVICES.ASSET_SERIAL] on
    //   index 'DaliColumn[db_name,column_geoid]'
    //
    // Fix: drop the old indexes (IF EXISTS is safe on a fresh DB) so that
    // indexCommands() re-creates them with NULL_STRATEGY SKIP.
    // This method is included in all() and is idempotent: on a fresh DB the
    // DROP IF EXISTS is a no-op, on an old DB it removes the broken indexes
    // before they are recreated correctly.
    static String[] migrationCommands() {
        return new String[]{
                "DROP INDEX IF EXISTS `DaliSchema[db_name,schema_geoid]`",
                "DROP INDEX IF EXISTS `DaliTable[db_name,table_geoid]`",
                "DROP INDEX IF EXISTS `DaliColumn[db_name,column_geoid]`",
        };
    }

    // ── Combined — ordered: types → properties → migrations → indexes ───────

    static String[] all() {
        List<String> result = new ArrayList<>();
        for (String s : typeCommands())      result.add(s);
        for (String s : propertyCommands())  result.add(s);
        for (String s : migrationCommands()) result.add(s);  // drop old indexes before recreating
        for (String s : indexCommands())     result.add(s);
        return result.toArray(new String[0]);
    }
}
