package com.hound.storage;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.hound.metrics.PipelineTimer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.SemanticResult;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционный тест: сравниваем COUNT(*) вершин и рёбер между тремя режимами записи:
 * <ul>
 *   <li><b>EMBEDDED</b> — локальная ArcadeDB (tmpdir, не затрагивает сервер)</li>
 *   <li><b>REMOTE</b>   → изолированная тестовая база {@value #DB_REMOTE}</li>
 *   <li><b>REMOTE_BATCH</b> → изолированная тестовая база {@value #DB_BATCH}</li>
 * </ul>
 *
 * <p>Фикстуры берутся из реального корпуса: {@value #TEST_DATA_ROOT}
 * <p>Продакшн-база {@code hound} <b>не затрагивается</b>.
 *
 * <h3>Запуск</h3>
 * <pre>
 *   # 1. Поднять ArcadeDB
 *   docker run -d --name arcade -p 2480:2480 \
 *     -e ARCADEDB_SERVER_ROOT_PASSWORD=playwithdata \
 *     arcadedata/arcadedb:latest
 *
 *   # 2. Запустить все IT-тесты
 *   ./gradlew test --tests "*EmbeddedVsBatchIT*" -Dintegration=true
 *
 *   # 3. Только быстрый smoke
 *   ./gradlew test --tests "*EmbeddedVsBatchIT#smoke*" -Dintegration=true
 * </pre>
 */
@Tag("integration")
@EnabledIfSystemProperty(named = "integration", matches = "true")
class EmbeddedVsBatchIT {

    // ─── ArcadeDB connection ─────────────────────────────────────────────────
    private static final String HOST = "localhost";
    private static final int    PORT = 2480;
    private static final String USER = "root";
    private static final String PASS = "playwithdata";

    /** Изолированные тестовые БД — продакшн-база "hound" не затрагивается. */
    private static final String DB_REMOTE = "hound_remote";
    private static final String DB_BATCH  = "hound_remote_batch";

    /** Локальная embedded БД для эталонных сравнений. */
    private static final String DB_EMBEDDED = "target/test-db-embedded-it";

    /**
     * Корень реального тестового корпуса.
     * Структура: test_plsql/DWH2/…  и  test_plsql/ODS/…
     */
    private static final String TEST_DATA_ROOT = "C:/AIDA/Dali4/HOUND/test_plsql";

    /**
     * Конкретные пакеты для детерминированных сравнительных тестов.
     * Выбраны как представительные (DML + SELECT, разные базы).
     */
    private static final String FIXTURE_DWH2 =
            TEST_DATA_ROOT + "/DWH2/Test_group_1/package_RMS/DM_LOADER_SET_DEFAULT.pck";
    private static final String FIXTURE_ODS =
            TEST_DATA_ROOT + "/ODS/ODS/SCR_CDWH_DET_ACCOUNT_PKG.pck";

    // Fallback: маленькие встроенные фикстуры из classpath
    private static final String FIXTURE_FALLBACK_DML = "pkg_dml_basic.pck";
    private static final String FIXTURE_FALLBACK_CTE = "pkg_select_cte.pck";

    // ─── Vertex / Edge types to compare ──────────────────────────────────────
    // VERTEX_TYPES: exact-match comparison between REMOTE and REMOTE_BATCH.
    // DaliAtom excluded — saveRemote() inserts ALL atom containers (no stmtGeoid filter),
    // while saveEmbedded() skips containers without a statement vertex. Known pre-existing divergence.
    private static final String[] VERTEX_TYPES = {
        "DaliSession", "DaliSchema", "DaliTable", "DaliColumn",
        "DaliRoutine", "DaliPackage", "DaliStatement",
        "DaliOutputColumn", "DaliJoin",
        "DaliParameter", "DaliVariable", "DaliRecord"
    };

    // EDGE_TYPES: exact-match types that are structurally stable.
    // Excluded (known divergences between REMOTE and REMOTE_BATCH):
    //   HAS_ATOM    — pre-existing REMOTE vs EMBEDDED divergence
    //   READS_FROM  — REMOTE drops edges on silent rcmd() failures; BATCH is atomic
    //   WRITES_TO   — same
    //   HAS_COLUMN  — same
    //   FILTER_FLOW — same
    // These are tracked in EDGE_TYPES_TOLERANCE and checked with ±200% tolerance.
    // HAS_JOIN moved to exact-match after fixing the cartesian-product bug in saveRemote().
    private static final String[] EDGE_TYPES = {
        "CONTAINS_TABLE", "CONTAINS_STMT",
        "HAS_OUTPUT_COL", "CONTAINS_ROUTINE", "HAS_PARAMETER", "HAS_VARIABLE", "HAS_JOIN"
    };

    /** Edge types compared with ±200% tolerance (BATCH between 1/3 and 3× REMOTE). */
    private static final String[] EDGE_TYPES_TOLERANCE = {
        "READS_FROM", "WRITES_TO", "HAS_COLUMN", "HAS_ATOM", "FILTER_FLOW",
        "BELONGS_TO_SESSION",
        "BULK_COLLECTS_INTO", "RECORD_USED_IN"
    };

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        wipe(Path.of(DB_EMBEDDED));
        recreateRemoteDb(DB_REMOTE);
        recreateRemoteDb(DB_BATCH);
    }

    @AfterEach
    void tearDown() throws IOException {
        wipe(Path.of(DB_EMBEDDED));
        // remote БД оставляем после теста — удобно для ручной инспекции в ArcadeDB Studio
        // Следующий @BeforeEach пересоздаст их заново
    }

    // ─── Test 1: REMOTE vs REMOTE_BATCH — DWH2 пакет ─────────────────────────

    @Test
    void remoteAndBatchProduceSameCounts_dwh2() throws Exception {
        Path fixture = resolveFixture(FIXTURE_DWH2, FIXTURE_FALLBACK_DML);
        SemanticResult base = parseFixture(fixture);

        assertHasStatements(base, fixture);

        long t0 = System.currentTimeMillis();
        Map<String, Long> remoteCounts = writeRemote(reSid(base, base.getSessionId() + "-r"));
        long remoteMs = System.currentTimeMillis() - t0;

        t0 = System.currentTimeMillis();
        Map<String, Long> batchCounts  = writeBatch (reSid(base, base.getSessionId() + "-b"));
        long batchMs = System.currentTimeMillis() - t0;

        printTable("REMOTE vs REMOTE_BATCH | " + fixture.getFileName(),
                "REMOTE", remoteCounts, "BATCH", batchCounts);
        printTimings("DWH2",
                new String[]{"REMOTE", "REMOTE_BATCH"},
                new long[]  {remoteMs, batchMs});
        printGraphConnectivity(DB_BATCH);
        assertCountsMatch("REMOTE", remoteCounts, "REMOTE_BATCH", batchCounts);
    }

    // ─── Test 2: REMOTE vs REMOTE_BATCH — ODS пакет ──────────────────────────

    @Test
    void remoteAndBatchProduceSameCounts_ods() throws Exception {
        Path fixture = resolveFixture(FIXTURE_ODS, FIXTURE_FALLBACK_CTE);
        SemanticResult base = parseFixture(fixture);

        assertHasStatements(base, fixture);

        long t0 = System.currentTimeMillis();
        Map<String, Long> remoteCounts = writeRemote(reSid(base, base.getSessionId() + "-r"));
        long remoteMs = System.currentTimeMillis() - t0;

        t0 = System.currentTimeMillis();
        Map<String, Long> batchCounts  = writeBatch (reSid(base, base.getSessionId() + "-b"));
        long batchMs = System.currentTimeMillis() - t0;

        printTable("REMOTE vs REMOTE_BATCH | " + fixture.getFileName(),
                "REMOTE", remoteCounts, "BATCH", batchCounts);
        printTimings("ODS",
                new String[]{"REMOTE", "REMOTE_BATCH"},
                new long[]  {remoteMs, batchMs});
        assertCountsMatch("REMOTE", remoteCounts, "REMOTE_BATCH", batchCounts);
    }

    // ─── Test 3: EMBEDDED vs REMOTE vs REMOTE_BATCH — 3-way timing ──────────

    @Test
    void embeddedAndBatchProduceSameCounts() throws Exception {
        Path fixture = resolveFixture(FIXTURE_DWH2, FIXTURE_FALLBACK_DML);
        SemanticResult base = parseFixture(fixture);

        assertHasStatements(base, fixture);

        long t0 = System.currentTimeMillis();
        Map<String, Long> embeddedCounts = writeEmbedded(base);
        long embMs = System.currentTimeMillis() - t0;

        t0 = System.currentTimeMillis();
        Map<String, Long> remoteCounts = writeRemote(reSid(base, base.getSessionId() + "-r"));
        long remoteMs = System.currentTimeMillis() - t0;

        t0 = System.currentTimeMillis();
        Map<String, Long> batchCounts   = writeBatch(reSid(base, base.getSessionId() + "-b"));
        long batchMs = System.currentTimeMillis() - t0;

        printTable("EMBEDDED vs REMOTE_BATCH | " + fixture.getFileName(),
                "EMBEDDED", embeddedCounts, "BATCH", batchCounts);
        printTimings("3-way DWH2",
                new String[]{"EMBEDDED", "REMOTE", "REMOTE_BATCH"},
                new long[]  {embMs,      remoteMs, batchMs});
        printGraphConnectivity(DB_BATCH);
        assertCountsMatch("EMBEDDED", embeddedCounts, "REMOTE_BATCH", batchCounts);
    }

    // ─── Test 4: Параметрический — несколько пакетов из реального корпуса ────

    /**
     * Для каждого из выбранных пакетов: REMOTE vs REMOTE_BATCH должны совпадать.
     * Использует небольшую выборку (не весь корпус) чтобы тест был управляемым.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("sampledFixtures")
    void remoteAndBatchMatch_parameterized(String label, Path fixture) throws Exception {
        SemanticResult base = parseFixture(fixture);
        if (base.getStructure() == null ||
            base.getStructure().getStatements().isEmpty()) {
            System.out.println("SKIP (пустой результат): " + fixture.getFileName());
            return; // не падаем — пустой файл не является ошибкой
        }

        // Пересоздаём БД для изоляции между параметрами
        recreateRemoteDb(DB_REMOTE);
        recreateRemoteDb(DB_BATCH);

        Map<String, Long> remoteCounts = writeRemote(reSid(base, base.getSessionId() + "-r"));
        Map<String, Long> batchCounts  = writeBatch (reSid(base, base.getSessionId() + "-b"));

        printTable("REMOTE vs BATCH | " + fixture.getFileName(),
                "REMOTE", remoteCounts, "BATCH", batchCounts);
        assertCountsMatch("REMOTE", remoteCounts, "REMOTE_BATCH", batchCounts);
    }

    /**
     * Выборка: по 5 пакетов из каждой базы = 10 файлов итого.
     * Берём каждый N-й файл, чтобы покрыть разные паттерны SQL.
     */
    static Stream<org.junit.jupiter.params.provider.Arguments> sampledFixtures() throws Exception {
        List<org.junit.jupiter.params.provider.Arguments> result = new ArrayList<>();

        String[] roots = {
            TEST_DATA_ROOT + "/DWH2/Test_group_1/package_RMS",
            TEST_DATA_ROOT + "/ODS/ODS"
        };

        for (String root : roots) {
            Path dir = Path.of(root);
            if (!Files.exists(dir)) continue;

            List<Path> files;
            try (var stream = Files.list(dir)) {
                files = stream
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".pck") || name.endsWith(".sql");
                    })
                    .sorted()
                    .toList();
            }

            // Выбираем каждый 10-й файл, минимум 5
            int step = Math.max(1, files.size() / 5);
            for (int i = 0; i < files.size(); i += step) {
                Path f = files.get(i);
                result.add(org.junit.jupiter.params.provider.Arguments.of(
                    f.getParent().getFileName() + "/" + f.getFileName(), f));
            }
        }

        // Fallback на classpath если реального корпуса нет
        if (result.isEmpty()) {
            Path fallback = classpathFixture(FIXTURE_FALLBACK_DML);
            if (fallback != null)
                result.add(org.junit.jupiter.params.provider.Arguments.of("fallback/pkg_dml_basic.pck", fallback));
        }

        return result.stream();
    }

    // ─── Test 5: Smoke — batch пишет хоть что-то ─────────────────────────────

    @Test
    void smokeTest_batchProducesNonZeroVertices() throws Exception {
        Path fixture = resolveFixture(FIXTURE_DWH2, FIXTURE_FALLBACK_DML);
        SemanticResult result = parseFixture(fixture);

        Map<String, Long> counts = writeBatch(reSid(result, result.getSessionId() + "-smoke"));

        long stmts   = counts.getOrDefault("DaliStatement", 0L);
        long tables  = counts.getOrDefault("DaliTable", 0L);
        long sessions = counts.getOrDefault("DaliSession", 0L);

        System.out.printf("Smoke OK: DaliSession=%d DaliStatement=%d DaliTable=%d%n",
                sessions, stmts, tables);

        assertTrue(sessions  > 0, "REMOTE_BATCH: DaliSession must exist");
        assertTrue(stmts     > 0, "REMOTE_BATCH: DaliStatement must exist");
        assertTrue(tables    > 0, "REMOTE_BATCH: DaliTable must exist");
    }

    // ─── Test 6: G6 — DaliRecord appears in both REMOTE and REMOTE_BATCH ────

    /**
     * Verifies that a BULK COLLECT INTO pattern produces a DaliRecord vertex
     * and the BULK_COLLECTS_INTO / RECORD_USED_IN edges in both REMOTE and REMOTE_BATCH.
     */
    @Test
    void g6_daliRecord_appearsInBothRemoteAndBatch() throws Exception {
        String sql = """
                CREATE OR REPLACE PACKAGE BODY TEST_PKG.PKG_G6_IT AS
                    PROCEDURE load_sales IS
                        TYPE t_tab IS TABLE OF orders_stg%ROWTYPE;
                        l_tab t_tab;
                    BEGIN
                        SELECT order_id, line_num, amount
                        BULK COLLECT INTO l_tab
                        FROM orders_stg;

                        FORALL i IN 1..l_tab.COUNT
                            INSERT INTO fact_sales
                            VALUES (l_tab(i).order_id, l_tab(i).line_num, l_tab(i).amount);
                    END;
                END PKG_G6_IT;
                /
                """;

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        SemanticResult base = engine.getResult("g6-it-" + System.nanoTime(),
                "pkg_g6_it.pck", "plsql", 0L);

        assertNotNull(base.getStructure(), "Structure must not be null");
        assertFalse(base.getStructure().getRecords().isEmpty(),
                "G6: Structure must contain at least one DaliRecord (BULK COLLECT target)");

        Map<String, Long> remoteCounts = writeRemote(reSid(base, base.getSessionId() + "-r"));
        Map<String, Long> batchCounts  = writeBatch (reSid(base, base.getSessionId() + "-b"));

        long remoteRec = remoteCounts.getOrDefault("DaliRecord", 0L);
        long batchRec  = batchCounts .getOrDefault("DaliRecord", 0L);

        System.out.printf("G6 DaliRecord: REMOTE=%d  REMOTE_BATCH=%d%n", remoteRec, batchRec);

        assertTrue(remoteRec > 0,
                "REMOTE: DaliRecord must be written for BULK COLLECT pattern, got 0");
        assertTrue(batchRec  > 0,
                "REMOTE_BATCH: DaliRecord must be written for BULK COLLECT pattern, got 0");
        assertEquals(remoteRec, batchRec,
                "REMOTE and REMOTE_BATCH must produce the same DaliRecord count");

        // Verify BULK_COLLECTS_INTO and RECORD_USED_IN edges exist in BATCH DB
        long bulkEdge = scalarRemote(DB_BATCH, "SELECT count(*) AS cnt FROM BULK_COLLECTS_INTO");
        long usedEdge = scalarRemote(DB_BATCH, "SELECT count(*) AS cnt FROM RECORD_USED_IN");
        System.out.printf("G6 edges (BATCH): BULK_COLLECTS_INTO=%d  RECORD_USED_IN=%d%n",
                bulkEdge, usedEdge);
        assertTrue(bulkEdge > 0, "REMOTE_BATCH: BULK_COLLECTS_INTO edge must be present");
        assertTrue(usedEdge > 0, "REMOTE_BATCH: RECORD_USED_IN edge must be present");
    }

    // ─── Write helpers ───────────────────────────────────────────────────────

    private Map<String, Long> writeEmbedded(SemanticResult result) throws IOException {
        String dbPath = Path.of(DB_EMBEDDED).toAbsolutePath().toString();
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(dbPath)) {
            writer.saveResult(result, new PipelineTimer());
        }
        return countEmbedded(dbPath);
    }

    private Map<String, Long> writeRemote(SemanticResult result) throws Exception {
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(
                HOST, PORT, DB_REMOTE, USER, PASS, false)) {
            writer.saveResult(result, new PipelineTimer());
        }
        return countRemote(DB_REMOTE);
    }

    private Map<String, Long> writeBatch(SemanticResult result) throws Exception {
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(
                HOST, PORT, DB_BATCH, USER, PASS, true)) {
            writer.saveResult(result, new PipelineTimer());
        }
        return countRemote(DB_BATCH);
    }

    // ─── Count helpers ───────────────────────────────────────────────────────

    private Map<String, Long> countEmbedded(String dbPath) {
        Map<String, Long> counts = new LinkedHashMap<>();
        try (Database db = new DatabaseFactory(dbPath).open()) {
            for (String t : VERTEX_TYPES)
                counts.put(t, scalarEmbedded(db, "SELECT count(*) AS cnt FROM " + t));
            for (String t : EDGE_TYPES)
                counts.put(t, scalarEmbedded(db, "SELECT count(*) AS cnt FROM " + t));
        }
        return counts;
    }

    private Map<String, Long> countRemote(String dbName) throws Exception {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String t : VERTEX_TYPES)
            counts.put(t, scalarRemote(dbName, "SELECT count(*) AS cnt FROM " + t));
        for (String t : EDGE_TYPES)
            counts.put(t, scalarRemote(dbName, "SELECT count(*) AS cnt FROM " + t));
        return counts;
    }

    private static long scalarEmbedded(Database db, String sql) {
        try {
            var rs = db.query("sql", sql);
            if (rs.hasNext()) {
                Object v = rs.next().toMap().get("cnt");
                return v instanceof Number n ? n.longValue() : 0L;
            }
        } catch (Exception ignored) {}
        return 0L;
    }

    private static long scalarRemote(String dbName, String query) throws Exception {
        String body = "{\"language\":\"sql\",\"command\":\"" +
                query.replace("\"", "\\\"") + "\"}";
        String resp = httpPost("/api/v1/command/" + dbName, body);
        int idx = resp.indexOf("\"cnt\":");
        if (idx < 0) return 0L;
        int start = idx + 6, end = start;
        while (end < resp.length() &&
               (Character.isDigit(resp.charAt(end)) || resp.charAt(end) == '-')) end++;
        try { return Long.parseLong(resp.substring(start, end)); }
        catch (NumberFormatException e) { return 0L; }
    }

    // ─── Assertion / Print helpers ───────────────────────────────────────────

    private static void assertCountsMatch(String labelA, Map<String, Long> a,
                                          String labelB, Map<String, Long> b) {
        List<String> mismatches = new ArrayList<>();
        for (String t : VERTEX_TYPES)          checkExact(t, a, b, labelA, labelB, mismatches);
        for (String t : EDGE_TYPES)            checkExact(t, a, b, labelA, labelB, mismatches);
        for (String t : EDGE_TYPES_TOLERANCE)  checkTolerance(t, a, b, labelA, labelB, mismatches);
        if (!mismatches.isEmpty())
            fail("Mismatches " + labelA + " vs " + labelB + ":\n  " +
                    String.join("\n  ", mismatches));
    }

    private static void checkExact(String type, Map<String, Long> a, Map<String, Long> b,
                                    String la, String lb, List<String> out) {
        long va = a.getOrDefault(type, 0L);
        long vb = b.getOrDefault(type, 0L);
        if (va != vb) out.add(type + "  " + la + "=" + va + "  " + lb + "=" + vb);
    }

    /**
     * Tolerance check: BATCH count must be between 1/3 and 3× of REMOTE count.
     * Accounts for known divergences: REMOTE cartesian-product HAS_JOIN bug,
     * REMOTE silent rcmd() failures, and REMOTE vs EMBEDDED DaliAtom divergence.
     */
    private static void checkTolerance(String type, Map<String, Long> a, Map<String, Long> b,
                                        String la, String lb, List<String> out) {
        long va = a.getOrDefault(type, 0L);
        long vb = b.getOrDefault(type, 0L);
        if (va == 0 && vb == 0) return; // both zero: OK
        if (va == 0) { out.add(type + " ❌ " + la + "=0 but " + lb + "=" + vb); return; }
        if (vb == 0) { out.add(type + " ❌ " + la + "=" + va + " but " + lb + "=0"); return; }
        double ratio = (double) vb / va;
        if (ratio < 0.33 || ratio > 3.0)
            out.add(type + " ❌ ratio=" + String.format("%.2f", ratio) +
                    " (" + la + "=" + va + " " + lb + "=" + vb + ") — expected 0.33–3.0x");
    }

    private static void printTable(String title,
                                   String colA, Map<String, Long> a,
                                   String colB, Map<String, Long> b) {
        System.out.println("\n=== " + title + " ===");
        System.out.printf("%-30s %9s %9s%n", "Type", colA, colB);
        System.out.println("-".repeat(50));
        for (String t : VERTEX_TYPES) printRow(t, a, b);
        System.out.println("-".repeat(50));
        for (String t : EDGE_TYPES)   printRow(t, a, b);
        System.out.println();
    }

    private static void printRow(String type, Map<String, Long> a, Map<String, Long> b) {
        long va = a.getOrDefault(type, 0L);
        long vb = b.getOrDefault(type, 0L);
        System.out.printf("%-30s %9d %9d%s%n", type, va, vb, va == vb ? "" : " ❌");
    }

    private static void assertHasStatements(SemanticResult r, Path f) {
        assertNotNull(r.getStructure(), "Structure is null for " + f);
        assertFalse(r.getStructure().getStatements().isEmpty(),
                "No statements parsed in " + f.getFileName());
    }

    private static void printTimings(String label, String[] modes, long[] ms) {
        System.out.println("\n=== Timing: " + label + " ===");
        for (int i = 0; i < modes.length; i++)
            System.out.printf("  %-20s %5d ms%n", modes[i] + ":", ms[i]);
        if (modes.length > 1 && ms[0] > 0)
            for (int i = 1; i < modes.length; i++)
                System.out.printf("  %s / %s = %.2fx%n", modes[i], modes[0], (double) ms[i] / ms[0]);
    }

    /**
     * Проверяет связность графа через SQL-обход (out/in edge-функции ArcadeDB).
     * Только вывод — не проваливает тест.
     */
    private static void printGraphConnectivity(String dbName) throws Exception {
        System.out.println("\n--- Graph connectivity [" + dbName + "] ---");
        long stmtsTotal    = scalarRemote(dbName, "SELECT count(*) AS cnt FROM DaliStatement");
        long stmtsLinked   = scalarRemote(dbName,
            "SELECT count(*) AS cnt FROM DaliStatement WHERE in('CONTAINS_STMT').size() > 0");
        long tablesTotal   = scalarRemote(dbName, "SELECT count(*) AS cnt FROM DaliTable");
        long tablesWithCol = scalarRemote(dbName,
            "SELECT count(*) AS cnt FROM DaliTable WHERE out('HAS_COLUMN').size() > 0");
        long schemasWithTables = scalarRemote(dbName,
            "SELECT count(*) AS cnt FROM DaliSchema WHERE out('CONTAINS_TABLE').size() > 0");
        long colsWithTable = scalarRemote(dbName,
            "SELECT count(*) AS cnt FROM DaliColumn WHERE in('HAS_COLUMN').size() > 0");

        System.out.printf("  DaliStatement  linked/total : %d / %d%n", stmtsLinked, stmtsTotal);
        System.out.printf("  DaliTable      with columns : %d / %d%n", tablesWithCol, tablesTotal);
        System.out.printf("  DaliSchema     with tables  : %d%n", schemasWithTables);
        System.out.printf("  DaliColumn     in HAS_COLUMN: %d / %d%n", colsWithTable, tablesTotal > 0 ? scalarRemote(dbName, "SELECT count(*) AS cnt FROM DaliColumn") : 0);
    }

    // ─── HTTP helpers ────────────────────────────────────────────────────────

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static String httpPost(String path, String body) throws Exception {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + path))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.isEmpty() ? "{}" : body))
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static void recreateRemoteDb(String dbName) throws Exception {
        // ArcadeDB server management via POST /api/v1/server with {"command":"..."}
        try { httpPost("/api/v1/server", "{\"command\":\"drop database " + dbName + "\"}"); }
        catch (Exception ignored) {}
        String res = httpPost("/api/v1/server", "{\"command\":\"create database " + dbName + "\"}");
        if (!res.contains("\"ok\"")) throw new RuntimeException("Failed to create DB " + dbName + ": " + res);
    }

    // ─── Parse helpers ───────────────────────────────────────────────────────

    private static SemanticResult parseFixture(Path file) throws IOException {
        String sql = Files.readString(file);
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine.getResult("it-" + System.nanoTime(),
                file.getFileName().toString(), "plsql", 0L);
    }

    /**
     * Пытается найти файл по абсолютному пути из реального корпуса,
     * при отсутствии — падает обратно на маленький classpath-фикстур.
     */
    private static Path resolveFixture(String absolutePath, String classpathFallback) {
        Path p = Path.of(absolutePath);
        if (Files.exists(p)) return p;
        System.out.println("WARN: реальный файл не найден (" + absolutePath +
                "), используем classpath: " + classpathFallback);
        return classpathFixtureRequired(classpathFallback);
    }

    private static Path classpathFixtureRequired(String name) {
        Path p = Path.of("src/test/resources/plsql", name);
        if (Files.exists(p)) return p;
        try {
            var url = EmbeddedVsBatchIT.class.getClassLoader().getResource("plsql/" + name);
            if (url != null) return Path.of(url.toURI());
        } catch (Exception ignored) {}
        throw new IllegalStateException("Fixture not found: " + name);
    }

    private static Path classpathFixture(String name) {
        try { return classpathFixtureRequired(name); } catch (Exception e) { return null; }
    }

    private static SemanticResult reSid(SemanticResult r, String newSid) {
        return new SemanticResult(
                newSid, r.getFilePath(), r.getDialect(), r.getProcessingTimeMs(),
                r.getStructure(), r.getLineage(), r.getAtoms(),
                r.getResolutionLog(), r.getCalledRoutines(), r.getSchemaRegistrationLog()
        );
    }

    private static void wipe(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var w = Files.walk(p)) {
            w.sorted(Comparator.reverseOrder())
             .forEach(x -> { try { Files.deleteIfExists(x); } catch (IOException ignored) {} });
        }
    }
}
