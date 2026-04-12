// File: src/main/java/com/hound/storage/SchemaInitializer.java
package com.hound.storage;

// TODO(arcadedb-embed): ensureSchema(Database) removed during 26.x upgrade.
// Restore full embedded schema initialisation after engine API stabilises.
// G6/G8/DaliRecordField types (BULK_COLLECT_INTO, RECORD_USED_IN, HAS_RECORD_FIELD,
// FIELD_MAPS_TO) must be added to remoteSchemaCommands() and ensureSchema() when restored.

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
