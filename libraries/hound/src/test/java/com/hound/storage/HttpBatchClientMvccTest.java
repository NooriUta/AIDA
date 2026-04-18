package com.hound.storage;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MVCC conflict investigation for ArcadeDB HTTP Batch endpoint.
 *
 * <p><b>Production volumes (baseline):</b>
 * <ul>
 *   <li>DaliTable:  1 603 records
 *   <li>DaliColumn: 17 908 records
 *   <li>DaliAtom:   52 131 records
 *   <li>Edges:      ~200 000+ (ATOM_REF_COLUMN, DATA_FLOW, HAS_COLUMN, …)
 * </ul>
 *
 * <p><b>Test groups:</b>
 * <ol>
 *   <li>Unit (T1–T3): verify URL parameters via reflection — no ArcadeDB required.
 *   <li>Integration (T4–T8): require {@code -Dintegration=true} and ArcadeDB on :2480.
 *       <ul>
 *         <li>T4: single session, production-scale batch, {@code parallelFlush=true}
 *         <li>T5: single session, production-scale batch, {@code parallelFlush=false}
 *         <li>T6: high-fan-in payload — many atoms → same DaliColumn (worst-case contention)
 *         <li>T7: 4 concurrent sessions writing simultaneously (multi-session race)
 *         <li>T8: HttpBatchClient retry behaviour on HTTP 500
 *       </ul>
 * </ol>
 *
 * <p>Run integration tests:
 * <pre>
 *   ./gradlew :libraries:hound:test \
 *     --tests "com.hound.storage.HttpBatchClientMvccTest" \
 *     -Dintegration=true
 * </pre>
 */
class HttpBatchClientMvccTest {

    // ── connection ──────────────────────────────────────────────────────────
    private static final String HOST    = "localhost";
    private static final int    PORT    = 2480;
    private static final String USER    = "root";
    private static final String PASS    = "playwithdata";
    private static final String TEST_DB = "hound_mvcc_test";

    // ── production-scale constants ──────────────────────────────────────────
    /** Tables in real hound DB. */
    private static final int N_TABLES   = 1_603;
    /** Columns in real hound DB. */
    private static final int N_COLUMNS  = 17_908;
    /** Atoms in real hound DB. */
    private static final int N_ATOMS    = 52_131;
    /** Average columns per atom reference (DATA_FLOW fan-in approximation). */
    private static final int REFS_PER_ATOM = 2;

    // ── batch size that triggers multiple sub-transactions on server side ──
    /** Small server-side batchSize → many sub-txns → maximum parallelFlush contention. */
    private static final int SERVER_BATCH_SIZE = 500;

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 1 — Unit tests (no ArcadeDB required)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("T1: HttpBatchClient URL contains parallelFlush=true")
    void url_containsParallelFlushTrue() throws Exception {
        HttpBatchClient client = new HttpBatchClient(HOST, PORT, TEST_DB, USER, PASS);
        String url = readPrivateUrl(client);
        System.out.println("Batch URL: " + url);
        assertTrue(url.contains("parallelFlush=true"),
                "parallelFlush=true not found in URL: " + url);
    }

    @Test
    @DisplayName("T2: HttpBatchClient URL contains wal=false")
    void url_containsWalFalse() throws Exception {
        String url = readPrivateUrl(new HttpBatchClient(HOST, PORT, TEST_DB, USER, PASS));
        assertTrue(url.contains("wal=false"), "wal=false not found in URL: " + url);
    }

    @Test
    @DisplayName("T3: HttpBatchClient URL batchSize=100000")
    void url_batchSizeIs100000() throws Exception {
        String url = readPrivateUrl(new HttpBatchClient(HOST, PORT, TEST_DB, USER, PASS));
        assertTrue(url.contains("batchSize=100000"),
                "batchSize=100000 not found in URL: " + url);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 2 — Integration tests
    // ═══════════════════════════════════════════════════════════════════════

    @BeforeEach
    void setup() throws Exception {
        dropDb();
        arcadeServer("create database " + TEST_DB);
        initSchema();
    }

    @AfterEach
    void teardown() throws Exception {
        dropDb();
    }

    // ── T4 ─────────────────────────────────────────────────────────────────

    /**
     * T4: Single session, production-scale batch, {@code parallelFlush=true}.
     *
     * <p>Payload: N_TABLES DaliTable + N_COLUMNS DaliColumn + N_ATOMS DaliAtom
     * + HAS_COLUMN edges + ATOM_REF_COLUMN edges (fan-in 3/column avg)
     * ≈ 200 K records total — matches real hound corpus.
     *
     * <p>Run 5 rounds, recreating DB each round so the same schema is re-inserted.
     * Measures HTTP 5xx rate. If parallelFlush causes MVCC, at least one round fails.
     */
    @Test
    @Tag("integration")
    @EnabledIfSystemProperty(named = "integration", matches = "true")
    @DisplayName("T4: production-scale batch, parallelFlush=true — measure MVCC rate")
    void productionScale_parallelFlushTrue() throws Exception {
        int ROUNDS = 5;
        int errors = 0;
        List<String> errorDetails = new ArrayList<>();

        String url = batchUrl(true, SERVER_BATCH_SIZE);
        System.out.printf("%nT4 URL: %s%n", url);
        System.out.printf("T4 payload: %d tables + %d columns + %d atoms + edges ≈ %d records%n",
                N_TABLES, N_COLUMNS, N_ATOMS, N_TABLES + N_COLUMNS + N_ATOMS + N_COLUMNS + N_ATOMS * REFS_PER_ATOM);

        for (int round = 1; round <= ROUNDS; round++) {
            recreateDb();
            String payload = buildFullPayload("sess-" + round, N_TABLES, N_COLUMNS, N_ATOMS);
            long t0 = System.currentTimeMillis();
            int status = postBatch(url, payload);
            long ms = System.currentTimeMillis() - t0;
            System.out.printf("  round %d/%d → HTTP %d  (%d ms, payload %.1f MB)%n",
                    round, ROUNDS, status, ms, payload.length() / 1_048_576.0);
            if (status >= 500) {
                errors++;
                errorDetails.add("round " + round + " → HTTP " + status);
            }
        }

        System.out.printf("T4 result: %d/%d rounds failed%n%n", errors, ROUNDS);
        // Report, do not assert — this is a measurement test
        System.out.printf("T4 CONCLUSION: parallelFlush=true failure rate = %d/%d%n", errors, ROUNDS);
    }

    // ── T5 ─────────────────────────────────────────────────────────────────

    /**
     * T5: Same payload as T4 but with {@code parallelFlush=false}.
     *
     * <p>Expected: 0 errors. If T4 shows failures and T5 shows 0, parallelFlush=true
     * is confirmed as the cause. If both show 0, problem is elsewhere.
     */
    @Test
    @Tag("integration")
    @EnabledIfSystemProperty(named = "integration", matches = "true")
    @DisplayName("T5: production-scale batch, parallelFlush=false — baseline comparison")
    void productionScale_parallelFlushFalse() throws Exception {
        int ROUNDS = 5;
        int errors = 0;

        String url = batchUrl(false, SERVER_BATCH_SIZE);
        System.out.printf("%nT5 URL: %s%n", url);

        for (int round = 1; round <= ROUNDS; round++) {
            recreateDb();
            String payload = buildFullPayload("sess-" + round, N_TABLES, N_COLUMNS, N_ATOMS);
            long t0 = System.currentTimeMillis();
            int status = postBatch(url, payload);
            long ms = System.currentTimeMillis() - t0;
            System.out.printf("  round %d/%d → HTTP %d  (%d ms)%n", round, ROUNDS, status, ms);
            if (status >= 500) errors++;
        }

        System.out.printf("T5 result: %d/%d rounds failed%n%n", errors, ROUNDS);
        System.out.printf("T5 CONCLUSION: parallelFlush=false failure rate = %d/%d%n", errors, ROUNDS);
        assertEquals(0, errors,
                "parallelFlush=false should produce 0 errors but got " + errors);
    }

    // ── T6 ─────────────────────────────────────────────────────────────────

    /**
     * T6: Worst-case fan-in — all atoms reference the SAME 100 DaliColumn vertices.
     *
     * <p>With 52 131 atoms each generating 2 ATOM_REF_COLUMN edges to only 100 columns,
     * every column vertex receives ~1 043 edge back-references. This maximises B-tree
     * page contention when ArcadeDB flushes those vertex pages in parallel.
     *
     * <p>Compare error rate with parallelFlush=true vs false.
     */
    @Test
    @Tag("integration")
    @EnabledIfSystemProperty(named = "integration", matches = "true")
    @DisplayName("T6: high fan-in (52K atoms → 100 columns) — worst-case page contention")
    void highFanIn_parallelFlushComparison() throws Exception {
        int HOT_COLUMNS = 100;   // only 100 "popular" columns — all atoms reference them
        int ROUNDS      = 5;

        System.out.printf("%nT6: %d atoms × 2 edges → %d hot columns (fan-in ~%d per col)%n",
                N_ATOMS, HOT_COLUMNS, N_ATOMS * 2 / HOT_COLUMNS);

        int errorsTrue  = 0;
        int errorsFalse = 0;

        for (int round = 1; round <= ROUNDS; round++) {
            recreateDb();
            // All atoms fan into the same HOT_COLUMNS columns
            String payload = buildHighFanInPayload("sess-hot-" + round, HOT_COLUMNS, N_ATOMS);

            // Test with parallelFlush=true
            int s1 = postBatch(batchUrl(true,  SERVER_BATCH_SIZE), payload);
            if (s1 >= 500) { errorsTrue++;  System.out.printf("  round %d parallelFlush=true  → HTTP %d%n", round, s1); }

            // Recreate DB and test with parallelFlush=false
            recreateDb();
            int s2 = postBatch(batchUrl(false, SERVER_BATCH_SIZE), payload);
            if (s2 >= 500) { errorsFalse++; System.out.printf("  round %d parallelFlush=false → HTTP %d%n", round, s2); }

            System.out.printf("  round %d: pF=true→%d  pF=false→%d%n", round, s1, s2);
        }

        System.out.printf("T6 CONCLUSION: parallelFlush=true errors=%d/%d  parallelFlush=false errors=%d/%d%n%n",
                errorsTrue, ROUNDS, errorsFalse, ROUNDS);
    }

    // ── T7 ─────────────────────────────────────────────────────────────────

    /**
     * T7: 4 concurrent sessions writing simultaneously.
     *
     * <p>Simulates a JobRunr worker pool with 4 parallel parse jobs, each sending
     * its own batch to the same ArcadeDB. Sessions share the same canonical schema
     * (same DaliColumn geoids) — the pre-insert {@code rcmd()} calls from one session
     * may conflict with another session's batch writing edges to those same columns.
     *
     * <p>Total load: 4 sessions × (N_TABLES/4 + N_COLUMNS/4 + N_ATOMS/4) ≈ production volume.
     */
    @Test
    @Tag("integration")
    @EnabledIfSystemProperty(named = "integration", matches = "true")
    @DisplayName("T7: 4 concurrent sessions → race condition on shared DaliColumn pages")
    void concurrentSessions_parallelFlushTrue() throws Exception {
        int SESSIONS        = 4;
        int TABLES_PER_SES  = N_TABLES  / SESSIONS;
        int COLUMNS_PER_SES = N_COLUMNS / SESSIONS;
        int ATOMS_PER_SES   = N_ATOMS   / SESSIONS;
        int CONCURRENT_RUNS = 3;   // repeat the 4-concurrent-session scenario N times

        String url = batchUrl(true, SERVER_BATCH_SIZE);
        System.out.printf("%nT7: %d concurrent sessions × %d tables/%d cols/%d atoms = %.0f total records per run%n",
                SESSIONS, TABLES_PER_SES, COLUMNS_PER_SES, ATOMS_PER_SES,
                SESSIONS * (double)(TABLES_PER_SES + COLUMNS_PER_SES + ATOMS_PER_SES
                        + COLUMNS_PER_SES + ATOMS_PER_SES * REFS_PER_ATOM));

        AtomicInteger totalErrors = new AtomicInteger(0);

        for (int run = 1; run <= CONCURRENT_RUNS; run++) {
            recreateDb();
            ExecutorService pool = Executors.newFixedThreadPool(SESSIONS);
            CountDownLatch  ready = new CountDownLatch(SESSIONS);
            CountDownLatch  start = new CountDownLatch(1);
            List<Future<Integer>> futures = new ArrayList<>();

            final int finalRun = run;
            for (int s = 0; s < SESSIONS; s++) {
                final int sid = s;
                futures.add(pool.submit(() -> {
                    String payload = buildFullPayload(
                            "concurrent-run" + finalRun + "-sess" + sid,
                            TABLES_PER_SES, COLUMNS_PER_SES, ATOMS_PER_SES);
                    ready.countDown();
                    start.await();                       // all threads fire simultaneously
                    return postBatch(url, payload);
                }));
            }

            ready.await();  // all threads are ready
            start.countDown();  // release all at once

            int runErrors = 0;
            for (int s = 0; s < SESSIONS; s++) {
                int status = futures.get(s).get(120, TimeUnit.SECONDS);
                if (status >= 500) {
                    runErrors++;
                    System.out.printf("  run %d sess %d → HTTP %d%n", run, s, status);
                }
            }
            pool.shutdown();
            totalErrors.addAndGet(runErrors);
            System.out.printf("  run %d: %d/%d sessions failed%n", run, runErrors, SESSIONS);
        }

        int total = SESSIONS * CONCURRENT_RUNS;
        System.out.printf("T7 CONCLUSION: %d/%d concurrent session writes failed (MVCC race condition)%n%n",
                totalErrors.get(), total);
    }

    // ── T8 ─────────────────────────────────────────────────────────────────

    /**
     * T8: HttpBatchClient retry behaviour — confirms existing safety net.
     *
     * <p>Sends an invalid payload (unknown type). ArcadeDB returns HTTP 500.
     * HttpBatchClient must retry exactly 3 times then throw {@code RuntimeException}.
     */
    @Test
    @Tag("integration")
    @EnabledIfSystemProperty(named = "integration", matches = "true")
    @DisplayName("T8: HttpBatchClient retries HTTP 500 exactly 3 times then throws")
    void httpBatchClient_retriesOnHttp500_thenThrows() {
        HttpBatchClient client = new HttpBatchClient(HOST, PORT, TEST_DB, USER, PASS);
        String badPayload = "{\"@type\":\"vertex\",\"@class\":\"NonExistentType_T8\",\"x\":1}\n";

        long t0 = System.currentTimeMillis();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.send(badPayload, "t8-retry-sid"));
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("T8: threw after %d ms: %s%n", ms, ex.getMessage());
        assertTrue(ex.getMessage().contains("Batch failed after"),
                "Expected 'Batch failed after N attempts', got: " + ex.getMessage());
        // 3 retries with 1s + 2s backoff → at least 3 s elapsed
        assertTrue(ms >= 3_000,
                "Expected ≥3s for 3 retry attempts (1s+2s backoff), got: " + ms + "ms");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Payload builders
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Build a full production-scale NDJSON payload:
     * <ol>
     *   <li>nTables DaliTable vertices
     *   <li>nColumns DaliColumn vertices (distributed across tables)
     *   <li>nAtoms DaliAtom vertices
     *   <li>HAS_COLUMN edges (table → column)
     *   <li>ATOM_REF_COLUMN edges (atom → column, fan-in ~REFS_PER_ATOM)
     * </ol>
     */
    private static String buildFullPayload(String sessionId,
                                           int nTables, int nColumns, int nAtoms) {
        // Average columns per table
        int colsPerTable = Math.max(1, nColumns / nTables);

        StringBuilder sb = new StringBuilder(nColumns * 250 + nAtoms * 200);

        // ── Tables ────────────────────────────────────────────────────────
        for (int t = 0; t < nTables; t++) {
            String tblId = sessionId + ".schema.tbl_" + t;
            sb.append("{\"@type\":\"vertex\",\"@class\":\"DaliTable\"")
              .append(",\"@id\":\"").append(tblId).append('"')
              .append(",\"db_name\":\"testdb\"")
              .append(",\"table_geoid\":\"").append(tblId).append('"')
              .append(",\"table_name\":\"tbl_").append(t).append('"')
              .append(",\"schema_geoid\":\"schema\"")
              .append(",\"table_type\":\"TABLE\"")
              .append(",\"data_source\":\"RECONSTRUCTED\"")
              .append(",\"column_count\":").append(colsPerTable)
              .append("}\n");
        }

        // ── Columns ───────────────────────────────────────────────────────
        for (int c = 0; c < nColumns; c++) {
            int tblIdx = Math.min(c / colsPerTable, nTables - 1);  // guard overflow on last table
            String tblId = sessionId + ".schema.tbl_" + tblIdx;
            String colId = sessionId + ".schema.tbl_" + tblIdx + ".col_" + c;
            sb.append("{\"@type\":\"vertex\",\"@class\":\"DaliColumn\"")
              .append(",\"@id\":\"").append(colId).append('"')
              .append(",\"db_name\":\"testdb\"")
              .append(",\"column_geoid\":\"").append(colId).append('"')
              .append(",\"column_name\":\"col_").append(c).append('"')
              .append(",\"table_geoid\":\"").append(tblId).append('"')
              .append(",\"session_id\":\"").append(sessionId).append('"')
              .append(",\"data_source\":\"RECONSTRUCTED\"")
              .append(",\"is_output\":false,\"is_pk\":false,\"is_fk\":false")
              .append("}\n");
        }

        // ── Atoms ─────────────────────────────────────────────────────────
        for (int a = 0; a < nAtoms; a++) {
            String atomId = sessionId + ".atom_" + a;
            sb.append("{\"@type\":\"vertex\",\"@class\":\"DaliAtom\"")
              .append(",\"@id\":\"").append(atomId).append('"')
              .append(",\"session_id\":\"").append(sessionId).append('"')
              .append(",\"atom_geoid\":\"").append(atomId).append('"')
              .append(",\"status\":\"Обработано\"")
              .append("}\n");
        }

        // ── HAS_COLUMN edges (table → column) ─────────────────────────────
        for (int c = 0; c < nColumns; c++) {
            int tblIdx = Math.min(c / colsPerTable, nTables - 1);  // mirror column assignment
            String tblId = sessionId + ".schema.tbl_" + tblIdx;
            String colId = sessionId + ".schema.tbl_" + tblIdx + ".col_" + c;
            sb.append("{\"@type\":\"edge\",\"@class\":\"HAS_COLUMN\"")
              .append(",\"@from\":\"").append(tblId).append('"')
              .append(",\"@to\":\"").append(colId).append('"')
              .append("}\n");
        }

        // ── ATOM_REF_COLUMN edges (atom → column, distributed) ────────────
        for (int a = 0; a < nAtoms; a++) {
            String atomId = sessionId + ".atom_" + a;
            for (int r = 0; r < REFS_PER_ATOM; r++) {
                int colIdx = (a * REFS_PER_ATOM + r) % nColumns;
                int tblIdx = Math.min(colIdx / colsPerTable, nTables - 1);  // mirror column assignment
                String colId = sessionId + ".schema.tbl_" + tblIdx + ".col_" + colIdx;
                sb.append("{\"@type\":\"edge\",\"@class\":\"ATOM_REF_COLUMN\"")
                  .append(",\"@from\":\"").append(atomId).append('"')
                  .append(",\"@to\":\"").append(colId).append('"')
                  .append("}\n");
            }
        }

        return sb.toString();
    }

    /**
     * Build a high-fan-in payload: nAtoms atoms all referencing the same hotColumns columns.
     * Creates maximum B-tree page contention per column vertex.
     * Fan-in per column = nAtoms * REFS_PER_ATOM / hotColumns.
     */
    private static String buildHighFanInPayload(String sessionId, int hotColumns, int nAtoms) {
        StringBuilder sb = new StringBuilder(nAtoms * 250);

        // Hot columns
        for (int c = 0; c < hotColumns; c++) {
            String colId = sessionId + ".hot_col_" + c;
            sb.append("{\"@type\":\"vertex\",\"@class\":\"DaliColumn\"")
              .append(",\"@id\":\"").append(colId).append('"')
              .append(",\"db_name\":\"testdb\"")
              .append(",\"column_geoid\":\"").append(colId).append('"')
              .append(",\"column_name\":\"col_").append(c).append('"')
              .append(",\"table_geoid\":\"hot_table\"")
              .append(",\"session_id\":\"").append(sessionId).append('"')
              .append(",\"data_source\":\"MASTER\"")
              .append(",\"is_output\":false,\"is_pk\":false,\"is_fk\":false")
              .append("}\n");
        }

        // Atoms
        for (int a = 0; a < nAtoms; a++) {
            String atomId = sessionId + ".atom_" + a;
            sb.append("{\"@type\":\"vertex\",\"@class\":\"DaliAtom\"")
              .append(",\"@id\":\"").append(atomId).append('"')
              .append(",\"session_id\":\"").append(sessionId).append('"')
              .append(",\"atom_geoid\":\"").append(atomId).append('"')
              .append(",\"status\":\"Обработано\"")
              .append("}\n");
        }

        // ATOM_REF_COLUMN — all atoms fan into the same hotColumns
        for (int a = 0; a < nAtoms; a++) {
            String atomId = sessionId + ".atom_" + a;
            for (int r = 0; r < REFS_PER_ATOM; r++) {
                String colId = sessionId + ".hot_col_" + ((a * REFS_PER_ATOM + r) % hotColumns);
                sb.append("{\"@type\":\"edge\",\"@class\":\"ATOM_REF_COLUMN\"")
                  .append(",\"@from\":\"").append(atomId).append('"')
                  .append(",\"@to\":\"").append(colId).append('"')
                  .append("}\n");
            }
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Infrastructure helpers
    // ═══════════════════════════════════════════════════════════════════════

    private String batchUrl(boolean parallelFlush, int batchSize) {
        return String.format(
                "http://%s:%d/api/v1/batch/%s?lightEdges=true&wal=false&parallelFlush=%s&batchSize=%d",
                HOST, PORT, TEST_DB, parallelFlush, batchSize);
    }

    private int postBatch(String url, String payload) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/x-ndjson")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            String body = resp.body();
            System.out.printf("    HTTP %d: %.300s%n", resp.statusCode(),
                    body.length() > 300 ? body.substring(0, 300) + "…" : body);
        }
        return resp.statusCode();
    }

    /** Send a SQL command to the test DB (DELETE, schema creation, etc.). */
    private void dbCommand(String sql) throws Exception {
        String body = "{\"language\":\"sql\",\"command\":" + jsonStr(sql) + "}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/command/" + TEST_DB))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    }

    /** Send a server-level command (create/drop database). */
    private static void arcadeServer(String command) throws Exception {
        String body = "{\"command\":" + jsonStr(command) + "}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/server"))
                .header("Authorization", staticBasicAuth())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400 && !resp.body().contains("already exists")
                && !resp.body().contains("does not exist")) {
            System.out.printf("arcadeServer '%s' → %d: %s%n",
                    command, resp.statusCode(), resp.body());
        }
    }

    private void dropDb() throws Exception {
        try { arcadeServer("drop database " + TEST_DB); } catch (Exception ignored) {}
    }

    private void recreateDb() throws Exception {
        dropDb();
        arcadeServer("create database " + TEST_DB);
        initSchema();
    }

    private void initSchema() throws Exception {
        // Minimal schema matching production DaliColumn / DaliAtom / DaliTable
        String[] cmds = {
            "CREATE VERTEX TYPE DaliTable   IF NOT EXISTS",
            "CREATE VERTEX TYPE DaliColumn  IF NOT EXISTS",
            "CREATE VERTEX TYPE DaliAtom    IF NOT EXISTS",
            "CREATE EDGE   TYPE HAS_COLUMN       IF NOT EXISTS",
            "CREATE EDGE   TYPE ATOM_REF_COLUMN  IF NOT EXISTS",
            "CREATE PROPERTY DaliColumn.db_name       IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliColumn.column_geoid  IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliColumn.column_name   IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliColumn.table_geoid   IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliColumn.session_id    IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliColumn.data_source   IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliColumn.is_output     IF NOT EXISTS BOOLEAN",
            "CREATE PROPERTY DaliColumn.is_pk         IF NOT EXISTS BOOLEAN",
            "CREATE PROPERTY DaliColumn.is_fk         IF NOT EXISTS BOOLEAN",
            "CREATE PROPERTY DaliAtom.session_id      IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliAtom.atom_geoid      IF NOT EXISTS STRING",
            "CREATE PROPERTY DaliAtom.status          IF NOT EXISTS STRING",
            // The same UNIQUE_HASH index as production — critical for reproducing page contention
            "CREATE INDEX IF NOT EXISTS ON DaliColumn (db_name, column_geoid) UNIQUE_HASH NULL_STRATEGY SKIP"
        };
        for (String cmd : cmds) dbCommand(cmd);
    }

    private String basicAuth() { return staticBasicAuth(); }

    private static String staticBasicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
    }

    private static String jsonStr(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String readPrivateUrl(HttpBatchClient client) throws Exception {
        Field f = HttpBatchClient.class.getDeclaredField("baseUrl");
        f.setAccessible(true);
        return (String) f.get(client);
    }
}
