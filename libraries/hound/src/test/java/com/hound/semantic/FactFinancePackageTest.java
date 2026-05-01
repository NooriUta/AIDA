package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.PlTypeInfo;
import com.hound.semantic.model.RoutineInfo;
import com.hound.semantic.model.StatementInfo;
import com.hound.semantic.model.Structure;
import com.hound.semantic.model.TableInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * TC-HOUND-FIN-01..14
 * Comprehensive parse + structure assertions for production package
 *   {@code DWH.PKG_ETL_FACT_FINANCE} (PKG_ETL_05_FACT_FINANCE.sql, 7058 строк).
 *
 * <p>The fixture is a real ERP financial-DWH ETL package combining:
 *   <ul>
 *     <li>4x scalar TABLE OF (NUMBER/DATE/VARCHAR2) PL/SQL collections</li>
 *     <li>6x TYPE IS RECORD (~180 fields total) + 6x TABLE OF the records</li>
 *     <li>3x strong-typed REF CURSOR</li>
 *     <li>18 routines: 7 public ETL procedures + 2 PIPELINED PARALLEL_ENABLE
 *         functions + 4 private helpers + 4 reconcile/calc procedures + 2
 *         currency-lookup functions</li>
 *     <li>PRAGMA AUTONOMOUS_TRANSACTION (log_step)</li>
 *     <li>BULK COLLECT INTO + FORALL ... VALUES OF SAVE EXCEPTIONS (Oracle 11g+ syntax)</li>
 *     <li>MERGE INTO + WITH (nested CTEs, up to 12 per statement) + window functions</li>
 *     <li>Cross-schema reads: FIN.*, CRM.*, DWH.* dimensions</li>
 *     <li>EXECUTE IMMEDIATE 'TRUNCATE TABLE ...'</li>
 *     <li>Sequence NEXTVAL references</li>
 *   </ul>
 *
 * <p><b>Interdependent test order:</b> TC-FIN-01 (parse) is the foundational
 * test. Every subsequent test calls {@code assumeTrue(parseSucceeded, ...)}
 * so a parse failure aborts the whole suite cleanly with SKIPPED markers
 * instead of cascading false failures.
 *
 * <p>Tag {@code "plsql_parse"} groups all assertions for selective test runs:
 * {@code ./gradlew :libraries:hound:test --tests '*FactFinancePackageTest'} or
 * {@code ./gradlew test -DincludeTags=plsql_parse}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("plsql_parse")
class FactFinancePackageTest {

    private static final String FIXTURE = "plsql/finance/PKG_ETL_05_FACT_FINANCE.sql";

    // ── Shared state — populated by TC-FIN-01 (@BeforeAll), read by all tests ─

    private UniversalSemanticEngine engine;
    private Structure structure;
    private List<String> antlrErrors = new ArrayList<>();
    private List<String> antlrAmbiguities = new ArrayList<>();
    private boolean parseSucceeded = false;
    private long parseDurationMs = -1;

    // ── @BeforeAll: parse once, share across all tests ────────────────────────

    @BeforeAll
    void parsePackageOnce() throws Exception {
        long t0 = System.currentTimeMillis();

        // 1. Load fixture from test resources
        String sql;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(FIXTURE)) {
            assertNotNull(is, "Fixture not found on classpath: " + FIXTURE);
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 2. Pass SQL directly — grammar's sql_plus_command rule handles directives.
        String cleaned = sql;

        // 3. Capture ANTLR4 syntax errors AND ambiguity reports separately —
        //    distinguishes real syntax errors from grammar ambiguity warnings.
        BaseErrorListener errListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> r, Object offending,
                                    int line, int charPos,
                                    String msg, RecognitionException ex) {
                antlrErrors.add("line " + line + ":" + charPos + " — " + msg);
            }
            @Override
            public void reportAmbiguity(Parser recognizer,
                                        org.antlr.v4.runtime.dfa.DFA dfa,
                                        int startIndex, int stopIndex,
                                        boolean exact,
                                        java.util.BitSet ambigAlts,
                                        org.antlr.v4.runtime.atn.ATNConfigSet configs) {
                Token t = recognizer.getTokenStream().get(startIndex);
                antlrAmbiguities.add("line " + t.getLine() + ":" + t.getCharPositionInLine()
                        + " — AMBIGUITY in " + dfa.toString(recognizer.getVocabulary())
                        + " alts=" + ambigAlts);
            }
        };

        // 4. Run lexer + parser + walker
        engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("DWH");

        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(cleaned));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errListener);

        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errListener);
        // Enable full-context (LL) prediction to surface true syntax errors
        // and report ambiguities. SLL mode (default) can hide grammar issues.
        parser.getInterpreter().setPredictionMode(
                org.antlr.v4.runtime.atn.PredictionMode.LL_EXACT_AMBIG_DETECTION);

        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();

        structure = engine.getResult().getStructure();
        parseDurationMs = System.currentTimeMillis() - t0;
        parseSucceeded = true;  // reach here only if no exception
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-01: FOUNDATIONAL — parse must succeed end-to-end
    //  Если этот тест падает, все последующие будут SKIPPED (assumeTrue).
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(1)
    void tcFin01_parseSucceedsEndToEnd() {
        assertTrue(parseSucceeded,
                "Parser must complete walking PKG_ETL_FACT_FINANCE without exceptions");
        assertNotNull(structure, "Structure must be populated post-walk");

        System.out.println("[TC-FIN-01] Parse OK in " + parseDurationMs + "ms; "
                + "ANTLR errors=" + antlrErrors.size()
                + ", routines=" + structure.getRoutines().size()
                + ", plTypes=" + structure.getPlTypes().size()
                + ", tables=" + structure.getTables().size()
                + ", columns=" + structure.getColumns().size()
                + ", statements=" + structure.getStatements().size());

        // Post-fix counts (after CONNECT-BY guard in isSqlPlusDirective):
        //   ANTLR errors=0, routines=18, plTypes=19, statements ≥ 400.
        // Pre-fix: 27 errors, 14 routines, 7 routines with 0 statements inside.
        assertTrue(antlrErrors.isEmpty(),
                "Expected 0 ANTLR errors after CONNECT-BY guard fix; got "
                + antlrErrors.size() + ":\n  "
                + String.join("\n  ", antlrErrors.stream().limit(10).toList()));
        assertTrue(structure.getRoutines().size() >= 18,
                "Expected ≥ 18 routines (all spec routines + private helpers); got " + structure.getRoutines().size());
        assertTrue(structure.getPlTypes().size() >= 19,
                "Expected ≥ 19 PlTypes; got " + structure.getPlTypes().size());
        assertTrue(structure.getStatements().size() >= 350,
                "Expected ≥ 350 statements (full body parse); got " + structure.getStatements().size());

        // Soft assertion: a 7K-line production package has Oracle constructs that
        // the open-source PlSqlParser grammar partially supports (CONNECT BY
        // hierarchical SELECT, certain analytic-function syntax). All errors
        // are RECOVERED — parser walked the rest of the file and populated
        // structure successfully. Threshold ≤ 50 keeps test stable across
        // grammar updates while still catching catastrophic regressions.
        assertTrue(antlrErrors.size() <= 50,
                "ANTLR4 errors must be ≤ 50 (recoverable). Got " + antlrErrors.size()
                + ":\n  " + String.join("\n  ", antlrErrors.stream().limit(10).toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-02: All 18 routines registered (post CONNECT-BY guard fix)
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(2)
    void tcFin02_allRoutinesRegistered() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 (parse) did not succeed");

        Map<String, RoutineInfo> routines = structure.getRoutines();
        Set<String> expectedNames = Set.of(
                // 7 public ETL procedures
                "LOAD_FACT_INVOICES", "LOAD_FACT_PAYMENTS", "LOAD_FACT_JOURNAL_ENTRIES",
                "LOAD_FACT_BUDGET_VARIANCE", "LOAD_FACT_PAYMENT_AGING",
                "LOAD_FACT_GL_BALANCE", "CALC_INTERCOMPANY_ELIM",
                // 2 PIPELINED functions
                "PIPE_FACT_INVOICES", "PIPE_FACT_JOURNALS",
                // 3 private helpers
                "LOG_STEP", "INIT_RUN", "HANDLE_FORALL_EXCEPTIONS",
                // 2 currency lookup functions
                "GET_FUNCTIONAL_CURRENCY", "GET_REPORTING_CURRENCY",
                // 4 reconcile/calc procedures (now reachable after fix)
                "RECONCILE_INVOICE_TOTALS", "RECONCILE_PAYMENT_TOTALS",
                "RECONCILE_JOURNAL_TOTALS", "CALCULATE_CASH_POSITION"
        );

        Set<String> seen = new HashSet<>();
        for (RoutineInfo ri : routines.values()) {
            String simple = ri.getName().toUpperCase();
            int dot = simple.lastIndexOf('.');
            if (dot >= 0) simple = simple.substring(dot + 1);
            seen.add(simple);
        }

        Set<String> missing = new HashSet<>(expectedNames);
        missing.removeAll(seen);

        assertTrue(missing.isEmpty(),
                "Missing routines: " + missing + "\nSeen: " + seen);
        assertTrue(routines.size() >= 18,
                "Expected ≥ 18 DaliRoutine vertices; got " + routines.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-03: 6 RECORD types registered with all their fields
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(3)
    void tcFin03_recordTypesRegistered() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Map<String, PlTypeInfo> plTypes = structure.getPlTypes();

        // Min field counts per RECORD (verified against fixture lines 21-233).
        // Using ≥ to absorb minor field-counting differences and remain stable
        // across grammar updates.
        Map<String, Integer> expectedRecords = Map.of(
                "T_INVOICE_STG_REC", 38,
                "T_PAYMENT_STG_REC", 29,
                "T_JOURNAL_STG_REC", 40,
                "T_BUDGET_VAR_REC",  29,
                "T_GL_BALANCE_REC",  27,
                "T_IC_ELIM_REC",     17
        );

        for (var e : expectedRecords.entrySet()) {
            String name = e.getKey();
            int minFieldCount = e.getValue();

            PlTypeInfo rec = plTypes.values().stream()
                    .filter(pt -> name.equals(pt.getName()) && pt.isRecord())
                    .findFirst().orElse(null);

            assertNotNull(rec, "RECORD " + name + " must be registered");
            assertTrue(rec.getFields().size() >= minFieldCount,
                    name + " must have ≥ " + minFieldCount
                    + " fields; got " + rec.getFields().size());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-04: 6 record-based + 4 scalar TABLE OF collections registered
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(4)
    void tcFin04_collectionTypesRegistered() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Map<String, PlTypeInfo> plTypes = structure.getPlTypes();

        // 6 record-based COLLECTIONs
        Set<String> expectedRecCollections = Set.of(
                "T_INVOICE_STG_TAB", "T_PAYMENT_STG_TAB", "T_JOURNAL_STG_TAB",
                "T_BUDGET_VAR_TAB",  "T_GL_BALANCE_TAB",  "T_IC_ELIM_TAB"
        );
        for (String name : expectedRecCollections) {
            PlTypeInfo coll = plTypes.values().stream()
                    .filter(pt -> name.equals(pt.getName()) && pt.isCollection())
                    .findFirst().orElse(null);
            assertNotNull(coll, "COLLECTION " + name + " must be registered");
            assertNotNull(coll.getElementTypeName(),
                    name + " elementTypeName must be set");
        }

        // 4 scalar TABLE OF collections (NUMBER/DATE/VARCHAR2)
        Set<String> expectedScalarCollections = Set.of(
                "T_ID_ARRAY", "T_AMOUNT_ARRAY",
                "T_VARCHAR_ARRAY", "T_DATE_ARRAY"
        );
        for (String name : expectedScalarCollections) {
            PlTypeInfo coll = plTypes.values().stream()
                    .filter(pt -> name.equals(pt.getName()) && pt.isCollection())
                    .findFirst().orElse(null);
            assertNotNull(coll, "Scalar COLLECTION " + name + " must be registered");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-05: 3 strong-typed REF CURSORs registered
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(5)
    void tcFin05_refCursorsRegistered() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Map<String, PlTypeInfo> plTypes = structure.getPlTypes();

        Map<String, String> expectedRefCursors = Map.of(
                "T_INV_REF_CURSOR", "T_INVOICE_STG_REC",
                "T_PAY_REF_CURSOR", "T_PAYMENT_STG_REC",
                "T_JNL_REF_CURSOR", "T_JOURNAL_STG_REC"
        );

        for (var e : expectedRefCursors.entrySet()) {
            String name = e.getKey();
            String expectedReturn = e.getValue();

            PlTypeInfo rc = plTypes.values().stream()
                    .filter(pt -> name.equals(pt.getName()) && pt.isRefCursor())
                    .findFirst().orElse(null);

            assertNotNull(rc, "REF CURSOR " + name + " must be registered");
            assertNotNull(rc.getElementTypeName(),
                    name + " (strong-typed) must have elementTypeName set");
            assertTrue(rc.getElementTypeName().toUpperCase().contains(expectedReturn),
                    name + " return type must reference " + expectedReturn
                    + ", got: " + rc.getElementTypeName());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-06: Total DaliPlTypeField count ≥ 180 (sum of RECORD fields)
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(6)
    void tcFin06_plTypeFieldsCount() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        long totalFields = structure.getPlTypes().values().stream()
                .filter(PlTypeInfo::isRecord)
                .mapToLong(pt -> pt.getFields().size())
                .sum();

        // Sum of all RECORD fields: 38+29+40+29+27+17 = 180
        assertTrue(totalFields >= 180,
                "Expected ≥ 180 RECORD fields total; got " + totalFields);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-07: Source schemas — FIN.* and CRM.* tables registered
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(7)
    void tcFin07_sourceTablesRegistered() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Set<String> expectedFinTables = Set.of(
                "INVOICES", "INVOICE_LINES", "PAYMENTS", "PAYMENT_ALLOCATIONS",
                "JOURNALS", "JOURNAL_LINES", "ACCOUNTS", "FISCAL_PERIODS",
                "TAX_CODES", "COST_CENTERS", "PAYMENT_TERMS", "BANK_ACCOUNTS",
                "CURRENCIES", "BUDGET_LINES"
        );
        Set<String> expectedCrmTables = Set.of(
                "CUSTOMERS", "CUSTOMER_SEGMENTS"
        );

        Set<String> tableNames = new HashSet<>();
        for (TableInfo t : structure.getTables().values()) {
            if (t.tableName() != null) tableNames.add(t.tableName().toUpperCase());
        }

        for (String fin : expectedFinTables) {
            assertTrue(tableNames.contains(fin),
                    "FIN." + fin + " must be registered as source. Tables: " + tableNames);
        }
        for (String crm : expectedCrmTables) {
            assertTrue(tableNames.contains(crm),
                    "CRM." + crm + " must be registered as source. Tables: " + tableNames);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-08: DWH target/staging tables registered (≥ 18)
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(8)
    void tcFin08_dwhTargetTablesRegistered() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Set<String> expectedDwhTargets = Set.of(
                "ETL_LOG", "FACT_INVOICES_STG", "FACT_INVOICES",
                "FACT_INVOICE_DAILY_SNAPSHOT", "FACT_INVOICE_CUSTOMER_SUMMARY",
                "FACT_INVOICE_PERIOD_SUMMARY",
                "FACT_PAYMENTS_STG", "FACT_PAYMENTS",
                "FACT_PAYMENT_BANK_SUMMARY", "FACT_PAYMENT_FX_SUMMARY",
                "FACT_JOURNAL_ENTRIES", "FACT_JOURNAL_ACCOUNT_SUMMARY",
                "FACT_JOURNAL_CC_SUMMARY",
                "FACT_BUDGET_VARIANCE", "FACT_BUDGET_CC_ROLLUP",
                "FACT_AGING_TREND",
                "FACT_GL_BALANCE", "FACT_GL_TRIAL_BALANCE",
                "FACT_IC_ENTITY_PAIR_ANALYSIS",
                "FACT_CASH_POSITION", "FACT_RECON_INVOICE_TOTALS"
        );

        Set<String> tableNames = new HashSet<>();
        for (TableInfo t : structure.getTables().values()) {
            if (t.tableName() != null) tableNames.add(t.tableName().toUpperCase());
        }

        Set<String> missing = new HashSet<>(expectedDwhTargets);
        missing.removeAll(tableNames);

        assertTrue(missing.isEmpty(),
                "Missing DWH target tables (pre-recovery boundary): " + missing
                + "\nFound: " + tableNames);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-09: PIPELINED functions detected (isPipelined=true after PIPE ROW walk)
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(9)
    void tcFin09_pipelinedFunctionsDetected() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        long pipelinedCount = structure.getRoutines().values().stream()
                .filter(RoutineInfo::isPipelined)
                .count();
        assertTrue(pipelinedCount >= 2,
                "Expected ≥ 2 PIPELINED functions; got " + pipelinedCount);

        RoutineInfo pipeInv = structure.getRoutines().values().stream()
                .filter(r -> r.getName().toUpperCase().endsWith("PIPE_FACT_INVOICES"))
                .findFirst().orElse(null);
        assertNotNull(pipeInv, "pipe_fact_invoices must be registered");
        assertTrue(pipeInv.isPipelined(),
                "pipe_fact_invoices must have isPipelined=true (PIPE ROW reached)");

        RoutineInfo pipeJnl = structure.getRoutines().values().stream()
                .filter(r -> r.getName().toUpperCase().endsWith("PIPE_FACT_JOURNALS"))
                .findFirst().orElse(null);
        assertNotNull(pipeJnl, "pipe_fact_journals must be registered");
        assertTrue(pipeJnl.isPipelined(),
                "pipe_fact_journals must have isPipelined=true (PIPE ROW reached)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-10: Statements parsed — INSERT/MERGE/UPDATE/SELECT/CTE
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(10)
    void tcFin10_statementsParsed() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Map<String, StatementInfo> stmts = structure.getStatements();
        assertTrue(stmts.size() >= 50,
                "Expected ≥ 50 statements (CTEs, INSERT, MERGE, UPDATE, SELECT); got " + stmts.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-11: MERGE statements detected (≥ 17 MERGE INTO calls in the file)
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(11)
    void tcFin11_mergeStatementsDetected() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        long mergeCount = structure.getStatements().values().stream()
                .filter(s -> "MERGE".equalsIgnoreCase(s.getType()))
                .count();
        // ~16-19 MERGE INTOs across all 7 LOAD_FACT_* + reconcile/calc procedures.
        assertTrue(mergeCount >= 15,
                "Expected ≥ 15 MERGE statements; got " + mergeCount);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-12: BULK COLLECT virtual records materialised (DaliRecord)
    //
    //  Each procedure FETCHes a strong-typed cursor BULK COLLECT INTO v_buffer
    //  (typed e.g. t_invoice_stg_tab); engine should materialise the cursor
    //  variable as DaliRecord/VTABLE with all the record fields injected.
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(12)
    void tcFin12_bulkCollectVirtualRecords() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        // Each LOAD_FACT_* procedure has at least one v_buffer (collection variable)
        // → DaliTable(VTABLE) registered with plTypeGeoid set
        long vtables = structure.getTables().values().stream()
                .filter(t -> "VTABLE".equals(t.tableType()))
                .count();
        assertTrue(vtables >= 1,
                "Expected ≥ 1 VTABLE materialised from BULK COLLECT cursor variables; got " + vtables);

        // VTABLEs come from cursor BULK COLLECT INTO v_buffer where v_buffer
        // is a strong-typed PL/SQL collection (t_invoice_stg_tab etc.) — engine
        // materialises one VTABLE per LOAD_FACT_* procedure cursor.
        assertTrue(vtables >= 3,
                "Expected ≥ 3 VTABLE entries (one per LOAD_FACT_* procedure cursor); got " + vtables);

        // At least one must have plTypeGeoid set (back-ref to T_*_STG_TAB)
        long withPlTypeRef = structure.getTables().values().stream()
                .filter(t -> "VTABLE".equals(t.tableType()) && t.getPlTypeGeoid() != null)
                .count();
        assertTrue(withPlTypeRef >= 1,
                "Expected ≥ 1 VTABLE with plTypeGeoid back-reference to a PlType; got " + withPlTypeRef);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-13: Cross-schema references — FIN, CRM, DWH all visible
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(13)
    void tcFin13_crossSchemaReferences() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        Set<String> seenSchemas = new HashSet<>();
        for (TableInfo t : structure.getTables().values()) {
            String geoid = t.geoid();
            if (geoid == null) continue;
            // geoid format: "SCHEMA.TABLE" or "SCHEMA.TABLE:VTABLE..."
            int dot = geoid.indexOf('.');
            int colon = geoid.indexOf(':');
            if (dot > 0 && (colon < 0 || dot < colon)) {
                seenSchemas.add(geoid.substring(0, dot).toUpperCase());
            }
        }

        assertTrue(seenSchemas.contains("FIN"),
                "FIN schema must be referenced. Seen: " + seenSchemas);
        assertTrue(seenSchemas.contains("CRM"),
                "CRM schema must be referenced. Seen: " + seenSchemas);
        assertTrue(seenSchemas.contains("DWH"),
                "DWH schema must be referenced. Seen: " + seenSchemas);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-FIN-14: PRAGMA AUTONOMOUS_TRANSACTION captured on log_step
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(14)
    void tcFin14_autonomousTransactionPragmaCaptured() {
        assumeTrue(parseSucceeded, "Skipped: TC-FIN-01 did not succeed");

        long autonomousCount = structure.getRoutines().values().stream()
                .filter(RoutineInfo::isAutonomousTransaction)
                .count();

        assertTrue(autonomousCount >= 1,
                "Expected ≥ 1 routine with PRAGMA AUTONOMOUS_TRANSACTION (log_step); got " + autonomousCount);

        // log_step specifically must have it
        RoutineInfo logStep = structure.getRoutines().values().stream()
                .filter(r -> r.getName().toUpperCase().endsWith("LOG_STEP"))
                .findFirst().orElse(null);
        assertNotNull(logStep, "log_step procedure must be registered");
        assertTrue(logStep.isAutonomousTransaction(),
                "log_step must have PRAGMA AUTONOMOUS_TRANSACTION set");
    }
}
