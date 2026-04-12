package com.hound.semantic;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultSet;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.metrics.PipelineTimer;
import com.hound.storage.ArcadeDBSemanticWriter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * G1/G2 verification: DaliAffectedColumn population, edge counts, DML breakdown.
 *
 * Parses all plsql/ fixtures → embedded ArcadeDB → runs the 4 diagnostic queries.
 * Run: ./gradlew test --tests "*AffectedColumnDbTest*" --info
 */
class AffectedColumnDbTest {

    private static final String DB = "target/test-db-affected-col";

    // ── setup / teardown ─────────────────────────────────────────────

    @BeforeEach
    void setUp() throws IOException {
        wipe(Path.of(DB));
    }

    @AfterEach
    void tearDown() throws IOException {
        wipe(Path.of(DB));
    }

    private static void wipe(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var walk = Files.walk(p)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(x -> { try { Files.deleteIfExists(x); } catch (IOException ignored) {} });
        }
    }

    // ── fixture parsing ──────────────────────────────────────────────

    private static List<Path> fixtures() throws Exception {
        URL url = AffectedColumnDbTest.class.getClassLoader().getResource("plsql");
        if (url == null) return List.of();
        return Files.list(Path.of(url.toURI()))
                .filter(p -> p.getFileName().toString().endsWith(".pck"))
                .sorted()
                .collect(Collectors.toList());
    }

    private static void parseAndSave(Path file, ArcadeDBSemanticWriter writer) throws IOException {
        String sql = Files.readString(file);
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer     = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser   = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        writer.saveResult(engine.getResult(
                "test:" + file.getFileName(), file.getFileName().toString(), "PLSQL", 0L),
                new PipelineTimer(), null, null);
    }

    // ── main test ────────────────────────────────────────────────────

    @Test
    void affectedColumnReport() throws Exception {
        List<Path> files = fixtures();
        if (files.isEmpty()) {
            System.out.println("[SKIP] no plsql/ fixtures found");
            return;
        }

        String dbPath = Path.of(DB).toAbsolutePath().toString();

        // 1. Parse + write into fresh DB
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(dbPath)) {
            for (Path f : files) parseAndSave(f, writer);
        }

        // 2. Re-open for queries (writer closed above)
        try (Database db = new DatabaseFactory(dbPath).open()) {

            sep("G1/G2 — DaliAffectedColumn Verification Report");
            System.out.printf("  Fixtures parsed: %d%n", files.size());
            files.forEach(f -> System.out.printf("    %s%n", f.getFileName()));

            // ── Query 1: source_type breakdown ──────────────────────
            sep("1. source_type breakdown");
            Map<String, Long> bySource = countBy(db,
                    "SELECT source_type FROM DaliAffectedColumn", "source_type");
            long acTotal = bySource.values().stream().mapToLong(l -> l).sum();
            System.out.printf("  Total DaliAffectedColumn rows : %d%n%n", acTotal);
            row("source_type", "count");
            bySource.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> row(e.getKey(), e.getValue()));

            // ── Query 2: type_affect (DML target nodes) ──────────────
            sep("2. type_affect — DML targets only (INSERT/UPDATE/DELETE/null)");
            Map<String, Long> byTypeAffect = countBy(db,
                    "SELECT type_affect FROM DaliAffectedColumn", "type_affect");
            row("type_affect", "count");
            byTypeAffect.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> row(e.getKey(), e.getValue()));

            // ── Query 3: INSERT with order_affect bound ───────────────
            sep("3. INSERT with order_affect IS NOT NULL (G2 VALUES binding)");
            long insertBound = scalar(db,
                    "SELECT count(*) AS cnt FROM DaliAffectedColumn " +
                    "WHERE type_affect = 'INSERT' AND order_affect IS NOT NULL");
            long insertTotal = byTypeAffect.getOrDefault("INSERT", 0L);
            System.out.printf("  INSERT total         : %d%n", insertTotal);
            System.out.printf("  INSERT order_affect  : %d%n", insertBound);
            System.out.printf("  Bound %%              : %s%n",
                    insertTotal > 0 ? String.format("%.1f%%", insertBound * 100.0 / insertTotal) : "n/a");

            // ── Query 4: HAS_AFFECTED_COL edge count ─────────────────
            sep("4. HAS_AFFECTED_COL edges");
            long edgeCount = scalar(db, "SELECT count(*) AS cnt FROM HAS_AFFECTED_COL");
            System.out.printf("  HAS_AFFECTED_COL     : %d%n", edgeCount);
            System.out.printf("  DaliAffectedColumn   : %d%n", acTotal);
            System.out.printf("  Match                : %s%n",
                    edgeCount == acTotal ? "OK" : "MISMATCH (expected equal)");

            // ── Query 5: MERGE SOURCE.* resolution ───────────────────
            sep("5. MERGE SOURCE.* alias resolution (DaliResolutionLog)");
            Map<String, Long> sourceRes = countBy(db,
                    "SELECT result_kind FROM DaliResolutionLog WHERE raw_input LIKE 'SOURCE.%'",
                    "result_kind");
            if (sourceRes.isEmpty()) {
                System.out.println("  (no SOURCE.* atoms — check pkg_merge_using.pck)");
            } else {
                row("result_kind", "count");
                sourceRes.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .forEach(e -> row(e.getKey(), e.getValue()));
                long unresSource = sourceRes.getOrDefault("unresolved", 0L);
                System.out.printf("%n  Unresolved SOURCE.*  : %d  (expect 0)%n", unresSource);
            }

            // ── Query 6: sanity totals ────────────────────────────────
            sep("6. Sanity totals");
            long stmtCount = scalar(db, "SELECT count(*) AS cnt FROM DaliStatement");
            long atomCount = scalar(db, "SELECT count(*) AS cnt FROM DaliAtom");
            System.out.printf("  DaliStatement        : %d%n", stmtCount);
            System.out.printf("  DaliAtom             : %d%n", atomCount);
            System.out.printf("  DaliAffectedColumn   : %d%n", acTotal);
            System.out.printf("  Avg AC per stmt      : %.1f%n",
                    stmtCount > 0 ? acTotal / (double) stmtCount : 0);

            sep("ASSERTIONS");

            // Assertions
            assertTrue(acTotal > 0,
                    "DaliAffectedColumn must be non-empty");
            assertEquals(acTotal, edgeCount,
                    "HAS_AFFECTED_COL count must equal DaliAffectedColumn count");
            assertTrue(bySource.containsKey("SELECT") || bySource.containsKey("WHERE")
                            || bySource.containsKey("INSERT") || bySource.containsKey("UPDATE"),
                    "Expected at least one of SELECT/WHERE/INSERT/UPDATE in source_type");

            // MERGE fixture check
            boolean hasMerge = files.stream().anyMatch(f -> f.getFileName().toString().contains("merge"));
            if (hasMerge) {
                long unresSource = sourceRes.getOrDefault("unresolved", 0L);
                assertEquals(0L, unresSource,
                        "BUG S1.BUG-4 regression: SOURCE.* in MERGE USING must be resolved");
                System.out.println("  SOURCE.* unresolved  : 0  [PASS]");
            }

            System.out.println("\n  All assertions passed.");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────

    private static Map<String, Long> countBy(Database db, String sql, String field) {
        Map<String, Long> map = new LinkedHashMap<>();
        try (ResultSet rs = db.query("sql", sql)) {
            while (rs.hasNext()) {
                Object v = rs.next().<Object>getProperty(field);
                String key = v != null ? v.toString() : "(null)";
                map.merge(key, 1L, Long::sum);
            }
        } catch (Exception e) {
            System.out.printf("  [WARN] query failed: %s — %s%n", sql, e.getMessage());
        }
        return map;
    }

    private static long scalar(Database db, String sql) {
        try (ResultSet rs = db.query("sql", sql)) {
            if (rs.hasNext()) {
                Object v = rs.next().<Object>getProperty("cnt");
                return v instanceof Number n ? n.longValue() : 0L;
            }
        } catch (Exception e) {
            System.out.printf("  [WARN] scalar query failed: %s — %s%n", sql, e.getMessage());
        }
        return 0L;
    }

    private static void sep(String title) {
        System.out.printf("%n── %s %s%n", title, "─".repeat(Math.max(0, 54 - title.length())));
    }

    private static void row(Object col1, Object col2) {
        System.out.printf("  %-30s %8s%n", col1, col2);
    }
}
