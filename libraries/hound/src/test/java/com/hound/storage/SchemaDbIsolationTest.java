package com.hound.storage;

import org.junit.jupiter.api.Disabled;

/**
 * Tests for database-level isolation in embedded ArcadeDB.
 *
 * TODO(arcadedb-embed): restore after ArcadeDB 26.x engine API stabilises.
 * Requires arcadedb-engine dependency and SchemaInitializer.ensureSchema(Database).
 */
@Disabled("Embedded mode removed during ArcadeDB 26.x upgrade — restore after engine API stabilisation")
class SchemaDbIsolationTest {
}
