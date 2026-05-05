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
 * T4 + T4c — DATA_FLOW edge verification and statement-atom integrity.
 *
 * Parses all plsql/ fixtures → REMOTE_BATCH ArcadeDB (hound_test) → runs SQL assertions.
 * Run: ./gradlew test --tests "*DataFlowDbTest*" -Dintegration=true
 *
 * Assertions:
 *   T4:  DATA_FLOW and FILTER_FLOW edges are created, flow_type values are valid.
 *   T4c: Every non-DDL DaliStatement has ≥1 atom in its subtree (stmt_geoid prefix).
 */
@Tag("integration")
@EnabledIfSystemProperty(named = "integration", matches = "true")
class DataFlowDbTest {

    // ── ArcadeDB connection ──────────────────────────────────────────────────
    private static final String HOST    = "localhost";
    private static final int    PORT    = 2480;
    private static final String USER    = "root";
    private static final String PASS    = "playwithdata";
    private static final String DB_TEST = "hound_test";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    // ── Valid flow_type values ─────────────────────────────────────────────────
    private static final Set<String> VALID_FLOW_TYPES =
            Set.of("DIRECT", "AGGREGATE", "INSERT", "UPDATE", "MERGE", "TRANSFORM");

    private static final Set<String> DDL_ZERO_ATOM_TYPES = Set.of(
            "CREATE_TABLE", "CREATE_TABLE_AS", "CREATE_SEQUENCE",
            "CREATE_INDEX", "DROP_TABLE", "ALTER_TABLE",
            "GRANT", "COMMIT", "ROLLBACK"
    );

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        recreateDb();
    }

    // ── Fixture parsing ───────────────────────────────────────────────────────

    private static List<Path> fixtures() throws Exception {
        URL url = DataFlowDbTest.class.getClassLoader().getResource("plsql");
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
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        writer.saveResult(engine.getResult(
                "test:" + file.getFileName(), file.getFileName().toString(), "PLSQL", 0L),
                new PipelineTimer(), null, null);
    }

    /** Parse all fixtures and write via REMOTE_BATCH. Returns false if no fixtures. */
    private boolean parseAllFixtures() throws Exception {
        List<Path> files = fixtures();
        if (files.isEmpty()) return false;
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(HOST, PORT, DB_TEST, USER, PASS, true)) {
            for (Path f : files) parseAndSave(f, writer);
        }
        return true;
    }

    // ── T4.1: DATA_FLOW edges are non-empty ──────────────────────────────────

    @Test
    @DisplayName("T4.1: DATA_FLOW edges are created (> 0)")
    void dataFlowEdges_nonEmpty() throws Exception {
        if (!parseAllFixtures()) { System.out.println("[SKIP] no fixtures"); return; }
        long cnt = scalar("SELECT count(*) AS cnt FROM DATA_FLOW");
        System.out.printf("DATA_FLOW count: %d%n", cnt);
        assertTrue(cnt > 0, "DATA_FLOW edges must be non-empty after parsing fixtures");
    }

    // ── T4.2: DATA_FLOW count ≤ resolved atom count ──────────────────────────

    @Test
    @DisplayName("T4.2: DATA_FLOW count ≤ resolved atom count")
    void dataFlowEdges_boundByResolvedAtoms() throws Exception {
        if (!parseAllFixtures()) { System.out.println("[SKIP] no fixtures"); return; }
        long dfCount     = scalar("SELECT count(*) AS cnt FROM DATA_FLOW");
        long resolvedCnt = scalar("SELECT count(*) AS cnt FROM DaliAtom WHERE coalesce(primary_status, status) IN ['RESOLVED', 'Обработано']");
        System.out.printf("DATA_FLOW: %d  resolved atoms: %d%n", dfCount, resolvedCnt);
        assertTrue(dfCount <= resolvedCnt,
                "DATA_FLOW count (%d) must not exceed resolved atom count (%d)".formatted(dfCount, resolvedCnt));
    }

    // ── T4.3: flow_type values are all within the expected set ────────────────

    @Test
    @DisplayName("T4.3: all DATA_FLOW.flow_type values are in {DIRECT, AGGREGATE, INSERT, UPDATE, MERGE, TRANSFORM}")
    void dataFlowFlowTypes_areValid() throws Exception {
        if (!parseAllFixtures()) { System.out.println("[SKIP] no fixtures"); return; }
        Map<String, Long> byType = countBy("SELECT flow_type FROM DATA_FLOW LIMIT 100000", "flow_type");
        System.out.printf("DATA_FLOW flow_type breakdown: %s%n", byType);
        for (String ft : byType.keySet()) {
            assertTrue(VALID_FLOW_TYPES.contains(ft),
                    "Unexpected DATA_FLOW flow_type: '" + ft + "' — expected one of " + VALID_FLOW_TYPES);
        }
        assertTrue(byType.containsKey("DIRECT") || !byType.isEmpty(),
                "Expected at least one DATA_FLOW edge with a valid flow_type");
    }

    // ── T4.4: FILTER_FLOW edges exist ─────────────────────────────────────────

    @Test
    @DisplayName("T4.4: FILTER_FLOW edges are created (> 0)")
    void filterFlowEdges_exist() throws Exception {
        if (!parseAllFixtures()) { System.out.println("[SKIP] no fixtures"); return; }
        long cnt = scalar("SELECT count(*) AS cnt FROM FILTER_FLOW");
        System.out.printf("FILTER_FLOW count: %d%n", cnt);
        assertTrue(cnt > 0, "FILTER_FLOW edges must be non-empty after parsing fixtures with WHERE clauses");
    }

    // ── T4c: no non-DDL DaliStatement has zero atoms in its subtree ───────────

    @Test
    @DisplayName("T4c: every non-DDL DaliStatement has ≥1 atom in its subtree (including child scopes)")
    void statementsWithoutAtoms_onlyAllowedTypes() throws Exception {
        if (!parseAllFixtures()) { System.out.println("[SKIP] no fixtures"); return; }

        // 1. Collect all statement (geoid → type) pairs
        Map<String, String> stmtTypes = new LinkedHashMap<>();
        String stmtResp = arcadePost("SELECT stmt_geoid, type FROM DaliStatement LIMIT 1000000");
        for (String rowJson : resultRows(stmtResp)) {
            String geoid = getField(rowJson, "stmt_geoid");
            String type  = getField(rowJson, "type");
            if (geoid != null) stmtTypes.put(geoid, type != null ? type : "(null)");
        }

        // 2. Collect all statement_geoids that appear in DaliAtom
        Set<String> atomGeoids = new HashSet<>();
        String atomResp = arcadePost("SELECT statement_geoid FROM DaliAtom WHERE statement_geoid IS NOT NULL LIMIT 1000000");
        String atomMarker = "\"statement_geoid\":\"";
        int pos = 0;
        while ((pos = atomResp.indexOf(atomMarker, pos)) >= 0) {
            int s = pos + atomMarker.length();
            int e = atomResp.indexOf("\"", s);
            if (e > s) atomGeoids.add(atomResp.substring(s, e));
            pos = e > s ? e + 1 : pos + 1;
        }

        // 3. For each statement, check if it (or any child scope) has an atom
        List<String> violations = new ArrayList<>();
        for (var entry : stmtTypes.entrySet()) {
            String geoid = entry.getKey();
            String type  = entry.getValue();
            if (DDL_ZERO_ATOM_TYPES.contains(type)) continue;
            boolean hasAtomInSubtree = atomGeoids.stream()
                    .anyMatch(ag -> ag.equals(geoid) || ag.startsWith(geoid + ":"));
            if (!hasAtomInSubtree) violations.add("type=" + type + "  geoid=" + geoid);
        }

        if (!violations.isEmpty()) {
            System.out.println("Statements without atoms in subtree (" + violations.size() + "):");
            violations.forEach(v -> System.out.println("  " + v));
        }

        assertTrue(violations.isEmpty(),
                "DaliStatements with zero atoms in subtree (non-DDL types should always have atoms):\n"
                        + String.join("\n", violations));
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

    /** Split ArcadeDB response into individual row JSON strings. */
    private static List<String> resultRows(String resp) {
        List<String> rows = new ArrayList<>();
        int start = resp.indexOf("\"result\":[");
        if (start < 0) return rows;
        start += 10;
        int depth = 0, rowStart = -1;
        for (int i = start; i < resp.length(); i++) {
            char c = resp.charAt(i);
            if (c == '{') { if (depth == 0) rowStart = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0 && rowStart >= 0) { rows.add(resp.substring(rowStart, i + 1)); rowStart = -1; } }
        }
        return rows;
    }

    /** Extract a string field value from a single result row JSON. */
    private static String getField(String rowJson, String field) {
        String marker = "\"" + field + "\":\"";
        int idx = rowJson.indexOf(marker);
        if (idx < 0) return null;
        int start = idx + marker.length();
        int end   = rowJson.indexOf("\"", start);
        return end > start ? rowJson.substring(start, end) : null;
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
}
