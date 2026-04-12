package studio.seer.lineage.service;

import org.junit.jupiter.api.Test;
import studio.seer.lineage.model.ExploreResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExploreService static helpers.
 *
 * No Quarkus context or DB connection needed — all methods under test
 * are pure static functions operating on plain Java collections.
 */
class ExploreServiceTest {

    // ── ScopeRef.parse ────────────────────────────────────────────────────────

    @Test
    void scopeRef_null_returnsRidWithEmptyName() {
        var ref = ExploreService.ScopeRef.parse(null);
        assertEquals("rid",  ref.type());
        assertEquals("",     ref.name());
        assertNull(ref.dbName());
    }

    @Test
    void scopeRef_blank_returnsRidWithEmptyName() {
        var ref = ExploreService.ScopeRef.parse("   ");
        assertEquals("rid", ref.type());
        assertEquals("",    ref.name());
    }

    @Test
    void scopeRef_noDash_treatedAsRidScope() {
        var ref = ExploreService.ScopeRef.parse("#10:42");
        assertEquals("rid",   ref.type());
        assertEquals("#10:42", ref.name());
        assertNull(ref.dbName());
    }

    @Test
    void scopeRef_schemaOnly_noDb() {
        var ref = ExploreService.ScopeRef.parse("schema-DWH");
        assertEquals("schema", ref.type());
        assertEquals("DWH",    ref.name());
        assertNull(ref.dbName());
    }

    @Test
    void scopeRef_schemaWithDb_parsedCorrectly() {
        var ref = ExploreService.ScopeRef.parse("schema-HR|MYDB");
        assertEquals("schema", ref.type());
        assertEquals("HR",     ref.name());
        assertEquals("MYDB",   ref.dbName());
    }

    @Test
    void scopeRef_pkg_parsedCorrectly() {
        var ref = ExploreService.ScopeRef.parse("pkg-PKG_ETL_CRM");
        assertEquals("pkg",         ref.type());
        assertEquals("PKG_ETL_CRM", ref.name());
        assertNull(ref.dbName());
    }

    @Test
    void scopeRef_db_parsedCorrectly() {
        var ref = ExploreService.ScopeRef.parse("db-ORACLE_PROD");
        assertEquals("db",          ref.type());
        assertEquals("ORACLE_PROD", ref.name());
        assertNull(ref.dbName());
    }

    @Test
    void scopeRef_pipeWithoutDbPrefix_stillParsed() {
        // Any type with pipe suffix gets dbName extracted
        var ref = ExploreService.ScopeRef.parse("schema-ODS|HoundDB");
        assertEquals("schema",  ref.type());
        assertEquals("ODS",     ref.name());
        assertEquals("HoundDB", ref.dbName());
    }

    // ── buildResult ───────────────────────────────────────────────────────────

    @Test
    void buildResult_emptyRows_returnsEmptyResult() {
        ExploreResult r = ExploreService.buildResult(List.of(), "ROOT", "DaliSchema");
        assertTrue(r.nodes().isEmpty());
        assertTrue(r.edges().isEmpty());
    }

    @Test
    void buildResult_singleRow_producesTwoNodesOneEdge() {
        var row = Map.<String, Object>of(
            "srcId",    "#1:0",
            "srcLabel", "MY_SCHEMA",
            "srcType",  "DaliSchema",
            "tgtId",    "#2:0",
            "tgtLabel", "MY_TABLE",
            "tgtScope", "MY_SCHEMA",
            "tgtType",  "DaliTable",
            "edgeType", "CONTAINS_TABLE"
        );

        ExploreResult r = ExploreService.buildResult(List.of(row), "MY_SCHEMA", "DaliSchema");

        assertEquals(2, r.nodes().size());
        assertEquals(1, r.edges().size());
        assertEquals("#1:0__CONTAINS_TABLE__#2:0", r.edges().get(0).id());
    }

    @Test
    void buildResult_duplicateEdge_deduplicated() {
        var row = Map.<String, Object>of(
            "srcId", "#1:0", "srcLabel", "S", "srcType", "DaliSchema",
            "tgtId", "#2:0", "tgtLabel", "T", "tgtScope", "", "tgtType", "DaliTable",
            "edgeType", "CONTAINS_TABLE"
        );

        ExploreResult r = ExploreService.buildResult(List.of(row, row), "S", "DaliSchema");

        assertEquals(2, r.nodes().size());
        assertEquals(1, r.edges().size(), "Duplicate edge must be deduplicated");
    }

    @Test
    void buildResult_tgtScopePreservedOnTargetNode() {
        var row = Map.<String, Object>of(
            "srcId", "#1:0", "srcLabel", "PKG", "srcType", "DaliPackage",
            "tgtId", "#3:0", "tgtLabel", "PROC", "tgtScope", "MY_SCHEMA",
            "tgtType", "DaliRoutine", "edgeType", "CONTAINS_ROUTINE"
        );

        ExploreResult r = ExploreService.buildResult(List.of(row), "PKG", "DaliPackage");

        var target = r.nodes().stream()
            .filter(n -> n.id().equals("#3:0"))
            .findFirst().orElseThrow();
        assertEquals("MY_SCHEMA", target.scope());
    }

    @Test
    void buildResult_srcTypeFallsBackToRootType_whenColumnMissing() {
        // Row without "srcType" key → should fall back to the rootType argument
        var row = Map.<String, Object>of(
            "srcId", "#1:0", "srcLabel", "PKG",
            "tgtId", "#2:0", "tgtLabel", "R", "tgtScope", "", "tgtType", "DaliRoutine",
            "edgeType", "CONTAINS_ROUTINE"
        );

        ExploreResult r = ExploreService.buildResult(List.of(row), "PKG", "DaliPackage");

        var src = r.nodes().stream()
            .filter(n -> n.id().equals("#1:0"))
            .findFirst().orElseThrow();
        assertEquals("DaliPackage", src.type(), "srcType should fall back to rootType");
    }

    @Test
    void buildResult_multipleSourceTypes_eachPreserved() {
        var row1 = Map.<String, Object>of(
            "srcId", "#1:0", "srcLabel", "SCHEMA", "srcType", "DaliSchema",
            "tgtId", "#2:0", "tgtLabel", "T1", "tgtScope", "S", "tgtType", "DaliTable",
            "edgeType", "CONTAINS_TABLE"
        );
        var row2 = Map.<String, Object>of(
            "srcId", "#3:0", "srcLabel", "STMT", "srcType", "DaliStatement",
            "tgtId", "#2:0", "tgtLabel", "T1", "tgtScope", "S", "tgtType", "DaliTable",
            "edgeType", "WRITES_TO"
        );

        ExploreResult r = ExploreService.buildResult(List.of(row1, row2), "SCHEMA", "DaliSchema");

        assertEquals(3, r.nodes().size());
        assertEquals(2, r.edges().size());
        var stmt = r.nodes().stream()
            .filter(n -> n.id().equals("#3:0"))
            .findFirst().orElseThrow();
        assertEquals("DaliStatement", stmt.type());
    }

    // ── toExploreResult ───────────────────────────────────────────────────────

    @Test
    void toExploreResult_emptyList_returnsEmpty() {
        ExploreResult r = ExploreService.toExploreResult(List.of());
        assertTrue(r.nodes().isEmpty());
        assertTrue(r.edges().isEmpty());
    }

    @Test
    void toExploreResult_singleCompleteRow_producesTwoNodesOneEdge() {
        var n = Map.<String, Object>of("@rid", "#1:0", "@type", "DaliTable",    "table_name", "ORDERS");
        var m = Map.<String, Object>of("@rid", "#2:0", "@type", "DaliColumn",   "column_name", "ORDER_ID");
        var r = Map.<String, Object>of("@rid", "#5:0", "@type", "HAS_COLUMN");

        ExploreResult result = ExploreService.toExploreResult(
            List.of(Map.of("n", n, "r", r, "m", m)));

        assertEquals(2, result.nodes().size());
        assertEquals(1, result.edges().size());
        assertEquals("#5:0", result.edges().get(0).id());
        assertEquals("ORDERS",   result.nodes().get(0).label());
        assertEquals("ORDER_ID", result.nodes().get(1).label());
    }

    @Test
    void toExploreResult_rowWithNullField_skipped() {
        // Row where 'r' is null — must be silently skipped
        var incomplete = new java.util.HashMap<String, Object>();
        incomplete.put("n", Map.of("@rid", "#1:0", "@type", "DaliTable"));
        incomplete.put("r", null);
        incomplete.put("m", Map.of("@rid", "#2:0", "@type", "DaliColumn"));

        ExploreResult result = ExploreService.toExploreResult(List.of(incomplete));

        assertTrue(result.nodes().isEmpty(),  "Incomplete row should be skipped");
        assertTrue(result.edges().isEmpty(),  "Incomplete row should be skipped");
    }
}
