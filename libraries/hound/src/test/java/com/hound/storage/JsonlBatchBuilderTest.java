package com.hound.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hound.semantic.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonlBatchBuilder}.
 */
class JsonlBatchBuilderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testBuildFromResult_basicStructure() throws Exception {
        // Build a minimal SemanticResult with 2 tables, 3 columns, 1 statement
        Map<String, TableInfo> tables = new LinkedHashMap<>();
        tables.put("SCHEMA1.ORDERS", new TableInfo("SCHEMA1.ORDERS", "ORDERS", "SCHEMA1", "TABLE"));
        tables.put("SCHEMA1.ITEMS", new TableInfo("SCHEMA1.ITEMS", "ITEMS", "SCHEMA1", "TABLE"));

        Map<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("SCHEMA1.ORDERS.ID", new ColumnInfo("SCHEMA1.ORDERS.ID", "SCHEMA1.ORDERS", "ID", null, null, false, 0));
        columns.put("SCHEMA1.ORDERS.NAME", new ColumnInfo("SCHEMA1.ORDERS.NAME", "SCHEMA1.ORDERS", "NAME", null, null, false, 1));
        columns.put("SCHEMA1.ITEMS.PRICE", new ColumnInfo("SCHEMA1.ITEMS.PRICE", "SCHEMA1.ITEMS", "PRICE", null, null, false, 0));

        Map<String, StatementInfo> statements = new LinkedHashMap<>();
        StatementInfo stmt = new StatementInfo("STMT:1", "SELECT", "SELECT * FROM ORDERS", 1, 1, null, null);
        stmt.addSourceTable("SCHEMA1.ORDERS", "O");
        statements.put("STMT:1", stmt);

        Map<String, Object> schemas = new LinkedHashMap<>();
        schemas.put("SCHEMA1", Map.of("name", "SCHEMA1"));

        Structure str = new Structure(Map.of(), schemas, Map.of(), tables, columns, Map.of(), statements);

        SemanticResult result = new SemanticResult("test-sid", "/test.sql", "plsql", 100,
                str, List.of(), Map.of(), List.of(), Map.of(), List.of());

        JsonlBatchBuilder builder = JsonlBatchBuilder.buildFromResult("test-sid", result);
        String payload = builder.build();

        // Verify basic counts
        assertTrue(builder.vertexCount() > 0, "Should have vertices");
        assertTrue(builder.edgeCount() > 0, "Should have edges");

        // Parse each line as JSON
        String[] lines = payload.split("\n");
        Set<String> vertexIds = new HashSet<>();
        boolean foundVertices = false;
        boolean edgesStarted = false;

        for (String line : lines) {
            if (line.isBlank()) continue;
            JsonNode node = MAPPER.readTree(line);
            assertTrue(node.has("@type"), "Each line must have @type");
            assertTrue(node.has("@class"), "Each line must have @class");

            String type = node.get("@type").asText();
            if ("vertex".equals(type)) {
                assertFalse(edgesStarted, "Vertex found after edges started — order violation");
                foundVertices = true;
                String id = node.has("@id") ? node.get("@id").asText() : null;
                if (id != null) {
                    assertTrue(vertexIds.add(id), "Duplicate @id: " + id);
                }
            } else if ("edge".equals(type)) {
                edgesStarted = true;
            } else if ("document".equals(type)) {
                // Documents go with vertices (before edges)
                assertFalse(edgesStarted, "Document found after edges started — order violation");
            }
        }

        assertTrue(foundVertices, "Should have at least one vertex");

        // Verify specific vertex classes are present
        Set<String> classes = new HashSet<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            JsonNode node = MAPPER.readTree(line);
            classes.add(node.get("@class").asText());
        }
        assertTrue(classes.contains("DaliSession"), "Missing DaliSession");
        assertTrue(classes.contains("DaliSchema"), "Missing DaliSchema");
        assertTrue(classes.contains("DaliTable"), "Missing DaliTable");
        assertTrue(classes.contains("DaliColumn"), "Missing DaliColumn");
        assertTrue(classes.contains("DaliStatement"), "Missing DaliStatement");
        // DaliSnippet is skipped in REMOTE_BATCH mode (batch endpoint rejects @type "document")
    }

    @Test
    void testEscaping_multilineSnippet() throws Exception {
        String snippet = "SELECT\n  a.id,\r\n  b.\"name\" AS \"full_name\"  -- comment\nFROM accounts a\nJOIN \"user_data\" b ON a.id = b.fk_id\nWHERE a.status = 'active'";

        JsonlBatchBuilder b = new JsonlBatchBuilder();
        b.appendVertex("DaliStatement", "STMT:1", Map.of(
                "session_id", "sid",
                "snippet", snippet
        ));

        String payload = b.build();
        String[] lines = payload.split("\n");

        // After escaping, the entire vertex should be on ONE line
        assertEquals(1, lines.length, "Multiline snippet must produce a single NDJSON line");

        // The line must parse as valid JSON
        JsonNode node = MAPPER.readTree(lines[0]);
        assertEquals("DaliStatement", node.get("@class").asText());

        // The snippet value, when parsed, should match the original
        String parsed = node.get("snippet").asText();
        assertEquals(snippet, parsed, "JSON-parsed snippet must equal original");
    }

    @Test
    void testVerticesBeforeEdges() {
        JsonlBatchBuilder b = new JsonlBatchBuilder();

        // Vertices must be added before any edge that references them (eager resolution)
        b.appendVertex("DaliTable", "T1", Map.of("name", "orders"));
        b.appendVertex("DaliColumn", "C1", Map.of("name", "id"));
        b.appendEdge("HAS_COLUMN", "T1", "C1", Map.of("session_id", "s1"));

        String payload = b.build();
        String[] lines = payload.split("\n");

        assertEquals(3, lines.length);

        // First two lines = vertices, third = edge
        assertTrue(lines[0].contains("\"@type\":\"vertex\""), "Line 0 should be vertex");
        assertTrue(lines[1].contains("\"@type\":\"vertex\""), "Line 1 should be vertex");
        assertTrue(lines[2].contains("\"@type\":\"edge\""), "Line 2 should be edge");
    }

    @Test
    void testEscapeJson_specialCharacters() {
        // Test all special characters
        assertEquals("hello\\nworld", JsonlBatchBuilder.escapeJson("hello\nworld"));
        assertEquals("tab\\there", JsonlBatchBuilder.escapeJson("tab\there"));
        assertEquals("quote\\\"here", JsonlBatchBuilder.escapeJson("quote\"here"));
        assertEquals("back\\\\slash", JsonlBatchBuilder.escapeJson("back\\slash"));
        assertEquals("cr\\rhere", JsonlBatchBuilder.escapeJson("cr\rhere"));
        assertEquals("\\u0000", JsonlBatchBuilder.escapeJson("\u0000"));
        assertEquals("\\u001f", JsonlBatchBuilder.escapeJson("\u001f"));
    }

    @Test
    void testNullValuesSkipped() throws Exception {
        JsonlBatchBuilder b = new JsonlBatchBuilder();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", "test");
        props.put("alias", null);
        props.put("order", 1);

        b.appendVertex("DaliColumn", "C1", props);
        String line = b.build().trim();

        JsonNode node = MAPPER.readTree(line);
        assertTrue(node.has("name"), "Non-null props must be present");
        assertFalse(node.has("alias"), "Null props must be skipped");
        assertTrue(node.has("order"), "Non-null props must be present");
    }

    @Test
    void testMd5_consistency() {
        // Same input must produce same output
        String a = JsonlBatchBuilder.md5("STMT:1:col_name");
        String b = JsonlBatchBuilder.md5("STMT:1:col_name");
        assertEquals(a, b);
        assertEquals(32, a.length(), "MD5 hex should be 32 chars");
    }

    @Test
    void testDocumentType_skippedInBatchMode() {
        JsonlBatchBuilder b = new JsonlBatchBuilder();
        b.appendDocument("DaliSnippet", Map.of("snippet", "SELECT 1", "session_id", "s1"));

        // ArcadeDB /api/v1/batch only accepts vertex and edge — documents are skipped
        assertTrue(b.build().trim().isEmpty(), "Documents should be skipped in REMOTE_BATCH mode");
    }

    @Test
    void testBooleanAndNumericValues() throws Exception {
        JsonlBatchBuilder b = new JsonlBatchBuilder();
        b.appendVertex("DaliColumn", "C1", Map.of(
                "is_output", true,
                "col_order", 42,
                "name", "test"
        ));

        String line = b.build().trim();
        JsonNode node = MAPPER.readTree(line);

        assertTrue(node.get("is_output").isBoolean());
        assertEquals(true, node.get("is_output").asBoolean());
        assertTrue(node.get("col_order").isNumber());
        assertEquals(42, node.get("col_order").asInt());
        assertTrue(node.get("name").isTextual());
    }

    /**
     * G6 / REMOTE_BATCH flush-order guard:
     * DaliRecord vertex MUST appear in the NDJSON payload BEFORE any
     * BULK_COLLECTS_INTO or RECORD_HAS_FIELD edge that references it.
     * ArcadeDB rejects dangling edges, so vertex-before-edge is mandatory.
     * D-1 (Sprint 1.3): RECORD_USED_IN removed; D-3: HAS_RECORD_FIELD → RECORD_HAS_FIELD.
     */
    @Test
    void g6_daliRecord_vertexBeforeEdges_inBatchPayload() throws Exception {
        Map<String, StatementInfo> statements = new LinkedHashMap<>();

        StatementInfo selectStmt = new StatementInfo(
                "SELECT:1", "SELECT", "SELECT order_id FROM orders_stg BULK COLLECT INTO l_tab",
                1, 1, null, "PROCEDURE:LOAD");
        selectStmt.setSubtype("BULK_COLLECT");
        statements.put("SELECT:1", selectStmt);

        StatementInfo insertStmt = new StatementInfo(
                "INSERT:2", "INSERT", "INSERT INTO fact_sales VALUES (l_tab(i).order_id)",
                3, 3, null, "PROCEDURE:LOAD");
        insertStmt.addAffectedColumn(
                "FACT_SALES.ORDER_ID", "ORDER_ID", "FACT_SALES", "L_TAB",
                "INSERT", "target", 1);
        statements.put("INSERT:2", insertStmt);

        RecordInfo rec = new RecordInfo("PROCEDURE:LOAD:RECORD:L_TAB", "L_TAB", "PROCEDURE:LOAD");
        rec.setSourceStatementGeoid("SELECT:1");
        rec.addField("ORDER_ID");
        Map<String, RecordInfo> records = new LinkedHashMap<>();
        records.put(rec.getGeoid(), rec);

        Structure str = new Structure(
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                statements, records);

        SemanticResult result = new SemanticResult(
                "test-sid", "/test.sql", "plsql", 0,
                str, List.of(), Map.of(), List.of(), Map.of(), List.of());

        JsonlBatchBuilder builder = JsonlBatchBuilder.buildFromResult("test-sid", result);
        String payload = builder.build();

        String[] lines = payload.split("\n");
        int daliRecordPos       = -1;
        int bulkCollectsIntoPos = -1;
        int recordHasFieldPos   = -1;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isBlank()) continue;
            JsonNode node = MAPPER.readTree(lines[i]);
            String cls  = node.has("@class") ? node.get("@class").asText() : "";
            String type = node.has("@type")  ? node.get("@type").asText()  : "";
            if ("DaliRecord".equals(cls) && "vertex".equals(type)) daliRecordPos = i;
            if ("BULK_COLLECTS_INTO".equals(cls) && "edge".equals(type)) bulkCollectsIntoPos = i;
            if ("RECORD_HAS_FIELD".equals(cls)   && "edge".equals(type)) recordHasFieldPos   = i;
        }

        assertTrue(daliRecordPos >= 0,       "DaliRecord vertex must be present in payload");
        assertTrue(bulkCollectsIntoPos >= 0, "BULK_COLLECTS_INTO edge must be present in payload");
        assertTrue(recordHasFieldPos >= 0,   "RECORD_HAS_FIELD edge must be present in payload");

        assertTrue(daliRecordPos < bulkCollectsIntoPos,
                "DaliRecord vertex (line " + daliRecordPos + ") must appear BEFORE " +
                "BULK_COLLECTS_INTO edge (line " + bulkCollectsIntoPos + ")");
        assertTrue(daliRecordPos < recordHasFieldPos,
                "DaliRecord vertex (line " + daliRecordPos + ") must appear BEFORE " +
                "RECORD_HAS_FIELD edge (line " + recordHasFieldPos + ")");
    }

    // ── HOUND-DB-001: DaliSchema must carry db_name resolved from the database map ──

    @Test
    void daliSchema_hasDbName_whenDatabaseLinked() throws Exception {
        // Schema "DWH.STAGING" belongs to database "DWH" with display name "DataWarehouse"
        Map<String, Object> databases = new LinkedHashMap<>();
        databases.put("DWH", Map.of("name", "DataWarehouse"));

        Map<String, Object> schemas = new LinkedHashMap<>();
        schemas.put("DWH.STAGING", Map.of("name", "STAGING", "db", "DWH"));

        Structure str = new Structure(databases, schemas, Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        SemanticResult result = new SemanticResult("sid1", "/f.sql", "plsql", 10,
                str, List.of(), Map.of(), List.of(), Map.of(), List.of());

        JsonlBatchBuilder b = JsonlBatchBuilder.buildFromResult("sid1", result);
        String payload = b.build();

        JsonNode schemaVertex = null;
        for (String line : payload.split("\n")) {
            if (line.isBlank()) continue;
            JsonNode n = MAPPER.readTree(line);
            if ("DaliSchema".equals(n.get("@class").asText())) { schemaVertex = n; break; }
        }
        assertNotNull(schemaVertex, "DaliSchema vertex must be present in batch");
        assertEquals("DataWarehouse", schemaVertex.get("db_name").asText(),
                "db_name must be resolved from the databases map, not null");
        assertEquals("DWH", schemaVertex.get("db_geoid").asText(),
                "db_geoid must equal the database geoid key");
    }

    @Test
    void hal3_01_compensationStats_edgesWrittenToBatch() throws Exception {
        Map<String, StatementInfo> statements = new LinkedHashMap<>();
        StatementInfo stmt = new StatementInfo(
                "STMT:1", "PLSQL_BLOCK", "v_name := emp_rec.name", 5, 5, null, "PROC:LOAD");
        statements.put("STMT:1", stmt);

        Map<String, RoutineInfo> routines = new LinkedHashMap<>();
        RoutineInfo routine = new RoutineInfo("PROC:LOAD", "LOAD", "PROCEDURE", null, null);
        routine.addTypedVariable("v_name", "VARCHAR2");
        routine.addTypedParameter("p_out", "NUMBER", "OUT");
        routines.put("PROC:LOAD", routine);

        List<CompensationStats> compStats = List.of(
                new CompensationStats("STMT:1", "ASSIGNS_TO_VARIABLE",
                        "PROC:LOAD:VAR:0", "VARIABLE", "test-sid"),
                new CompensationStats("STMT:1", "WRITES_TO_PARAMETER",
                        "PROC:LOAD:PARAM:0", "PARAMETER", "test-sid")
        );

        Structure str = new Structure(
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), routines,
                statements, Map.of(), Set.of(), Map.of(), Map.of(), compStats);

        SemanticResult result = new SemanticResult(
                "test-sid", "/test.sql", "plsql", 0,
                str, List.of(), Map.of(), List.of(), Map.of(), List.of());

        JsonlBatchBuilder builder = JsonlBatchBuilder.buildFromResult("test-sid", result);
        String payload = builder.build();

        int assignsCount = 0;
        int writesCount = 0;
        for (String line : payload.split("\n")) {
            if (line.isBlank()) continue;
            JsonNode node = MAPPER.readTree(line);
            String cls = node.has("@class") ? node.get("@class").asText() : "";
            if ("ASSIGNS_TO_VARIABLE".equals(cls)) {
                assignsCount++;
                assertEquals("STMT:1", node.get("@from").asText());
                assertEquals("PROC:LOAD:VAR:0", node.get("@to").asText());
                assertEquals("VARIABLE", node.get("target_kind").asText());
            }
            if ("WRITES_TO_PARAMETER".equals(cls)) {
                writesCount++;
                assertEquals("STMT:1", node.get("@from").asText());
                assertEquals("PROC:LOAD:PARAM:0", node.get("@to").asText());
                assertEquals("PARAMETER", node.get("target_kind").asText());
            }
        }
        assertEquals(1, assignsCount, "Exactly 1 ASSIGNS_TO_VARIABLE edge expected");
        assertEquals(1, writesCount, "Exactly 1 WRITES_TO_PARAMETER edge expected");
    }

    @Test
    void daliSchema_dbNameFallsBackToGeoid_whenDatabaseNotInMap() throws Exception {
        // Schema references a db geoid that is not in the databases map (edge case)
        Map<String, Object> schemas = new LinkedHashMap<>();
        schemas.put("UNKNOWN.SCH", Map.of("name", "SCH", "db", "UNKNOWN_DB"));

        Structure str = new Structure(Map.of(), schemas, Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        SemanticResult result = new SemanticResult("sid2", "/f.sql", "plsql", 10,
                str, List.of(), Map.of(), List.of(), Map.of(), List.of());

        JsonlBatchBuilder b = JsonlBatchBuilder.buildFromResult("sid2", result);
        String payload = b.build();

        JsonNode schemaVertex = null;
        for (String line : payload.split("\n")) {
            if (line.isBlank()) continue;
            JsonNode n = MAPPER.readTree(line);
            if ("DaliSchema".equals(n.get("@class").asText())) { schemaVertex = n; break; }
        }
        assertNotNull(schemaVertex, "DaliSchema vertex must be present");
        // Falls back to the geoid itself — not null
        assertEquals("UNKNOWN_DB", schemaVertex.get("db_name").asText(),
                "db_name must fall back to db_geoid when database map lookup fails");
    }
}
