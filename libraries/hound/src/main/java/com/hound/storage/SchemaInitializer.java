// File: src/main/java/com/hound/storage/SchemaInitializer.java
package com.hound.storage;

// STATUS: embedded ArcadeDB branch DISABLED as of ArcadeDB 26.x upgrade (chore/arcadedb-26-no-embedded).
// EmbeddedWriter.java and ensureSchema(Database) were removed — arcadedb-network client only.
//
// TODO(arcadedb-embed): restore when ArcadeDB 26 embedded engine API stabilises.
// When restoring, also add G6/G8/DaliRecordField types to ensureSchema():
//   edges: BULK_COLLECTS_INTO, RECORD_USED_IN, HAS_RECORD_FIELD, FIELD_MAPS_TO
//   vertex: DaliRecord, DaliRecordField

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
