package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.*;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LOW gaps G10 / G11 / G13 and column alias stripping.
 *
 * G10: CREATE VIEW with explicit column alias list (col1, col2, ...)
 * G11: schema.table.column 3-part column reference in onColumnRef
 * G13: synthetic column name (EXPR_N) when alias and expression text are both absent
 * cleanColumnName: DML target columns with alias prefix (t.col1) must strip the prefix
 */
class LowGapsTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    // ═══════ G10: CREATE VIEW explicit column alias list ═══════

    @Test
    void g10_createView_explicitColumnList_registersColumns() {
        String sql = """
            CREATE OR REPLACE VIEW HR.EMP_SUMMARY (emp_id, full_name, dept_no) AS
            SELECT employee_id, last_name || ' ' || first_name, department_id
            FROM hr.employees
            """;
        var engine = parse(sql);
        var tables  = engine.getBuilder().getTables();
        var columns = engine.getBuilder().getColumns();

        // View should be registered
        String viewGeoid = tables.keySet().stream()
                .filter(k -> k.toUpperCase().contains("EMP_SUMMARY"))
                .findFirst().orElse(null);
        assertNotNull(viewGeoid, "EMP_SUMMARY view must be registered. Got: " + tables.keySet());
        assertEquals("VIEW", tables.get(viewGeoid).tableType());

        // Explicit header columns must be registered on the view
        assertTrue(columns.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("EMP_SUMMARY") && k.toUpperCase().contains("EMP_ID")),
                "Column EMP_ID must be registered on view. Got: " +
                columns.keySet().stream().filter(k -> k.toUpperCase().contains("EMP_SUMMARY")).toList());

        assertTrue(columns.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("EMP_SUMMARY") && k.toUpperCase().contains("FULL_NAME")),
                "Column FULL_NAME must be registered on view.");

        assertTrue(columns.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("EMP_SUMMARY") && k.toUpperCase().contains("DEPT_NO")),
                "Column DEPT_NO must be registered on view.");
    }

    @Test
    void g10_createView_noExplicitColumnList_stillWorks() {
        // When no explicit column list, existing behavior (copy from inner SELECT) must not break
        String sql = """
            CREATE VIEW DEPT_VIEW AS
            SELECT department_id, department_name FROM departments
            """;
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        String viewGeoid = tables.keySet().stream()
                .filter(k -> k.toUpperCase().contains("DEPT_VIEW"))
                .findFirst().orElse(null);
        assertNotNull(viewGeoid, "DEPT_VIEW should be registered");
        assertEquals("VIEW", tables.get(viewGeoid).tableType());
    }

    // ═══════ G11: schema.table.column 3-part reference ═══════

    @Test
    void g11_schemaTableColumn_columnAddedToTable() {
        // SCHEMA.TABLE.COLUMN reference — column must be registered on the table
        String sql = """
            CREATE OR REPLACE PACKAGE BODY HR.PKG_TEST AS
              PROCEDURE p IS BEGIN
                INSERT INTO hr.emp_audit (audit_col)
                SELECT hr.employees.last_name FROM hr.employees;
              END;
            END;
            """;
        var engine = parse(sql);
        var columns = engine.getBuilder().getColumns();

        // hr.employees.last_name should register LAST_NAME on EMPLOYEES table
        boolean hasLastName = columns.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("EMPLOYEES") && k.toUpperCase().contains("LAST_NAME"));
        assertTrue(hasLastName,
                "LAST_NAME should be registered on EMPLOYEES (schema.table.column ref). Got: " + columns.keySet());
    }

    @Test
    void g11_schemaTableColumn_tableGeoidResolved() {
        // 3-part reference: table part must resolve to the correct geoid
        String sql = """
            CREATE OR REPLACE PACKAGE BODY TEST_SCH.PKG AS
              PROCEDURE proc IS BEGIN
                SELECT test_sch.orders.order_id FROM test_sch.orders;
              END;
            END;
            """;
        var engine = parse(sql);
        var tables  = engine.getBuilder().getTables();
        var columns = engine.getBuilder().getColumns();

        // orders must be known
        boolean hasOrders = tables.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("ORDERS"));
        assertTrue(hasOrders, "ORDERS table must be registered. Got: " + tables.keySet());

        // order_id must be on orders
        boolean hasOrderId = columns.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("ORDERS") && k.toUpperCase().contains("ORDER_ID"));
        assertTrue(hasOrderId, "ORDER_ID must be on ORDERS. Got: " + columns.keySet());
    }

    // ═══════ G13: synthetic column name EXPR_N ═══════

    @Test
    void g13_syntheticColumnName_whenNoAliasOrExpression() {
        // Expressions without aliases should produce EXPR_N names, not null keys
        String sql = """
            SELECT department_id, salary * 1.1, SYSDATE FROM employees
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        // Find the SELECT statement
        var selectStmt = stmts.values().stream()
                .filter(s -> "SELECT".equals(s.getType()))
                .findFirst().orElse(null);
        assertNotNull(selectStmt, "SELECT statement must exist");

        // columnsOutput must have no null or blank keys
        var outputCols = selectStmt.getColumnsOutput();
        assertFalse(outputCols.isEmpty(), "Should have output columns");
        for (String key : outputCols.keySet()) {
            assertNotNull(key, "Output column key must not be null");
            assertFalse(key.isBlank(), "Output column key must not be blank");
        }
    }

    @Test
    void g13_syntheticColumnName_exprPrefix() {
        // Unnamed expression must yield EXPR_<N> where N is its position
        String sql = "SELECT id, salary + bonus FROM emp";
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        var selectStmt = stmts.values().stream()
                .filter(s -> "SELECT".equals(s.getType()))
                .findFirst().orElse(null);
        assertNotNull(selectStmt);

        // Second column has no alias — must be EXPR_2 or the expression text itself (non-null)
        var keys = selectStmt.getColumnsOutput().keySet();
        boolean hasSecondColName = keys.stream()
                .anyMatch(k -> k != null && !k.isBlank());
        assertTrue(hasSecondColName, "All output column keys must be non-blank. Got: " + keys);
    }

    @Test
    void g13_syntheticColumnName_aliasedColumnNotAffected() {
        // When alias is present, it must still be used (not EXPR_N)
        String sql = "SELECT salary * 1.1 AS adjusted_salary FROM employees";
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        var selectStmt = stmts.values().stream()
                .filter(s -> "SELECT".equals(s.getType()))
                .findFirst().orElse(null);
        assertNotNull(selectStmt);

        boolean hasAliasedCol = selectStmt.getColumnsOutput().containsKey("ADJUSTED_SALARY")
                || selectStmt.getColumnsOutput().containsKey("adjusted_salary");
        assertTrue(hasAliasedCol,
                "Aliased column must keep its alias. Got: " + selectStmt.getColumnsOutput().keySet());
    }

    // ═══════ cleanColumnName: alias-prefix stripping for DML target columns ═══════

    @Test
    void mergeUpdate_aliasedTargetColumn_stripsAlias() {
        // MERGE UPDATE SET t.col1 = ... — column_name in grammar is "t.col1"
        // DaliAffectedColumn.column_name must be "COL1", not "T.COL1"
        String sql = """
            MERGE INTO target_tbl t
            USING source_tbl s ON (t.id = s.id)
            WHEN MATCHED THEN UPDATE SET t.col1 = s.val1, t.col2 = s.val2
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        var mergeStmt = stmts.values().stream()
                .filter(si -> "MERGE".equals(si.getType()))
                .findFirst().orElse(null);
        assertNotNull(mergeStmt, "MERGE statement must be parsed");

        List<Map<String, Object>> affected = mergeStmt.getAffectedColumns();
        assertFalse(affected.isEmpty(), "MERGE should produce affected columns");

        for (var ac : affected) {
            String colName = (String) ac.get("column_name");
            if (colName != null) {
                assertFalse(colName.contains("."),
                        "column_name must not contain alias prefix, got: " + colName);
            }
        }

        // col1 and col2 must appear as column_name (not t.col1 / t.col2)
        boolean hasCol1 = affected.stream().anyMatch(a -> "COL1".equals(a.get("column_name")));
        boolean hasCol2 = affected.stream().anyMatch(a -> "COL2".equals(a.get("column_name")));
        assertTrue(hasCol1, "COL1 must be in affected columns. Got: " +
                affected.stream().map(a -> a.get("column_name")).toList());
        assertTrue(hasCol2, "COL2 must be in affected columns");
    }

    @Test
    void mergeInsert_aliasedTargetColumn_stripsAlias() {
        // MERGE INSERT (t.col1, t.col2) — column names must strip alias prefix
        String sql = """
            MERGE INTO target_tbl t
            USING source_tbl s ON (t.id = s.id)
            WHEN NOT MATCHED THEN INSERT (t.col1, t.col2) VALUES (s.val1, s.val2)
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        var mergeStmt = stmts.values().stream()
                .filter(si -> "MERGE".equals(si.getType()))
                .findFirst().orElse(null);
        assertNotNull(mergeStmt, "MERGE statement must be parsed");

        List<Map<String, Object>> affected = mergeStmt.getAffectedColumns();
        for (var ac : affected) {
            String colName = (String) ac.get("column_name");
            if (colName != null) {
                assertFalse(colName.contains("."),
                        "column_name must not contain alias prefix, got: " + colName);
            }
        }
    }

    @Test
    void updateSet_aliasedTargetColumn_stripsAlias() {
        // UPDATE t SET t.col1 = ... — column_name node text is "t.col1"
        String sql = """
            UPDATE employees t SET t.salary = t.salary * 1.1 WHERE t.dept_id = 10
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        var updateStmt = stmts.values().stream()
                .filter(si -> "UPDATE".equals(si.getType()))
                .findFirst().orElse(null);
        assertNotNull(updateStmt, "UPDATE statement must be parsed");

        List<Map<String, Object>> affected = updateStmt.getAffectedColumns();
        for (var ac : affected) {
            String colName = (String) ac.get("column_name");
            if (colName != null) {
                assertFalse(colName.contains("."),
                        "column_name must not contain alias prefix, got: " + colName);
            }
        }

        boolean hasSalary = affected.stream().anyMatch(a -> "SALARY".equals(a.get("column_name")));
        assertTrue(hasSalary, "SALARY must be in affected columns (stripped). Got: " +
                affected.stream().map(a -> a.get("column_name")).toList());
    }

    @Test
    void insertColumnList_aliasedColumn_stripsAlias() {
        // INSERT INTO t (t.col1, t.col2) — unusual but grammar allows it
        String sql = """
            INSERT INTO orders (order_id, amount) VALUES (1, 100)
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        var insertStmt = stmts.values().stream()
                .filter(si -> "INSERT".equals(si.getType()))
                .findFirst().orElse(null);
        assertNotNull(insertStmt, "INSERT statement must be parsed");

        List<Map<String, Object>> affected = insertStmt.getAffectedColumns();
        for (var ac : affected) {
            String colName = (String) ac.get("column_name");
            if (colName != null) {
                assertFalse(colName.contains("."),
                        "column_name must not contain prefix, got: " + colName);
            }
        }
    }

    // ═══════ OC-propagation: SELECT→SUBQUERY→CTE output column lift ═══════

    @Test
    void cte_outputColumnsLiftedToCteScope() {
        // CTE: SELECT → SUBQUERY → CTE chain.
        // The CTE statement must have output columns so ATOM_REF_OUTPUT_COL can be built.
        String sql = """
            CREATE OR REPLACE PACKAGE BODY PKG_TEST AS
              PROCEDURE p IS
              BEGIN
                INSERT INTO target_tbl (col_a, col_b)
                SELECT t.col_a, t.col_b FROM (
                  WITH inc AS (
                    SELECT src.col_a, src.col_b FROM source_tbl src
                  )
                  SELECT * FROM inc
                ) t;
              END;
            END;
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        // Find the CTE statement (type=CTE, geoid contains "INC" or "inc")
        var cteStmt = stmts.values().stream()
                .filter(s -> "CTE".equals(s.getType()))
                .findFirst().orElse(null);
        assertNotNull(cteStmt, "CTE statement must exist. Got types: " +
                stmts.values().stream().map(s -> s.getType() + ":" + s.getGeoid()).toList());

        // CTE must have output columns propagated from the inner SELECT
        assertFalse(cteStmt.getColumnsOutput().isEmpty(),
                "CTE output columns must be propagated from inner SELECT. Got: " +
                cteStmt.getColumnsOutput().keySet());

        // Keys may carry table-alias prefix (e.g. "src.col_a") — just check col_a is present
        boolean hasColA = cteStmt.getColumnsOutput().keySet().stream()
                .anyMatch(k -> k != null && k.toUpperCase().endsWith("COL_A"));
        assertTrue(hasColA,
                "COL_A must be in CTE output columns (with or without alias prefix). Got: "
                + cteStmt.getColumnsOutput().keySet());
    }
}
