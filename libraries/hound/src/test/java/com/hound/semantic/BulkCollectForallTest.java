package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hound.semantic.model.StatementInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4-D: BULK COLLECT + FORALL lineage tests.
 *
 * TC-HOUND-BK1: FETCH BULK COLLECT LIMIT + FORALL INSERT SAVE EXCEPTIONS
 * TC-HOUND-BK2: SELECT BULK COLLECT INTO (without cursor)
 */
class BulkCollectForallTest {

    private static UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    private static List<Map<String, Object>> log(String sql) {
        return parse(sql).getResolutionLog();
    }

    private static List<StatementInfo> stmtsOfType(UniversalSemanticEngine e, String type) {
        return e.getBuilder().getStatements().values().stream()
                .filter(s -> type.equals(s.getType()))
                .collect(Collectors.toList());
    }

    // ── TC-HOUND-BK1: FETCH BULK COLLECT LIMIT + FORALL INSERT ───────────────

    @Test
    @DisplayName("TC-HOUND-BK1: FETCH BULK COLLECT LIMIT + FORALL INSERT SAVE EXCEPTIONS — parses OK")
    void bulkCollect_fetchWithLimit_forallInsert_parsesWithoutException() {
        assertDoesNotThrow(() -> {
            UniversalSemanticEngine engine = parse("""
                    CREATE OR REPLACE PROCEDURE bulk_load IS
                        TYPE order_tab IS TABLE OF orders%ROWTYPE;
                        v_orders order_tab;
                        CURSOR c IS SELECT order_id, customer_id, amount FROM orders WHERE status = 'NEW';
                        v_errors PLS_INTEGER;
                    BEGIN
                        OPEN c;
                        LOOP
                            FETCH c BULK COLLECT INTO v_orders LIMIT 1000;
                            EXIT WHEN v_orders.COUNT = 0;
                            FORALL i IN 1..v_orders.COUNT SAVE EXCEPTIONS
                                INSERT INTO orders_archive (order_id, customer_id, amount)
                                VALUES (v_orders(i).order_id, v_orders(i).customer_id, v_orders(i).amount);
                        END LOOP;
                        CLOSE c;
                    END;
                    """);
            assertNotNull(engine, "Engine must not be null after BULK COLLECT + FORALL parsing");
        }, "FETCH BULK COLLECT LIMIT + FORALL INSERT must parse without crash");
    }

    @Test
    @DisplayName("TC-HOUND-BK1b: FORALL INSERT recognised as INSERT statement type")
    void bulkCollect_forallInsert_recognisedAsInsert() {
        UniversalSemanticEngine engine = parse("""
                CREATE OR REPLACE PROCEDURE p IS
                    TYPE id_tab IS TABLE OF NUMBER;
                    v_ids id_tab := id_tab(1, 2, 3);
                BEGIN
                    FORALL i IN 1..v_ids.COUNT
                        INSERT INTO target_table (id) VALUES (v_ids(i));
                END;
                """);

        List<StatementInfo> inserts = stmtsOfType(engine, "INSERT");
        assertFalse(inserts.isEmpty(),
                "FORALL INSERT must be recognised as an INSERT statement");
    }

    // ── TC-HOUND-BK2: SELECT BULK COLLECT INTO (without cursor) ──────────────

    @Test
    @DisplayName("TC-HOUND-BK2: SELECT BULK COLLECT INTO — customer_id from source table not unresolved")
    void bulkCollect_selectInto_columnsResolved() {
        var entries = log("""
                CREATE OR REPLACE PROCEDURE load_customers IS
                    TYPE id_tab IS TABLE OF NUMBER;
                    v_ids id_tab;
                BEGIN
                    SELECT customer_id
                    BULK COLLECT INTO v_ids
                    FROM customers
                    WHERE status = 'ACTIVE';
                END;
                """);

        long unresolvedCustomerId = entries.stream()
                .filter(e -> "customer_id".equalsIgnoreCase((String) e.get("raw_input")))
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedCustomerId,
                "customer_id from customers table must not be unresolved in BULK COLLECT INTO");
    }

    @Test
    @DisplayName("TC-HOUND-BK2b: SELECT BULK COLLECT INTO multiple columns — all resolved")
    void bulkCollect_multiColumn_allResolved() {
        var entries = log("""
                CREATE OR REPLACE PROCEDURE p IS
                    TYPE name_tab IS TABLE OF VARCHAR2(100);
                    TYPE email_tab IS TABLE OF VARCHAR2(200);
                    v_names  name_tab;
                    v_emails email_tab;
                BEGIN
                    SELECT first_name, email
                    BULK COLLECT INTO v_names, v_emails
                    FROM employees
                    WHERE department_id = 10;
                END;
                """);

        long unresolvedEmployeeCols = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && (raw.equalsIgnoreCase("first_name")
                            || raw.equalsIgnoreCase("email")
                            || raw.equalsIgnoreCase("department_id"));
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedEmployeeCols,
                "first_name, email, department_id from employees must not be unresolved in BULK COLLECT");
    }
}
