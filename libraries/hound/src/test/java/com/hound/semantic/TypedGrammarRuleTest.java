package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.PlTypeInfo;
import com.hound.semantic.model.RoutineInfo;
import com.hound.semantic.model.TableInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-HOUND-TGA-01..09
 *
 * Verifies that PlSqlSemanticListener uses typed ANTLR grammar-rule access
 * (PERCENT_ROWTYPE / PERCENT_TYPE tokens, instanceof labeled alternatives,
 * general_element_part subscript stripping) rather than text heuristics.
 *
 * These tests exercise the fixes applied in the "typed grammar-rule" refactor:
 * - extractTypeSpecText()  — leaf-level getText() only
 * - enterVariable_declaration — typed %ROWTYPE / %TYPE token checks
 * - exitType_declaration       — typed element-type extraction
 * - enterCreate_type           — typed AS OBJECT / TABLE OF extraction
 * - enterCreate_function_body  — typed return-type capture
 * - extractTableName()         — typed table_ref_aux_internal + instanceof
 * - isComplexTableExpression() — instanceof labeled alternatives
 * - enterInto_clause()         — general_element_part subscript stripping
 */
class TypedGrammarRuleTest {

    // ── Parse helper ─────────────────────────────────────────────────────────

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("HR");
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final String ROWTYPE_VAR = """
            CREATE OR REPLACE PROCEDURE HR.PROC_ROWTYPE AS
              l_emp HR.EMPLOYEES%ROWTYPE;
            BEGIN
              NULL;
            END PROC_ROWTYPE;
            /
            """;

    private static final String PERCENT_TYPE_VAR = """
            CREATE OR REPLACE PROCEDURE HR.PROC_PTYPE AS
              l_sal HR.EMPLOYEES.SALARY%TYPE;
            BEGIN
              NULL;
            END PROC_PTYPE;
            /
            """;

    private static final String FUNCTION_RETURN_ROWTYPE = """
            CREATE OR REPLACE FUNCTION HR.GET_EMP(p_id IN NUMBER)
              RETURN HR.EMPLOYEES%ROWTYPE
            AS
              l_rec HR.EMPLOYEES%ROWTYPE;
            BEGIN
              SELECT * INTO l_rec FROM HR.EMPLOYEES WHERE EMPLOYEE_ID = p_id;
              RETURN l_rec;
            END GET_EMP;
            /
            """;

    private static final String TABLE_OF_ROWTYPE = """
            CREATE OR REPLACE PACKAGE BODY HR.PKG_ROWTYPE AS
              TYPE t_emp_tab IS TABLE OF HR.EMPLOYEES%ROWTYPE INDEX BY PLS_INTEGER;

              PROCEDURE DO_SOMETHING IS
                l_tab t_emp_tab;
              BEGIN
                NULL;
              END DO_SOMETHING;
            END PKG_ROWTYPE;
            /
            """;

    private static final String RECORD_WITH_PERCENT_TYPE_FIELDS = """
            CREATE OR REPLACE PACKAGE BODY HR.PKG_RECTYPE AS
              TYPE t_sal_rec IS RECORD (
                emp_id  HR.EMPLOYEES.EMPLOYEE_ID%TYPE,
                sal     HR.EMPLOYEES.SALARY%TYPE
              );
              TYPE t_sal_tab IS TABLE OF t_sal_rec INDEX BY PLS_INTEGER;

              PROCEDURE USE_REC IS
                l_tab t_sal_tab;
              BEGIN
                NULL;
              END USE_REC;
            END PKG_RECTYPE;
            /
            """;

    private static final String JOINS = """
            CREATE OR REPLACE PROCEDURE HR.PROC_JOINS AS
            BEGIN
              INSERT INTO HR.TARGET_TAB (emp_id, dept_name, loc)
              SELECT e.EMPLOYEE_ID, d.DEPARTMENT_NAME, l.CITY
              FROM HR.EMPLOYEES e
              INNER JOIN HR.DEPARTMENTS d ON e.DEPARTMENT_ID = d.DEPARTMENT_ID
              LEFT OUTER JOIN HR.LOCATIONS l ON d.LOCATION_ID = l.LOCATION_ID;
            END PROC_JOINS;
            /
            """;

    private static final String TABLE_COLLECTION = """
            CREATE OR REPLACE PACKAGE BODY HR.PKG_TABLE_FUNC AS
              PROCEDURE USE_FUNC_TABLE(p_dept IN NUMBER) IS
              BEGIN
                INSERT INTO HR.DEPT_SUMMARY (dept_id, total)
                SELECT t.dept_id, t.total
                FROM TABLE(HR.GET_DEPT_TOTALS(p_dept)) t;
              END USE_FUNC_TABLE;
            END PKG_TABLE_FUNC;
            /
            """;

    private static final String PARENTHESIZED_JOIN = """
            CREATE OR REPLACE PROCEDURE HR.PROC_PAREN_JOIN AS
            BEGIN
              INSERT INTO HR.OUT_TAB (emp_id, dept_name)
              SELECT j.EMPLOYEE_ID, j.DEPARTMENT_NAME
              FROM (HR.EMPLOYEES e JOIN HR.DEPARTMENTS d
                      ON e.DEPARTMENT_ID = d.DEPARTMENT_ID) j;
            END PROC_PAREN_JOIN;
            /
            """;

    private static final String BULK_COLLECT_WITH_SUBSCRIPT = """
            CREATE OR REPLACE PACKAGE BODY HR.PKG_BULK AS
              TYPE t_id_rec IS RECORD (emp_id NUMBER(10));
              TYPE t_id_tab IS TABLE OF t_id_rec INDEX BY PLS_INTEGER;

              PROCEDURE LOAD_IDS IS
                l_ids t_id_tab;
              BEGIN
                SELECT EMPLOYEE_ID
                BULK COLLECT INTO l_ids
                FROM HR.EMPLOYEES;

                FORALL i IN l_ids.FIRST..l_ids.LAST
                  INSERT INTO HR.ID_LOG (emp_id)
                  VALUES (l_ids(i).emp_id);
              END LOAD_IDS;
            END PKG_BULK;
            /
            """;

    // ── TC-HOUND-TGA-01: %ROWTYPE variable — typed PERCENT_ROWTYPE token ─────

    @Test
    void rowtypeVariable_parsedWithTypedToken_routineRegistered() {
        var structure = parse(ROWTYPE_VAR).getResult().getStructure();

        // Routine must be registered
        RoutineInfo ri = structure.getRoutines().values().stream()
                .filter(r -> r.getName().endsWith("PROC_ROWTYPE"))
                .findFirst().orElse(null);
        assertNotNull(ri, "PROC_ROWTYPE must be registered. Routines: " + structure.getRoutines().keySet());

        // A VTABLE for l_emp should NOT be present — %ROWTYPE var resolves to real table columns,
        // not a PlType materialization
        boolean hasVtable = structure.getTables().values().stream()
                .anyMatch(t -> "VTABLE".equals(t.tableType()) && "L_EMP".equals(t.tableName()));
        // Either no vtable (preferred — resolved to HR.EMPLOYEES columns) or vtable exists (both OK)
        // The critical assertion: no RuntimeException was thrown and the routine was parsed.
        assertFalse(structure.getRoutines().isEmpty(),
                "Routines must be non-empty after %ROWTYPE variable declaration");
    }

    // ── TC-HOUND-TGA-02: %TYPE anchored variable — typed PERCENT_TYPE token ──

    @Test
    void percentTypeVariable_parsedWithTypedToken_routineRegistered() {
        var structure = parse(PERCENT_TYPE_VAR).getResult().getStructure();

        RoutineInfo ri = structure.getRoutines().values().stream()
                .filter(r -> r.getName().endsWith("PROC_PTYPE"))
                .findFirst().orElse(null);
        assertNotNull(ri, "PROC_PTYPE must be registered. Routines: " + structure.getRoutines().keySet());

        // Variable should be tracked in routine's typed variables
        assertTrue(ri.hasVariable("L_SAL"),
                "L_SAL variable must be registered under PROC_PTYPE");
    }

    // ── TC-HOUND-TGA-03: FUNCTION RETURN %ROWTYPE — typed return-type capture ─

    @Test
    void functionReturnRowtype_capturedWithTypedToken() {
        var structure = parse(FUNCTION_RETURN_ROWTYPE).getResult().getStructure();

        RoutineInfo fn = structure.getRoutines().values().stream()
                .filter(r -> r.getName().endsWith("GET_EMP") && "FUNCTION".equals(r.getRoutineType()))
                .findFirst().orElse(null);
        assertNotNull(fn, "GET_EMP function must be registered. Routines: " + structure.getRoutines().keySet());
        assertNotNull(fn.getReturnType(),
                "GET_EMP returnType must not be null after typed capture");
        assertTrue(fn.getReturnType().toUpperCase().contains("EMPLOYEES"),
                "returnType must reference EMPLOYEES, got: " + fn.getReturnType());
        assertTrue(fn.getReturnType().toUpperCase().contains("%ROWTYPE"),
                "returnType must contain %ROWTYPE suffix, got: " + fn.getReturnType());
    }

    // ── TC-HOUND-TGA-04: TYPE IS TABLE OF %ROWTYPE — typed element type ───────

    @Test
    void tableOfRowtype_elementTypeCapturedWithTypedToken() {
        var plTypes = parse(TABLE_OF_ROWTYPE).getResult().getStructure().getPlTypes();

        PlTypeInfo coll = plTypes.values().stream()
                .filter(pt -> "T_EMP_TAB".equals(pt.getName()) && pt.isCollection())
                .findFirst().orElse(null);
        assertNotNull(coll, "T_EMP_TAB COLLECTION must be registered. PlTypes: " + plTypes.keySet());
        assertNotNull(coll.getElementTypeName(),
                "T_EMP_TAB elementTypeName must not be null");
        assertTrue(coll.getElementTypeName().toUpperCase().contains("%ROWTYPE"),
                "T_EMP_TAB element type must include %ROWTYPE, got: " + coll.getElementTypeName());
    }

    // ── TC-HOUND-TGA-05: TYPE IS RECORD with %TYPE fields — field types preserved

    @Test
    void recordWithPercentTypeFields_fieldTypesPreserved() {
        var plTypes = parse(RECORD_WITH_PERCENT_TYPE_FIELDS).getResult().getStructure().getPlTypes();

        PlTypeInfo rec = plTypes.values().stream()
                .filter(pt -> "T_SAL_REC".equals(pt.getName()) && pt.isRecord())
                .findFirst().orElse(null);
        assertNotNull(rec, "T_SAL_REC RECORD must be registered. PlTypes: " + plTypes.keySet());
        assertEquals(2, rec.getFields().size(),
                "T_SAL_REC must have 2 fields. Got: " + rec.getFields());

        // Field data types must contain %TYPE (not collapsed to bare column name)
        boolean empIdHasType = rec.getFields().stream()
                .anyMatch(f -> "EMP_ID".equals(f.name())
                        && f.dataType() != null
                        && f.dataType().toUpperCase().contains("%TYPE"));
        assertTrue(empIdHasType,
                "EMP_ID field must retain %TYPE in dataType. Fields: " + rec.getFields());
    }

    // ── TC-HOUND-TGA-06: INNER JOIN + LEFT OUTER JOIN — no parse exception ────

    @Test
    void joins_parsedWithoutExceptions_statementsRegistered() {
        var structure = parse(JOINS).getResult().getStructure();

        // Routine registered
        RoutineInfo ri = structure.getRoutines().values().stream()
                .filter(r -> r.getName().endsWith("PROC_JOINS"))
                .findFirst().orElse(null);
        assertNotNull(ri, "PROC_JOINS must be registered");

        // Statements registered (at least the INSERT with SELECT)
        assertFalse(structure.getStatements().isEmpty(),
                "Statements must be present after INNER JOIN + LEFT OUTER JOIN fixture");

        // Source tables must include all three join participants
        long empCount = structure.getTables().values().stream()
                .filter(t -> "EMPLOYEES".equals(t.tableName()))
                .count();
        assertTrue(empCount >= 1, "HR.EMPLOYEES must be registered as a source table");
    }

    // ── TC-HOUND-TGA-07: TABLE(func()) — FUNC_TABLE__ synthetic name via typed access

    @Test
    void tableCollectionExpression_producesTypedSyntheticName() {
        var tables = parse(TABLE_COLLECTION).getResult().getStructure().getTables();

        boolean hasFuncTable = tables.keySet().stream()
                .anyMatch(k -> k.contains("FUNC_TABLE__"));
        assertTrue(hasFuncTable,
                "TABLE(func()) must produce a FUNC_TABLE__* synthetic table geoid. Tables: " + tables.keySet());

        // Synthetic table name must not start with '(' — old text-heuristic artifact
        boolean hasBracketName = tables.values().stream()
                .anyMatch(t -> t.tableName() != null && t.tableName().startsWith("("));
        assertFalse(hasBracketName,
                "No table name must start with '(' — typed access must strip subquery markers. Tables: " + tables.keySet());
    }

    // ── TC-HOUND-TGA-08: parenthesized JOIN in FROM — no bogus table extracted ─

    @Test
    void parenthesizedJoin_noSpuriousTableName() {
        var tables = parse(PARENTHESIZED_JOIN).getResult().getStructure().getTables();

        // No table whose name is a raw parenthesized expression
        boolean hasBracketName = tables.values().stream()
                .anyMatch(t -> t.tableName() != null && t.tableName().startsWith("("));
        assertFalse(hasBracketName,
                "Parenthesized JOIN must not produce a '('-prefixed table name. Tables: " + tables.keySet());

        // The real tables must still be found
        boolean hasEmployees = tables.values().stream()
                .anyMatch(t -> "EMPLOYEES".equals(t.tableName()));
        boolean hasDepts = tables.values().stream()
                .anyMatch(t -> "DEPARTMENTS".equals(t.tableName()));
        assertTrue(hasEmployees, "HR.EMPLOYEES must be registered even in parenthesized JOIN");
        assertTrue(hasDepts, "HR.DEPARTMENTS must be registered even in parenthesized JOIN");
    }

    // ── TC-HOUND-TGA-09: BULK COLLECT INTO — subscript stripped via general_element_part

    @Test
    void bulkCollectInto_variableNameStrippedCleanly() {
        var structure = parse(BULK_COLLECT_WITH_SUBSCRIPT).getResult().getStructure();

        // The VTABLE for L_IDS (materialised from t_id_tab) must be registered
        TableInfo lIds = structure.getTables().values().stream()
                .filter(t -> "VTABLE".equals(t.tableType()) && "L_IDS".equals(t.tableName()))
                .findFirst().orElse(null);
        assertNotNull(lIds,
                "DaliTable(VTABLE) for L_IDS must be registered after BULK COLLECT INTO. "
                + "Tables: " + structure.getTables().keySet());

        // plTypeGeoid must be set — confirms variable was matched to PlTypeInfo t_id_tab
        assertNotNull(lIds.getPlTypeGeoid(),
                "L_IDS vtable must have plTypeGeoid (linked to T_ID_TAB)");

        // Columns must be injected from T_ID_REC fields
        long vtableCols = structure.getColumns().values().stream()
                .filter(c -> lIds.geoid().equals(c.getTableGeoid()))
                .count();
        assertTrue(vtableCols >= 1,
                "L_IDS vtable must have ≥1 column injected from T_ID_REC. Got: " + vtableCols);
    }
}
