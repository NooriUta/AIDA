package com.hound.semantic;

import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.metrics.PipelineTimer;
import com.hound.storage.ArcadeDBSemanticWriter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * G1/G2 verification: DaliAffectedColumn population, edge counts, DML breakdown.
 *
 * Parses all plsql/ fixtures → REMOTE_BATCH ArcadeDB (hound_test) → runs diagnostic queries.
 * Run: ./gradlew test --tests "*AffectedColumnDbTest*" -Dintegration=true
 */
@Tag("integration")
@EnabledIfSystemProperty(named = "integration", matches = "true")
class AffectedColumnDbTest {

    // ── ArcadeDB connection ──────────────────────────────────────────────────
    private static final String HOST    = "localhost";
    private static final int    PORT    = 2480;
    private static final String USER    = "root";
    private static final String PASS    = "playwithdata";
    private static final String DB_TEST = "hound_test";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        recreateDb();
    }

    // ── Fixture parsing ───────────────────────────────────────────────────────

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

    // ── Main test ─────────────────────────────────────────────────────────────

    @Test
    void affectedColumnReport() throws Exception {
        List<Path> files = fixtures();
        if (files.isEmpty()) {
            System.out.println("[SKIP] no plsql/ fixtures found");
            return;
        }

        // 1. Parse + write into fresh DB via REMOTE_BATCH
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(HOST, PORT, DB_TEST, USER, PASS, true)) {
            for (Path f : files) parseAndSave(f, writer);
        }

        sep("G1/G2 — DaliAffectedColumn Verification Report");
        System.out.printf("  Fixtures parsed: %d%n", files.size());
        files.forEach(f -> System.out.printf("    %s%n", f.getFileName()));

        // ── Query 1: source_type breakdown ────────────────────────────────────
        sep("1. source_type breakdown");
        Map<String, Long> bySource = countBy("SELECT source_type FROM DaliAffectedColumn LIMIT 100000", "source_type");
        long acTotal = bySource.values().stream().mapToLong(l -> l).sum();
        System.out.printf("  Total DaliAffectedColumn rows : %d%n%n", acTotal);
        row("source_type", "count");
        bySource.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> row(e.getKey(), e.getValue()));

        // ── Query 2: type_affect (DML target nodes) ──────────────────────────
        sep("2. type_affect — DML targets only (INSERT/UPDATE/DELETE/null)");
        Map<String, Long> byTypeAffect = countBy("SELECT type_affect FROM DaliAffectedColumn LIMIT 100000", "type_affect");
        row("type_affect", "count");
        byTypeAffect.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> row(e.getKey(), e.getValue()));

        // ── Query 3: INSERT with order_affect bound ───────────────────────────
        sep("3. INSERT with order_affect IS NOT NULL (G2 VALUES binding)");
        long insertBound = scalar("SELECT count(*) AS cnt FROM DaliAffectedColumn WHERE type_affect = 'INSERT' AND order_affect IS NOT NULL");
        long insertTotal = byTypeAffect.getOrDefault("INSERT", 0L);
        System.out.printf("  INSERT total         : %d%n", insertTotal);
        System.out.printf("  INSERT order_affect  : %d%n", insertBound);
        System.out.printf("  Bound %%              : %s%n",
                insertTotal > 0 ? String.format("%.1f%%", insertBound * 100.0 / insertTotal) : "n/a");

        // ── Query 4: HAS_AFFECTED_COL edge count ──────────────────────────────
        sep("4. HAS_AFFECTED_COL edges");
        long edgeCount = scalar("SELECT count(*) AS cnt FROM HAS_AFFECTED_COL");
        System.out.printf("  HAS_AFFECTED_COL     : %d%n", edgeCount);
        System.out.printf("  DaliAffectedColumn   : %d%n", acTotal);
        System.out.printf("  Match                : %s%n",
                edgeCount == acTotal ? "OK" : "MISMATCH (expected equal)");

        // ── Query 5: MERGE SOURCE.* resolution ───────────────────────────────
        sep("5. MERGE SOURCE.* alias resolution (DaliResolutionLog)");
        Map<String, Long> sourceRes = countBy(
                "SELECT result_kind FROM DaliResolutionLog WHERE raw_input LIKE 'SOURCE.%' LIMIT 100000",
                "result_kind");
        if (sourceRes.isEmpty()) {
            System.out.println("  (no SOURCE.* atoms — check pkg_merge_using.pck)");
        } else {
            row("result_kind", "count");
            sourceRes.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> row(e.getKey(), e.getValue()));
            long unresSource = sourceRes.getOrDefault("UNRESOLVED", 0L);
            System.out.printf("%n  Unresolved SOURCE.*  : %d  (expect 0)%n", unresSource);
        }

        // ── Query 6: sanity totals ────────────────────────────────────────────
        sep("6. Sanity totals");
        long stmtCount = scalar("SELECT count(*) AS cnt FROM DaliStatement");
        long atomCount = scalar("SELECT count(*) AS cnt FROM DaliAtom");
        System.out.printf("  DaliStatement        : %d%n", stmtCount);
        System.out.printf("  DaliAtom             : %d%n", atomCount);
        System.out.printf("  DaliAffectedColumn   : %d%n", acTotal);
        System.out.printf("  Avg AC per stmt      : %.1f%n",
                stmtCount > 0 ? acTotal / (double) stmtCount : 0);

        sep("ASSERTIONS");

        assertTrue(acTotal > 0, "DaliAffectedColumn must be non-empty");
        assertEquals(acTotal, edgeCount, "HAS_AFFECTED_COL count must equal DaliAffectedColumn count");
        assertTrue(bySource.containsKey("SELECT") || bySource.containsKey("WHERE")
                        || bySource.containsKey("INSERT") || bySource.containsKey("UPDATE"),
                "Expected at least one of SELECT/WHERE/INSERT/UPDATE in source_type");

        boolean hasMerge = files.stream().anyMatch(f -> f.getFileName().toString().contains("merge"));
        if (hasMerge) {
            long unresSource = sourceRes.getOrDefault("UNRESOLVED", 0L);
            assertEquals(0L, unresSource,
                    "BUG S1.BUG-4 regression: SOURCE.* in MERGE USING must be resolved");
            System.out.println("  SOURCE.* unresolved  : 0  [PASS]");
        }

        System.out.println("\n  All assertions passed.");
    }

    // ── HTTP + JSON helpers ───────────────────────────────────────────────────

    private static String arcadePost(String sql) throws Exception {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
        String escaped = sql.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
        String body = "{\"language\":\"sql\",\"command\":\"" + escaped + "\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/command/" + DB_TEST))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static long scalar(String sql) throws Exception {
        String resp = arcadePost(sql);
        int idx = resp.indexOf("\"cnt\":");
        if (idx < 0) return 0L;
        int s = idx + 6;
        while (s < resp.length() && !Character.isDigit(resp.charAt(s)) && resp.charAt(s) != '-') s++;
        int e = s;
        while (e < resp.length() && (Character.isDigit(resp.charAt(e)) || resp.charAt(e) == '-')) e++;
        try { return Long.parseLong(resp.substring(s, e)); } catch (NumberFormatException ex) { return 0L; }
    }

    private static Map<String, Long> countBy(String sql, String field) throws Exception {
        String resp  = arcadePost(sql);
        Map<String, Long> result = new LinkedHashMap<>();
        String sMarker = "\"" + field + "\":\"";
        String nMarker = "\"" + field + "\":null";
        int pos = 0;
        while (pos < resp.length()) {
            int si = resp.indexOf(sMarker, pos);
            int ni = resp.indexOf(nMarker, pos);
            if (si < 0 && ni < 0) break;
            if (ni >= 0 && (si < 0 || ni < si)) {
                result.merge("(null)", 1L, Long::sum);
                pos = ni + nMarker.length();
            } else {
                int start = si + sMarker.length();
                int end   = resp.indexOf("\"", start);
                if (end > start) result.merge(resp.substring(start, end), 1L, Long::sum);
                pos = end > start ? end + 1 : si + 1;
            }
        }
        return result;
    }

    private static void recreateDb() throws Exception {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
        HttpRequest drop = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/server"))
                .header("Authorization", auth).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"command\":\"drop database " + DB_TEST + "\"}"))
                .build();
        try { HTTP.send(drop, HttpResponse.BodyHandlers.ofString()); } catch (Exception ignored) {}
        HttpRequest create = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/server"))
                .header("Authorization", auth).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"command\":\"create database " + DB_TEST + "\"}"))
                .build();
        String res = HTTP.send(create, HttpResponse.BodyHandlers.ofString()).body();
        if (!res.contains("\"ok\"")) throw new RuntimeException("Failed to create DB " + DB_TEST + ": " + res);
    }

    // ── Print helpers ─────────────────────────────────────────────────────────

    private static void sep(String title) {
        System.out.printf("%n── %s %s%n", title, "─".repeat(Math.max(0, 54 - title.length())));
    }

    private static void row(Object col1, Object col2) {
        System.out.printf("  %-30s %8s%n", col1, col2);
    }
}
