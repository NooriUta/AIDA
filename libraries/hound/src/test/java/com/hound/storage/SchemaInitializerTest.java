package com.hound.storage;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.graph.MutableVertex;
import com.arcadedb.query.sql.executor.ResultSet;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SchemaInitializer — ADR-012 inheritance verification.
 *
 * Uses an embedded ArcadeDB in a temp directory (auto-cleaned).
 */
class SchemaInitializerTest {

    private static final String TEST_DB = "target/test-db-schema";
    private Database db;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up any previous test DB
        Path dbPath = Path.of(TEST_DB);
        if (Files.exists(dbPath)) {
            try (var walk = Files.walk(dbPath)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
        DatabaseFactory factory = new DatabaseFactory(TEST_DB);
        db = factory.create();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (db != null && db.isOpen()) db.close();
        Path dbPath = Path.of(TEST_DB);
        if (Files.exists(dbPath)) {
            try (var walk = Files.walk(dbPath)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }

    @Test
    @DisplayName("ADR-012: DaliPackage EXTENDS DaliRoutine in schema")
    void daliPackageExtendsDaliRoutine() {
        SchemaInitializer.ensureSchema(db);

        // Verify both types exist
        assertTrue(db.getSchema().existsType("DaliRoutine"), "DaliRoutine type must exist");
        assertTrue(db.getSchema().existsType("DaliPackage"), "DaliPackage type must exist");

        // Verify inheritance: DaliRoutine is a supertype of DaliPackage
        var routineType = db.getSchema().getType("DaliRoutine");
        assertTrue(routineType.isSuperTypeOf("DaliPackage"),
                "DaliRoutine must be a supertype of DaliPackage (IS-A)");
    }

    @Test
    @DisplayName("ADR-012: DaliPackage vertex is found by polymorphic SELECT FROM DaliRoutine")
    void daliPackageFoundByPolymorphicQuery() {
        SchemaInitializer.ensureSchema(db);

        db.transaction(() -> {
            // Insert a DaliPackage
            db.newVertex("DaliPackage")
                    .set("session_id", "test-session")
                    .set("package_geoid", "MY_PACKAGE")
                    .set("package_name", "MY_PACKAGE")
                    .set("routine_geoid", "MY_PACKAGE")
                    .set("routine_name", "MY_PACKAGE")
                    .set("routine_type", "PACKAGE")
                    .save();

            // Insert a regular DaliRoutine
            db.newVertex("DaliRoutine")
                    .set("session_id", "test-session")
                    .set("routine_geoid", "PROCEDURE:MY_PROC")
                    .set("routine_name", "MY_PROC")
                    .set("routine_type", "PROCEDURE")
                    .save();
        });

        // Polymorphic query: SELECT FROM DaliRoutine should return BOTH
        ResultSet rs = db.query("sql", "SELECT FROM DaliRoutine WHERE session_id = 'test-session'");
        int count = 0;
        boolean foundPackage = false;
        boolean foundProcedure = false;
        while (rs.hasNext()) {
            var record = rs.next().toMap();
            String rType = (String) record.get("routine_type");
            if ("PACKAGE".equals(rType)) foundPackage = true;
            if ("PROCEDURE".equals(rType)) foundProcedure = true;
            count++;
        }

        assertEquals(2, count, "Polymorphic query on DaliRoutine should return both package and procedure");
        assertTrue(foundPackage, "DaliPackage should be found via SELECT FROM DaliRoutine");
        assertTrue(foundProcedure, "DaliRoutine (PROCEDURE) should be found via SELECT FROM DaliRoutine");
    }

    @Test
    @DisplayName("ADR-012: CONTAINS_PACKAGE edge type is NOT created in v6 schema")
    void containsPackageEdgeNotCreated() {
        SchemaInitializer.ensureSchema(db);

        // CONTAINS_PACKAGE should NOT exist in fresh v6 schema
        assertFalse(db.getSchema().existsType("CONTAINS_PACKAGE"),
                "CONTAINS_PACKAGE edge type should not be created in v6 schema");

        // CONTAINS_ROUTINE should exist (used for both packages and routines)
        assertTrue(db.getSchema().existsType("CONTAINS_ROUTINE"),
                "CONTAINS_ROUTINE edge type must exist");
    }

    @Test
    @DisplayName("ADR-012: CONTAINS_ROUTINE edge works for Schema→Package")
    void containsRoutineEdgeWorksForPackages() {
        SchemaInitializer.ensureSchema(db);

        db.transaction(() -> {
            MutableVertex schema = db.newVertex("DaliSchema")
                    .set("session_id", "test-session")
                    .set("schema_geoid", "MY_SCHEMA")
                    .set("schema_name", "MY_SCHEMA")
                    .save();

            MutableVertex pkg = db.newVertex("DaliPackage")
                    .set("session_id", "test-session")
                    .set("package_geoid", "MY_SCHEMA.MY_PKG")
                    .set("package_name", "MY_PKG")
                    .set("routine_geoid", "MY_SCHEMA.MY_PKG")
                    .set("routine_name", "MY_PKG")
                    .set("routine_type", "PACKAGE")
                    .save();

            // Link using CONTAINS_ROUTINE (not CONTAINS_PACKAGE)
            schema.newEdge("CONTAINS_ROUTINE", pkg, true, new Object[0])
                    .set("session_id", "test-session")
                    .save();
        });

        // Verify the edge exists and is traversable
        ResultSet rs = db.query("sql",
                "SELECT expand(out('CONTAINS_ROUTINE')) FROM DaliSchema WHERE schema_geoid = 'MY_SCHEMA'");
        assertTrue(rs.hasNext(), "CONTAINS_ROUTINE edge from DaliSchema to DaliPackage should be traversable");
        var target = rs.next().toMap();
        assertEquals("MY_PKG", target.get("package_name"));
        assertEquals("PACKAGE", target.get("routine_type"));
    }

    @Test
    @DisplayName("Schema version is 26")
    void schemaVersionIsTwentySix() {
        SchemaInitializer.ensureSchema(db);

        ResultSet rs = db.query("sql", "SELECT schema_version FROM DaliMeta LIMIT 1");
        assertTrue(rs.hasNext(), "DaliMeta should have a record");
        int version = ((Number) rs.next().toMap().get("schema_version")).intValue();
        assertEquals(26, version, "Schema version should be 26");
    }

    @Test
    @DisplayName("v26: only DaliSnippet(snippet) has a FULL_TEXT index; name fields use NOTUNIQUE LSM_TREE")
    void fullTextOnlyOnSnippet() {
        SchemaInitializer.ensureSchema(db);

        var indexes = db.getSchema().getIndexes();

        // 1. Only DaliSnippet(snippet) must be FULL_TEXT
        for (var idx : indexes) {
            if ("FULL_TEXT".equals(idx.getType().name())) {
                assertEquals("DaliSnippet", idx.getTypeName(),
                        "FULL_TEXT index found on unexpected type: " + idx.getTypeName()
                        + " property: " + idx.getPropertyNames());
                assertEquals("snippet", idx.getPropertyNames().get(0),
                        "FULL_TEXT index on DaliSnippet must be on 'snippet', not: "
                        + idx.getPropertyNames());
            }
        }

        // 2. DaliSchema(schema_name) must be NOTUNIQUE (LSM_TREE), not FULL_TEXT
        boolean hasLsmSchemaName = Arrays.stream(indexes)
                .anyMatch(idx -> "DaliSchema".equals(idx.getTypeName())
                        && idx.getPropertyNames() != null
                        && idx.getPropertyNames().contains("schema_name")
                        && !"FULL_TEXT".equals(idx.getType().name()));
        assertTrue(hasLsmSchemaName,
                "DaliSchema(schema_name) must have a non-FULL_TEXT index (NOTUNIQUE LSM_TREE)");

        // 3. DaliAtom(atom_text) must also be NOTUNIQUE, not FULL_TEXT
        boolean hasLsmAtomText = Arrays.stream(indexes)
                .anyMatch(idx -> "DaliAtom".equals(idx.getTypeName())
                        && idx.getPropertyNames() != null
                        && idx.getPropertyNames().contains("atom_text")
                        && !"FULL_TEXT".equals(idx.getType().name()));
        assertTrue(hasLsmAtomText,
                "DaliAtom(atom_text) must have a non-FULL_TEXT index (NOTUNIQUE LSM_TREE)");
    }

    @Test
    @DisplayName("v25: DaliSnippetScript(script) has NO FULL_TEXT index")
    void snippetScriptHasNoFullTextIndexOnScript() {
        SchemaInitializer.ensureSchema(db);

        // The FULL_TEXT index on script was removed in v25 because the field stores whole-file
        // text (up to hundreds of KB) which exceeds ArcadeDB's 255 KB per-page limit.
        boolean hasBadIndex = Arrays.stream(db.getSchema().getIndexes())
                .anyMatch(idx -> "DaliSnippetScript".equals(idx.getTypeName())
                        && idx.getPropertyNames() != null
                        && !idx.getPropertyNames().isEmpty()
                        && "script".equals(idx.getPropertyNames().get(0)));
        assertFalse(hasBadIndex,
                "DaliSnippetScript must NOT have a FULL_TEXT index on script (page-size overflow)");
    }

    @Test
    @DisplayName("ADR-013: DaliRoutine has return_type and line_start properties")
    void daliRoutineHasReturnTypeAndLineStart() {
        SchemaInitializer.ensureSchema(db);

        var routineType = db.getSchema().getType("DaliRoutine");
        assertTrue(routineType.existsProperty("return_type"),
                "DaliRoutine must have return_type property");
        assertTrue(routineType.existsProperty("line_start"),
                "DaliRoutine must have line_start property");
    }
}
