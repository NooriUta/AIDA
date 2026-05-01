package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.PlTypeInfo;
import com.hound.semantic.model.TableInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-HOUND-PT-01..05
 *
 * Verifies HND-01..05: PL/SQL TYPE IS RECORD / TABLE OF templates are parsed,
 * registered as PlTypeInfo, and materialised into DaliRecord instances with
 * propagated field definitions.
 */
class PlTypeLineageTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final String TYPE_RECORD_AND_COLLECTION = """
            CREATE OR REPLACE PACKAGE BODY DWH.PKG_PLTYPE_TEST AS
              TYPE t_rec IS RECORD (
                col_a NUMBER(19),
                col_b VARCHAR2(255)
              );
              TYPE t_tab IS TABLE OF t_rec INDEX BY PLS_INTEGER;

              PROCEDURE PROC_A IS
                l_tab t_tab;
              BEGIN
                NULL;
              END PROC_A;
            END PKG_PLTYPE_TEST;
            """;

    private static final String BULK_COLLECT_FORALL = """
            CREATE OR REPLACE PACKAGE BODY DWH.PKG_BULK_TEST AS
              TYPE t_journal_rec IS RECORD (
                journal_line_sk  NUMBER(19),
                debit_amount     NUMBER(19,4)
              );
              TYPE t_journal_tab IS TABLE OF t_journal_rec INDEX BY PLS_INTEGER;

              PROCEDURE LOAD_JOURNAL IS
                l_journal_tab t_journal_tab;
              BEGIN
                SELECT journal_line_sk, debit_amount
                BULK COLLECT INTO l_journal_tab
                FROM DWH.STG_JOURNAL;

                FORALL i IN l_journal_tab.FIRST..l_journal_tab.LAST
                  INSERT INTO DWH.FACT_JOURNAL (journal_line_sk, debit_amount)
                  VALUES (l_journal_tab(i).journal_line_sk, l_journal_tab(i).debit_amount);
              END LOAD_JOURNAL;
            END PKG_BULK_TEST;
            """;

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("DWH");
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    // ── TC-HOUND-PT-01: TYPE IS RECORD → DaliPlType(RECORD) + DaliPlTypeField ─

    @Test
    void typeIsRecord_producesPlTypeWithFields() {
        Map<String, PlTypeInfo> plTypes = parse(TYPE_RECORD_AND_COLLECTION).getResult()
                .getStructure().getPlTypes();

        PlTypeInfo rec = plTypes.values().stream()
                .filter(pt -> "T_REC".equals(pt.getName()) && pt.isRecord())
                .findFirst().orElse(null);

        assertNotNull(rec, "PlTypeInfo for T_REC must be registered. Keys: " + plTypes.keySet());
        assertEquals(PlTypeInfo.Kind.RECORD, rec.getKind());
        assertEquals(2, rec.getFields().size(),
                "T_REC must have exactly 2 fields. Got: " + rec.getFields());
        assertEquals("COL_A", rec.getFields().get(0).name());
        assertEquals("COL_B", rec.getFields().get(1).name());
        assertNotNull(rec.getFields().get(0).dataType(), "COL_A must have a dataType");
    }

    // ── TC-HOUND-PT-02: TABLE OF → DaliPlType(COLLECTION) with OF_TYPE link ──

    @Test
    void tableOf_producesCollectionPlType() {
        Map<String, PlTypeInfo> plTypes = parse(TYPE_RECORD_AND_COLLECTION).getResult()
                .getStructure().getPlTypes();

        PlTypeInfo coll = plTypes.values().stream()
                .filter(pt -> "T_TAB".equals(pt.getName()) && pt.isCollection())
                .findFirst().orElse(null);

        assertNotNull(coll, "PlTypeInfo for T_TAB (COLLECTION) must be registered. Keys: " + plTypes.keySet());
        assertEquals(PlTypeInfo.Kind.COLLECTION, coll.getKind());
        assertEquals("T_REC", coll.getElementTypeName(),
                "T_TAB element type name must be T_REC");
    }

    // ── TC-HOUND-PT-03: l_tab variable → DaliTable(VTABLE) with injected columns ───
    //   COLLECTION variable is a set of rows → virtual table, not a record.

    @Test
    void collectionVariable_materialisesVirtualTableWithColumns() {
        var result = parse(TYPE_RECORD_AND_COLLECTION).getResult();
        var structure = result.getStructure();
        Map<String, TableInfo> tables = structure.getTables();

        TableInfo lTab = tables.values().stream()
                .filter(t -> "VTABLE".equals(t.tableType()) && "L_TAB".equals(t.tableName()))
                .findFirst().orElse(null);

        assertNotNull(lTab, "DaliTable(VTABLE) for L_TAB must be present. Tables: " + tables.keySet());
        assertNotNull(lTab.getPlTypeGeoid(),
                "L_TAB vtable must have plTypeGeoid set (back-ref to T_TAB)");
        assertTrue(lTab.getPlTypeGeoid().contains("T_TAB"),
                "plTypeGeoid must reference T_TAB, got: " + lTab.getPlTypeGeoid());

        // Columns injected from T_REC fields
        long vtableCols = structure.getColumns().values().stream()
                .filter(c -> lTab.geoid().equals(c.getTableGeoid()))
                .count();
        assertEquals(2, vtableCols,
                "L_TAB vtable must have 2 columns from T_REC. Cols: " + structure.getColumns().keySet());
    }

    // ── TC-HOUND-PT-04: BULK COLLECT + FORALL → records + statements present ─

    @Test
    void bulkCollectForall_recordAndStatementsPresent() {
        var result = parse(BULK_COLLECT_FORALL).getResult();
        var structure = result.getStructure();

        // PlType templates registered
        assertFalse(structure.getPlTypes().isEmpty(),
                "PlType templates must be registered for BULK COLLECT fixture");

        PlTypeInfo recType = structure.getPlTypes().values().stream()
                .filter(pt -> "T_JOURNAL_REC".equals(pt.getName()))
                .findFirst().orElse(null);
        assertNotNull(recType, "T_JOURNAL_REC PlTypeInfo must be present");
        assertEquals(2, recType.getFields().size(),
                "T_JOURNAL_REC must have 2 fields");

        // COLLECTION variable materialised as DaliTable(VTABLE)
        TableInfo lJournal = structure.getTables().values().stream()
                .filter(t -> "VTABLE".equals(t.tableType()) && "L_JOURNAL_TAB".equals(t.tableName()))
                .findFirst().orElse(null);
        assertNotNull(lJournal, "DaliTable(VTABLE) for L_JOURNAL_TAB must be present");
        assertNotNull(lJournal.getPlTypeGeoid(),
                "L_JOURNAL_TAB vtable must have plTypeGeoid set");
        long vtableCols = structure.getColumns().values().stream()
                .filter(c -> lJournal.geoid().equals(c.getTableGeoid()))
                .count();
        assertEquals(2, vtableCols,
                "L_JOURNAL_TAB vtable must have 2 columns from T_JOURNAL_REC");

        // Statements parsed (SELECT + INSERT)
        assertFalse(structure.getStatements().isEmpty(),
                "Statements must be parsed for BULK COLLECT fixture");
    }

    // ── TC-HOUND-PT-05: element type geoid linked after variable materialisation

    @Test
    void collectionElementTypeGeoid_linkedAfterMaterialisation() {
        var structure = parse(BULK_COLLECT_FORALL).getResult().getStructure();

        PlTypeInfo collType = structure.getPlTypes().values().stream()
                .filter(pt -> "T_JOURNAL_TAB".equals(pt.getName()) && pt.isCollection())
                .findFirst().orElse(null);
        assertNotNull(collType, "T_JOURNAL_TAB PlTypeInfo must be present");

        PlTypeInfo recType = structure.getPlTypes().values().stream()
                .filter(pt -> "T_JOURNAL_REC".equals(pt.getName()) && pt.isRecord())
                .findFirst().orElse(null);
        assertNotNull(recType);

        assertNotNull(collType.getElementTypeGeoid(),
                "COLLECTION elementTypeGeoid must be set after variable materialisation");
        assertEquals(recType.getGeoid(), collType.getElementTypeGeoid(),
                "elementTypeGeoid must point to T_JOURNAL_REC");
    }
}
