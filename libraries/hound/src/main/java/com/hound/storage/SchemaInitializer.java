// File: src/main/java/com/hound/storage/SchemaInitializer.java
package com.hound.storage;

// STATUS: embedded ArcadeDB branch DISABLED as of ArcadeDB 26.x upgrade (chore/arcadedb-26-no-embedded).
// EmbeddedWriter.java and ensureSchema(Database) were removed — arcadedb-network client only.
//
// NOTE(arcadedb-embed): re-enabling embedded mode is BLOCKED on upstream — ArcadeDB pins
// antlr4=4.9.1 (Gremlin compat, ArcadeData/arcadedb#3235) while our hound grammars need 4.13.2.
// Re-evaluate after July 2026 once ArcadeDB 27.x with ANTLR ≥ 4.13 lands.
// When that becomes possible, restore G6/G8/DaliRecordField types in ensureSchema():
//   edges: BULK_COLLECTS_INTO, RECORD_USED_IN, HAS_RECORD_FIELD, FIELD_MAPS_TO
//   vertex: DaliRecord, DaliRecordField
// NOT tech-debt: external blocker, no in-repo action available.

/**
 * Creates the ArcadeDB schema for all Dali vertex/edge/document types.
 *
 * <p>Remote mode only: delegates to {@link RemoteSchemaCommands#all()}.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {}

    /** Delegates to {@link RemoteSchemaCommands#all()} — types → properties → indexes. */
    public static String[] remoteSchemaCommands() {
        return RemoteSchemaCommands.all();
    }
}
