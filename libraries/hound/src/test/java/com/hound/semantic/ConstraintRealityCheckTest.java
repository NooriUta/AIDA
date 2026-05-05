package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.ConstraintInfo;
import com.hound.semantic.model.SemanticResult;
import com.hound.storage.JsonlBatchBuilder;
import com.hound.storage.WriteStats;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 3.1 CONSTRAINT_REALITY_CHECK — RC-1: Parser activation for all 4 constraint types.
 */
class ConstraintRealityCheckTest {

    private static final String DDL = """
            CREATE TABLE HR.DEPARTMENTS (
                DEPT_ID    NUMBER PRIMARY KEY,
                DEPT_NAME  VARCHAR2(100) NOT NULL,
                LOCATION   VARCHAR2(200),
                CONSTRAINT UQ_DEPT_NAME UNIQUE (DEPT_NAME),
                CONSTRAINT CHK_DEPT_LOC CHECK (LOCATION IS NOT NULL OR DEPT_NAME <> 'VIRTUAL')
            );

            CREATE TABLE HR.EMPLOYEES (
                EMP_ID       NUMBER,
                FIRST_NAME   VARCHAR2(50),
                LAST_NAME    VARCHAR2(50),
                EMAIL        VARCHAR2(100),
                DEPT_ID      NUMBER,
                MANAGER_ID   NUMBER,
                SALARY       NUMBER,
                CONSTRAINT PK_EMPLOYEES PRIMARY KEY (EMP_ID),
                CONSTRAINT FK_EMP_DEPT FOREIGN KEY (DEPT_ID) REFERENCES HR.DEPARTMENTS (DEPT_ID) ON DELETE SET NULL,
                CONSTRAINT FK_EMP_MGR  FOREIGN KEY (MANAGER_ID) REFERENCES HR.EMPLOYEES (EMP_ID),
                CONSTRAINT UQ_EMP_EMAIL UNIQUE (EMAIL),
                CONSTRAINT CHK_EMP_SAL CHECK (SALARY > 0)
            );

            CREATE TABLE HR.PROJECTS (
                PROJ_ID    NUMBER,
                PROJ_NAME  VARCHAR2(200),
                LEAD_ID    NUMBER,
                CONSTRAINT PK_PROJECTS PRIMARY KEY (PROJ_ID),
                CONSTRAINT FK_PROJ_LEAD FOREIGN KEY (LEAD_ID) REFERENCES HR.EMPLOYEES (EMP_ID) ON DELETE CASCADE
            );
            """;

    private static UniversalSemanticEngine engine;
    private static Map<String, ConstraintInfo> constraints;

    @BeforeAll
    static void parseOnce() {
        engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(DDL));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        constraints = engine.getBuilder().getConstraints();
    }

    // ═══════ RC-1.1: All constraint types present ═══════

    @Test
    void allFourTypesDetected() {
        long pkCount = constraints.values().stream().filter(ConstraintInfo::isPrimaryKey).count();
        long fkCount = constraints.values().stream().filter(ConstraintInfo::isForeignKey).count();
        long uqCount = constraints.values().stream().filter(ConstraintInfo::isUniqueConstraint).count();
        long chCount = constraints.values().stream().filter(ConstraintInfo::isCheckConstraint).count();

        assertTrue(pkCount >= 2, "Expected >=2 PKs (DEPARTMENTS inline + EMPLOYEES + PROJECTS), got " + pkCount);
        assertTrue(fkCount >= 3, "Expected >=3 FKs (FK_EMP_DEPT + FK_EMP_MGR + FK_PROJ_LEAD), got " + fkCount);
        assertTrue(uqCount >= 2, "Expected >=2 UQs (UQ_DEPT_NAME + UQ_EMP_EMAIL), got " + uqCount);
        assertTrue(chCount >= 2, "Expected >=2 CHKs (CHK_DEPT_LOC + CHK_EMP_SAL), got " + chCount);

        System.out.println("RC-1.1: Constraints by type — PK=" + pkCount + " FK=" + fkCount
                + " UQ=" + uqCount + " CH=" + chCount + " total=" + constraints.size());
    }

    // ═══════ RC-1.2: PK constraint details ═══════

    @Test
    void pk_employees_hasCorrectColumns() {
        ConstraintInfo pk = findByName("PK_EMPLOYEES");
        assertNotNull(pk, "PK_EMPLOYEES should exist. All constraints: " + constraintNames());
        assertEquals(ConstraintInfo.TYPE_PK, pk.getConstraintType());
        assertEquals(List.of("EMP_ID"), pk.getColumnNames(), "PK columns");
        assertTrue(pk.getHostTableGeoid().toUpperCase().contains("EMPLOYEES"),
                "Host table should be EMPLOYEES, got: " + pk.getHostTableGeoid());
    }

    @Test
    void pk_projects_hasCorrectColumns() {
        ConstraintInfo pk = findByName("PK_PROJECTS");
        assertNotNull(pk, "PK_PROJECTS should exist. All constraints: " + constraintNames());
        assertEquals(List.of("PROJ_ID"), pk.getColumnNames());
    }

    // ═══════ RC-1.3: FK constraint details ═══════

    @Test
    void fk_empDept_referencesCorrectTable() {
        ConstraintInfo fk = findByName("FK_EMP_DEPT");
        assertNotNull(fk, "FK_EMP_DEPT should exist. All constraints: " + constraintNames());
        assertEquals(ConstraintInfo.TYPE_FK, fk.getConstraintType());
        assertEquals(List.of("DEPT_ID"), fk.getColumnNames(), "FK columns");

        assertNotNull(fk.getRefTableGeoid(), "Referenced table geoid should not be null");
        assertTrue(fk.getRefTableGeoid().toUpperCase().contains("DEPARTMENTS"),
                "Should reference DEPARTMENTS, got: " + fk.getRefTableGeoid());
        assertEquals(List.of("DEPT_ID"), fk.getRefColumnNames(), "Referenced columns");
        assertEquals("SET NULL", fk.getOnDelete(), "ON DELETE action");
    }

    @Test
    void fk_projLead_hasCascadeDelete() {
        ConstraintInfo fk = findByName("FK_PROJ_LEAD");
        assertNotNull(fk, "FK_PROJ_LEAD should exist. All constraints: " + constraintNames());
        assertEquals("CASCADE", fk.getOnDelete(), "ON DELETE CASCADE expected");
        assertEquals(List.of("LEAD_ID"), fk.getColumnNames());
        assertTrue(fk.getRefTableGeoid().toUpperCase().contains("EMPLOYEES"),
                "Should reference EMPLOYEES, got: " + fk.getRefTableGeoid());
    }

    @Test
    void fk_empMgr_selfReference() {
        ConstraintInfo fk = findByName("FK_EMP_MGR");
        assertNotNull(fk, "FK_EMP_MGR should exist. All constraints: " + constraintNames());
        assertEquals(List.of("MANAGER_ID"), fk.getColumnNames());
        assertTrue(fk.getRefTableGeoid().toUpperCase().contains("EMPLOYEES"),
                "Self-referencing FK should point to EMPLOYEES, got: " + fk.getRefTableGeoid());
        assertNull(fk.getOnDelete(), "No ON DELETE clause for FK_EMP_MGR");
    }

    // ═══════ RC-1.4: UNIQUE constraint details ═══════

    @Test
    void uq_empEmail_hasCorrectColumn() {
        ConstraintInfo uq = findByName("UQ_EMP_EMAIL");
        assertNotNull(uq, "UQ_EMP_EMAIL should exist. All constraints: " + constraintNames());
        assertEquals(ConstraintInfo.TYPE_UQ, uq.getConstraintType());
        assertEquals(List.of("EMAIL"), uq.getColumnNames());
    }

    @Test
    void uq_deptName_hasCorrectColumn() {
        ConstraintInfo uq = findByName("UQ_DEPT_NAME");
        assertNotNull(uq, "UQ_DEPT_NAME should exist. All constraints: " + constraintNames());
        assertEquals(List.of("DEPT_NAME"), uq.getColumnNames());
    }

    // ═══════ RC-1.5: CHECK constraint details ═══════

    @Test
    void chk_empSal_hasExpression() {
        ConstraintInfo chk = findByName("CHK_EMP_SAL");
        assertNotNull(chk, "CHK_EMP_SAL should exist. All constraints: " + constraintNames());
        assertEquals(ConstraintInfo.TYPE_CH, chk.getConstraintType());
        assertNotNull(chk.getCheckExpression(), "CHECK expression should be captured");
        assertTrue(chk.getCheckExpression().toUpperCase().contains("SALARY"),
                "Expression should mention SALARY, got: " + chk.getCheckExpression());
    }

    @Test
    void chk_deptLoc_hasExpression() {
        ConstraintInfo chk = findByName("CHK_DEPT_LOC");
        assertNotNull(chk, "CHK_DEPT_LOC should exist. All constraints: " + constraintNames());
        assertNotNull(chk.getCheckExpression());
        assertTrue(chk.getCheckExpression().toUpperCase().contains("LOCATION"),
                "Expression should mention LOCATION, got: " + chk.getCheckExpression());
    }

    // ═══════ RC-1.6: GEOID scheme correctness ═══════

    @Test
    void pk_geoid_followsScheme() {
        ConstraintInfo pk = findByName("PK_EMPLOYEES");
        assertNotNull(pk);
        assertTrue(pk.getGeoid().endsWith("#PK"),
                "PK geoid should end with #PK, got: " + pk.getGeoid());
    }

    @Test
    void fk_geoid_containsTypeAndName() {
        ConstraintInfo fk = findByName("FK_EMP_DEPT");
        assertNotNull(fk);
        assertTrue(fk.getGeoid().contains("#FK#"),
                "FK geoid should contain #FK#, got: " + fk.getGeoid());
        assertTrue(fk.getGeoid().toUpperCase().contains("FK_EMP_DEPT"),
                "FK geoid should contain constraint name, got: " + fk.getGeoid());
    }

    // ═══════ RC-1.7: Diagnostics dump ═══════

    @Test
    void dumpAllConstraints() {
        System.out.println("=== RC-1 Constraint Reality Check — Full Dump ===");
        constraints.forEach((geoid, c) -> {
            System.out.printf("  %-50s  type=%-2s  name=%-20s  table=%s  cols=%s%n",
                    geoid, c.getConstraintType(),
                    c.getConstraintName() != null ? c.getConstraintName() : "(unnamed)",
                    c.getHostTableGeoid(), c.getColumnNames());
            if (c.isForeignKey()) {
                System.out.printf("    -> ref_table=%s  ref_cols=%s  on_delete=%s%n",
                        c.getRefTableGeoid(), c.getRefColumnNames(), c.getOnDelete());
            }
            if (c.isCheckConstraint()) {
                System.out.printf("    -> expr=%s%n", c.getCheckExpression());
            }
        });
        System.out.println("  TOTAL: " + constraints.size());
    }

    // ═══════ RC-2: JBB batch output — constraint vertices ═══════

    @Test
    void jbb_emitsConstraintVertices() {
        SemanticResult result = engine.getResult("rc2-sid", "/rc2.sql", "plsql", 0);
        JsonlBatchBuilder jbb = JsonlBatchBuilder.buildFromResult("rc2-sid", result);
        WriteStats ws = jbb.writeStats();

        assertTrue(ws.inserted("DaliPrimaryKey") >= 2,
                "Expected >=2 DaliPrimaryKey vertices, got " + ws.inserted("DaliPrimaryKey"));
        assertTrue(ws.inserted("DaliForeignKey") >= 3,
                "Expected >=3 DaliForeignKey vertices, got " + ws.inserted("DaliForeignKey"));
        assertTrue(ws.inserted("DaliUniqueConstraint") >= 2,
                "Expected >=2 DaliUniqueConstraint vertices, got " + ws.inserted("DaliUniqueConstraint"));
        assertTrue(ws.inserted("DaliCheckConstraint") >= 2,
                "Expected >=2 DaliCheckConstraint vertices, got " + ws.inserted("DaliCheckConstraint"));

        System.out.println("RC-2: JBB vertex counts — PK=" + ws.inserted("DaliPrimaryKey")
                + " FK=" + ws.inserted("DaliForeignKey")
                + " UQ=" + ws.inserted("DaliUniqueConstraint")
                + " CH=" + ws.inserted("DaliCheckConstraint"));
    }

    @Test
    void jbb_constraintVerticesInPayload() {
        SemanticResult result = engine.getResult("rc2-sid2", "/rc2.sql", "plsql", 0);
        JsonlBatchBuilder jbb = JsonlBatchBuilder.buildFromResult("rc2-sid2", result);
        String payload = jbb.build();

        assertTrue(payload.contains("DaliPrimaryKey"), "Payload should contain DaliPrimaryKey vertex lines");
        assertTrue(payload.contains("DaliForeignKey"), "Payload should contain DaliForeignKey vertex lines");
        assertTrue(payload.contains("DaliUniqueConstraint"), "Payload should contain DaliUniqueConstraint vertex lines");
        assertTrue(payload.contains("DaliCheckConstraint"), "Payload should contain DaliCheckConstraint vertex lines");

        assertTrue(payload.contains("FK_EMP_DEPT"), "Payload should contain FK_EMP_DEPT constraint name");
        assertTrue(payload.contains("PK_EMPLOYEES"), "Payload should contain PK_EMPLOYEES constraint name");
        assertTrue(payload.contains("UQ_EMP_EMAIL"), "Payload should contain UQ_EMP_EMAIL constraint name");
        assertTrue(payload.contains("SALARY"), "Payload should contain CHECK expression referencing SALARY");
    }

    @Test
    void jbb_fkVertexHasRefFields() {
        SemanticResult result = engine.getResult("rc2-sid3", "/rc2.sql", "plsql", 0);
        JsonlBatchBuilder jbb = JsonlBatchBuilder.buildFromResult("rc2-sid3", result);
        String payload = jbb.build();

        assertTrue(payload.contains("ref_table_geoid"), "FK vertex should have ref_table_geoid field");
        assertTrue(payload.contains("ref_column_names"), "FK vertex should have ref_column_names field");
        assertTrue(payload.contains("on_delete"), "FK vertex should have on_delete field");
    }

    // ═══════ Helpers ═══════

    private ConstraintInfo findByName(String name) {
        return constraints.values().stream()
                .filter(c -> name.equalsIgnoreCase(c.getConstraintName()))
                .findFirst().orElse(null);
    }

    private List<String> constraintNames() {
        return constraints.values().stream()
                .map(c -> c.getConstraintName() + "(" + c.getConstraintType() + ")")
                .toList();
    }
}
