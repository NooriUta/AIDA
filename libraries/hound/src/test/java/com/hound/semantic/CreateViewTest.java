package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.*;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * STAB-8 verification: CREATE VIEW registration as DaliTable with type VIEW.
 *
 * DoD:
 *  1. DaliTable(table_type='VIEW') — view registered in the tables map
 *  2. geoid contains schema prefix when schema is present: HR.MY_VIEW
 *  3. HAS_COLUMN edges — columns from inner SELECT registered on the view
 */
class CreateViewTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    // ═══════ Test 1: simple CREATE VIEW — registered as VIEW type ═══════

    @Test
    void createView_registeredAsView() {
        String sql = "CREATE OR REPLACE VIEW MY_VIEW AS SELECT id, name FROM employees";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        boolean hasView = tables.values().stream()
                .anyMatch(t -> "VIEW".equals(t.tableType()));
        assertTrue(hasView, "Should have at least one VIEW table. Got: " +
                tables.values().stream().map(t -> t.geoid() + ":" + t.tableType()).toList());
    }

    // ═══════ Test 2: CREATE VIEW with schema prefix ═══════

    @Test
    void createView_withSchema_geoidHasSchemaPrefix() {
        String sql = "CREATE OR REPLACE VIEW HR.EMP_VIEW AS SELECT employee_id, last_name FROM hr.employees";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        boolean hasHrView = tables.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("HR") && k.toUpperCase().contains("EMP_VIEW"));
        assertTrue(hasHrView, "VIEW geoid should contain schema prefix HR. Got: " + tables.keySet());

        // Verify table_type is VIEW (not TABLE)
        tables.forEach((geoid, t) -> {
            if (geoid.toUpperCase().contains("EMP_VIEW")) {
                assertEquals("VIEW", t.tableType(),
                        "table_type should be VIEW, got: " + t.tableType());
            }
        });
    }

    // ═══════ Test 3: CREATE VIEW — no columns registered as TABLE ═══════

    @Test
    void createView_notRegisteredAsTable() {
        String sql = "CREATE VIEW MY_VIEW AS SELECT id FROM source_tbl";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        // MY_VIEW should exist with type VIEW, not TABLE
        tables.forEach((geoid, t) -> {
            if (geoid.toUpperCase().contains("MY_VIEW")) {
                assertNotEquals("TABLE", t.tableType(),
                        "MY_VIEW must not have type TABLE");
                assertEquals("VIEW", t.tableType(),
                        "MY_VIEW must have type VIEW");
            }
        });
    }

    // ═══════ Test 4: CREATE VIEW — output columns become view columns ═══════

    @Test
    void createView_innerSelectColumnsRegisteredOnView() {
        String sql = """
            CREATE OR REPLACE VIEW DEPT_SUMMARY AS
            SELECT department_id, COUNT(*) AS cnt, MAX(salary) AS max_sal
            FROM employees
            GROUP BY department_id
            """;
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();
        var columns = engine.getBuilder().getColumns();

        // Find the view
        String viewGeoid = tables.keySet().stream()
                .filter(k -> k.toUpperCase().contains("DEPT_SUMMARY"))
                .findFirst()
                .orElse(null);
        assertNotNull(viewGeoid, "DEPT_SUMMARY view should be registered. Got tables: " + tables.keySet());
        assertEquals("VIEW", tables.get(viewGeoid).tableType());

        // Output columns of the inner SELECT should be registered as ColumnInfo on the view
        boolean hasDeptId = columns.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("DEPT_SUMMARY") &&
                               k.toUpperCase().contains("DEPARTMENT_ID"));
        assertTrue(hasDeptId,
                "Column DEPARTMENT_ID should be registered on view DEPT_SUMMARY. Got columns: " +
                columns.keySet().stream().filter(k -> k.toUpperCase().contains("DEPT_SUMMARY")).toList());
    }

    // ═══════ Test 5: CREATE VIEW — multiple views in one script ═══════

    @Test
    void createView_multipleViews() {
        String sql = """
            CREATE VIEW VIEW_A AS SELECT a FROM t1;
            CREATE VIEW VIEW_B AS SELECT b FROM t2;
            """;
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        long viewCount = tables.values().stream()
                .filter(t -> "VIEW".equals(t.tableType()))
                .count();
        assertTrue(viewCount >= 2,
                "Should have at least 2 VIEWs. Got view count=" + viewCount +
                " tables=" + tables.values().stream().map(t -> t.geoid() + ":" + t.tableType()).toList());
    }

    // ═══════ Test 6: CREATE VIEW — base tables registered as TABLE (not VIEW) ═══════

    @Test
    void createView_baseTableNotConfusedWithView() {
        String sql = "CREATE VIEW MY_VIEW AS SELECT id, name FROM base_table";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        // base_table must be TABLE, MY_VIEW must be VIEW
        tables.forEach((geoid, t) -> {
            if (geoid.toUpperCase().contains("BASE_TABLE")) {
                assertEquals("TABLE", t.tableType(),
                        "base_table should be TABLE, not VIEW");
            }
            if (geoid.toUpperCase().contains("MY_VIEW")) {
                assertEquals("VIEW", t.tableType(),
                        "MY_VIEW should be VIEW, not TABLE");
            }
        });
    }
}
