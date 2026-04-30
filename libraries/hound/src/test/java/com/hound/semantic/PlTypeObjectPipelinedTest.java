package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.PlTypeInfo;
import com.hound.semantic.model.RecordInfo;
import com.hound.semantic.model.RoutineInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-HOUND-PT-06..09 (HND-11)
 *
 * Verifies HND-08..10: schema-level CREATE TYPE AS OBJECT, TABLE OF (COLLECTION),
 * PIPELINED function flag capture, and PIPE ROW atom emission.
 */
class PlTypeObjectPipelinedTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UniversalSemanticEngine parse(String sql) {
        var input   = CharStreams.fromString(sql);
        var lexer   = new PlSqlLexer(input);
        lexer.removeErrorListeners();
        var tokens  = new CommonTokenStream(lexer);
        var parser  = new PlSqlParser(tokens);
        parser.removeErrorListeners();
        var tree    = parser.sql_script();
        var engine  = new UniversalSemanticEngine();
        var listener = new PlSqlSemanticListener(engine);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        return engine;
    }

    // ── TC-HOUND-PT-06: CREATE TYPE AS OBJECT ────────────────────────────────

    private static final String OBJECT_TYPE_SCHEMA = """
            CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_REC AS OBJECT (
                item_id    NUMBER(10),
                qty        NUMBER(12,3),
                unit_price NUMBER(18,4)
            );
            """;

    @Test
    void tc06_createTypeAsObject_registeredAsObjectKind() {
        var engine = parse(OBJECT_TYPE_SCHEMA);
        Map<String, PlTypeInfo> types = engine.getBuilder().getPlTypes();

        PlTypeInfo pt = types.get("TESTSCHEMA:TYPE:T_LINE_REC");
        assertNotNull(pt, "DaliPlType T_LINE_REC must be registered");
        assertEquals(PlTypeInfo.Kind.OBJECT, pt.getKind());
        assertEquals("T_LINE_REC", pt.getName());
        assertEquals("TESTSCHEMA", pt.getScopeGeoid());
        assertEquals(3, pt.getFields().size(), "three attributes expected");
        assertEquals("ITEM_ID",    pt.getFields().get(0).name());
        assertEquals("QTY",        pt.getFields().get(1).name());
        assertEquals("UNIT_PRICE", pt.getFields().get(2).name());
    }

    // ── TC-HOUND-PT-07: TABLE OF schema-level COLLECTION ─────────────────────

    private static final String OBJECT_AND_COLLECTION = """
            CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_REC AS OBJECT (
                item_id    NUMBER(10),
                qty        NUMBER(12,3),
                unit_price NUMBER(18,4)
            );
            /
            CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_LIST AS TABLE OF TESTSCHEMA.T_LINE_REC;
            """;

    @Test
    void tc07_tableOfSchemaLevel_registeredAsCollectionWithOfTypeLink() {
        var engine = parse(OBJECT_AND_COLLECTION);
        Map<String, PlTypeInfo> types = engine.getBuilder().getPlTypes();

        PlTypeInfo obj = types.get("TESTSCHEMA:TYPE:T_LINE_REC");
        assertNotNull(obj, "T_LINE_REC must be registered");
        assertEquals(PlTypeInfo.Kind.OBJECT, obj.getKind());

        PlTypeInfo col = types.get("TESTSCHEMA:TYPE:T_LINE_LIST");
        assertNotNull(col, "T_LINE_LIST must be registered");
        assertEquals(PlTypeInfo.Kind.COLLECTION, col.getKind());
        // elementTypeName before resolution
        assertNotNull(col.getElementTypeName(), "element type name must be set");
        assertTrue(col.getElementTypeName().toUpperCase().contains("T_LINE_REC"),
                "element type should reference T_LINE_REC");
    }

    // ── TC-HOUND-PT-08: PIPELINED function flag ───────────────────────────────

    private static final String PIPELINED_FUNC = """
            CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_REC AS OBJECT (
                item_id    NUMBER(10),
                qty        NUMBER(12,3),
                unit_price NUMBER(18,4)
            );
            /
            CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_LIST AS TABLE OF TESTSCHEMA.T_LINE_REC;
            /
            CREATE OR REPLACE PACKAGE BODY PKG_PIPE_TEST AS
                FUNCTION GET_LINES(p_id IN NUMBER) RETURN TESTSCHEMA.T_LINE_LIST PIPELINED IS
                    CURSOR c IS SELECT item_id, qty, unit_price FROM ORDER_LINES WHERE order_id = p_id;
                BEGIN
                    FOR r IN c LOOP
                        PIPE ROW (TESTSCHEMA.T_LINE_REC(
                            item_id => r.item_id, qty => r.qty, unit_price => r.unit_price));
                    END LOOP;
                END GET_LINES;
            END PKG_PIPE_TEST;
            """;

    @Test
    void tc08_pipelinedFunction_flagSet_returnTypeCaptured() {
        var engine = parse(PIPELINED_FUNC);
        Map<String, RoutineInfo> routines = engine.getBuilder().getRoutines();

        RoutineInfo ri = routines.values().stream()
                .filter(r -> r.getName().equalsIgnoreCase("GET_LINES"))
                .findFirst()
                .orElse(null);
        assertNotNull(ri, "GET_LINES routine must be registered");
        assertTrue(ri.isPipelined(), "GET_LINES must be marked pipelined");
        assertNotNull(ri.getReturnType(), "returnType must be captured");
        assertTrue(ri.getReturnType().toUpperCase().contains("T_LINE_LIST"),
                "returnType must reference T_LINE_LIST");
    }

    // ── TC-HOUND-PT-09: OBJECT variable materialisation ──────────────────────

    private static final String OBJECT_VARIABLE = """
            CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_REC AS OBJECT (
                item_id    NUMBER(10),
                qty        NUMBER(12,3),
                unit_price NUMBER(18,4)
            );
            /
            CREATE OR REPLACE PACKAGE BODY PKG_OBJ_VAR AS
                PROCEDURE PROC_A IS
                    v_line TESTSCHEMA.T_LINE_REC;
                BEGIN
                    NULL;
                END PROC_A;
            END PKG_OBJ_VAR;
            """;

    @Test
    void tc09_objectVariable_materialisedAsDaliRecord() {
        var engine = parse(OBJECT_VARIABLE);
        Map<String, RecordInfo> records = engine.getBuilder().getRecords();

        // A DaliRecord should be materialised for the variable v_line
        boolean found = records.values().stream()
                .anyMatch(r -> r.getVarName().equalsIgnoreCase("V_LINE")
                        && r.getFields().size() == 3);
        assertTrue(found, "v_line DaliRecord with 3 fields must be materialised from T_LINE_REC");
    }

    // ── TC-HOUND-PT-10: VARRAY type ──────────────────────────────────────────

    private static final String VARRAY_LOCAL = """
            CREATE OR REPLACE PACKAGE BODY PKG_VARRAY_TEST AS
                PROCEDURE PROC_A IS
                    TYPE t_ids IS VARRAY(10) OF NUMBER(10);
                    v_ids t_ids;
                BEGIN
                    NULL;
                END PROC_A;
            END PKG_VARRAY_TEST;
            """;

    @Test
    void tc10_varrayType_registeredAsVarrayKind() {
        var engine = parse(VARRAY_LOCAL);
        Map<String, PlTypeInfo> types = engine.getBuilder().getPlTypes();

        PlTypeInfo pt = types.values().stream()
                .filter(t -> "T_IDS".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(pt, "T_IDS VARRAY must be registered");
        assertEquals(PlTypeInfo.Kind.VARRAY, pt.getKind());
        assertNotNull(pt.getElementTypeName(), "element type name must be set");
        assertEquals(0, pt.getFields().size(), "VARRAY must have no field definitions");
    }

    // ── TC-HOUND-PT-11a: REF CURSOR strong typed ─────────────────────────────

    private static final String REF_CURSOR_STRONG = """
            CREATE OR REPLACE PACKAGE BODY PKG_CURSOR_TEST AS
                PROCEDURE PROC_A IS
                    TYPE t_emp_cur IS REF CURSOR RETURN EMPLOYEES%ROWTYPE;
                    v_cur t_emp_cur;
                BEGIN
                    NULL;
                END PROC_A;
            END PKG_CURSOR_TEST;
            """;

    @Test
    void tc11a_refCursorStrong_registeredWithReturnType() {
        var engine = parse(REF_CURSOR_STRONG);
        Map<String, PlTypeInfo> types = engine.getBuilder().getPlTypes();

        PlTypeInfo pt = types.values().stream()
                .filter(t -> "T_EMP_CUR".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(pt, "T_EMP_CUR REF CURSOR must be registered");
        assertEquals(PlTypeInfo.Kind.REF_CURSOR, pt.getKind());
        assertNotNull(pt.getElementTypeName(), "strong cursor must have return type");
        assertTrue(pt.getElementTypeName().toUpperCase().contains("EMPLOYEES"),
                "return type must reference EMPLOYEES table");
    }

    // ── TC-HOUND-PT-11b: REF CURSOR weak ─────────────────────────────────────

    private static final String REF_CURSOR_WEAK = """
            CREATE OR REPLACE PACKAGE BODY PKG_CURSOR_TEST AS
                PROCEDURE PROC_A IS
                    TYPE t_weak_cur IS REF CURSOR;
                    v_cur t_weak_cur;
                BEGIN
                    NULL;
                END PROC_A;
            END PKG_CURSOR_TEST;
            """;

    @Test
    void tc11b_refCursorWeak_registeredWithoutReturnType() {
        var engine = parse(REF_CURSOR_WEAK);
        Map<String, PlTypeInfo> types = engine.getBuilder().getPlTypes();

        PlTypeInfo pt = types.values().stream()
                .filter(t -> "T_WEAK_CUR".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(pt, "T_WEAK_CUR REF CURSOR must be registered");
        assertEquals(PlTypeInfo.Kind.REF_CURSOR, pt.getKind());
        assertNull(pt.getElementTypeName(), "weak cursor has no return type");
    }
}
