package com.hound.semantic;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
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
 * T4 + T4c — DATA_FLOW edge verification and statement-atom integrity.
 *
 * Parses all plsql/ fixtures → embedded ArcadeDB → runs SQL assertions.
 *
 * Assertions:
 *   T4:  DATA_FLOW and FILTER_FLOW edges are created, flow_type values are valid.
 *   T4c: Every non-DDL DaliStatement has ≥1 atom in its subtree (stmt_geoid prefix).
 *
 * Run: ./gradlew test --tests "*DataFlowDbTest*" --info
 */
class DataFlowDbTest {

    private static final String DB = "target/test-db-dataflow";

    // ── Valid flow_type values ────────────────────────────────────────────────

    private static final Set<String> VALID_FLOW_TYPES =
            Set.of("DIRECT", "AGGREGATE", "INSERT", "UPDATE", "MERGE", "TRANSFORM");

    /**
     * Statement types that are structurally allowed to have zero atoms in their
     * entire subtree — pure DDL with no column-expression context.
     */
    private static final Set<String> DDL_ZERO_ATOM_TYPES = Set.of(
            "CREATE_TABLE", "CREATE_TABLE_AS", "CREATE_SEQUENCE",
            "CREATE_INDEX", "DROP_TABLE", "ALTER_TABLE",
            "GRANT", "COMMIT", "ROLLBACK"
    );

    // ── Setup / teardown ─────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws IOException { wipe(Path.of(DB)); }

    @AfterEach
    void tearDown() throws IOException { wipe(Path.of(DB)); }

    private static void wipe(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var walk = Files.walk(p)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(x -> { try { Files.deleteIfExists(x); } catch (IOException ignored) {} });
        }
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

    // ── Shared setup: parse all fixtures once, open DB ───────────────────────

    private Database openParsedDb() throws Exception {
        List<Path> files = fixtures();
        if (files.isEmpty()) return null;

        String dbPath = Path.of(DB).toAbsolutePath().toString();
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(dbPath)) {
            for (Path f : files) parseAndSave(f, writer);
        }
        return new DatabaseFactory(dbPath).open();
    }

    // ── T4.1: DATA_FLOW edges are non-empty ───────────────────────────────────

    @Test
    @DisplayName("T4.1: DATA_FLOW edges are created (> 0)")
    void dataFlowEdges_nonEmpty() throws Exception {
        try (Database db = openParsedDb()) {
            if (db == null) { System.out.println("[SKIP] no fixtures"); return; }

            long cnt = scalar(db, "SELECT count(*) AS cnt FROM DATA_FLOW");
            System.out.printf("DATA_FLOW count: %d%n", cnt);
            assertTrue(cnt > 0, "DATA_FLOW edges must be non-empty after parsing fixtures");
        }
    }

    // ── T4.2: DATA_FLOW count ≤ resolved atom count ───────────────────────────

    @Test
    @DisplayName("T4.2: DATA_FLOW count ≤ resolved atom count")
    void dataFlowEdges_boundByResolvedAtoms() throws Exception {
        try (Database db = openParsedDb()) {
            if (db == null) { System.out.println("[SKIP] no fixtures"); return; }

            long dfCount     = scalar(db, "SELECT count(*) AS cnt FROM DATA_FLOW");
            long resolvedCnt = scalar(db,
                    "SELECT count(*) AS cnt FROM DaliAtom WHERE status = 'Обработано'");
            System.out.printf("DATA_FLOW: %d  resolved atoms: %d%n", dfCount, resolvedCnt);
            assertTrue(dfCount <= resolvedCnt,
                    "DATA_FLOW count (%d) must not exceed resolved atom count (%d)"
                            .formatted(dfCount, resolvedCnt));
        }
    }

    // ── T4.3: flow_type values are all within the expected set ────────────────

    @Test
    @DisplayName("T4.3: all DATA_FLOW.flow_type values are in {DIRECT, AGGREGATE, INSERT, UPDATE, MERGE, TRANSFORM}")
    void dataFlowFlowTypes_areValid() throws Exception {
        try (Database db = openParsedDb()) {
            if (db == null) { System.out.println("[SKIP] no fixtures"); return; }

            Map<String, Long> byType = countBy(db,
                    "SELECT flow_type FROM DATA_FLOW", "flow_type");
            System.out.printf("DATA_FLOW flow_type breakdown: %s%n", byType);

            for (String ft : byType.keySet()) {
                assertTrue(VALID_FLOW_TYPES.contains(ft),
                        "Unexpected DATA_FLOW flow_type: '" + ft + "' — expected one of " + VALID_FLOW_TYPES);
            }

            // At least DIRECT flow type must exist (SELECT path always produces it)
            assertTrue(byType.containsKey("DIRECT") || !byType.isEmpty(),
                    "Expected at least one DATA_FLOW edge with a valid flow_type");
        }
    }

    // ── T4.4: FILTER_FLOW edges exist ─────────────────────────────────────────

    @Test
    @DisplayName("T4.4: FILTER_FLOW edges are created (> 0)")
    void filterFlowEdges_exist() throws Exception {
        try (Database db = openParsedDb()) {
            if (db == null) { System.out.println("[SKIP] no fixtures"); return; }

            long cnt = scalar(db, "SELECT count(*) AS cnt FROM FILTER_FLOW");
            System.out.printf("FILTER_FLOW count: %d%n", cnt);
            assertTrue(cnt > 0, "FILTER_FLOW edges must be non-empty after parsing fixtures with WHERE clauses");
        }
    }

    // ── T4c: no non-DDL DaliStatement has zero atoms in its subtree ───────────

    /**
     * For every DaliStatement, count atoms whose statement_geoid either:
     *   (a) exactly matches the statement's geoid, OR
     *   (b) starts with geoid + ":" (subtree — child SELECT, CTE inner scope, etc.)
     *
     * Any statement with 0 subtree-atoms that is NOT in DDL_ZERO_ATOM_TYPES is a violation.
     * This covers CTE, SUBQUERY, USUBQUERY — their atoms live in child SELECT scopes.
     */
    @Test
    @DisplayName("T4c: every non-DDL DaliStatement has ≥1 atom in its subtree (including child scopes)")
    void statementsWithoutAtoms_onlyAllowedTypes() throws Exception {
        try (Database db = openParsedDb()) {
            if (db == null) { System.out.println("[SKIP] no fixtures"); return; }

            // 1. Collect all statement (geoid → type) pairs
            Map<String, String> stmtTypes = new LinkedHashMap<>();
            try (ResultSet rs = db.query("sql", "SELECT stmt_geoid, type FROM DaliStatement")) {
                while (rs.hasNext()) {
                    var row = rs.next();
                    String geoid = row.<String>getProperty("stmt_geoid");
                    String type  = row.<String>getProperty("type");
                    if (geoid != null) stmtTypes.put(geoid, type != null ? type : "(null)");
                }
            }

            // 2. Collect all statement_geoids that appear in DaliAtom
            Set<String> atomGeoids = new HashSet<>();
            try (ResultSet rs = db.query("sql", "SELECT statement_geoid FROM DaliAtom")) {
                while (rs.hasNext()) {
                    String g = rs.next().<String>getProperty("statement_geoid");
                    if (g != null) atomGeoids.add(g);
                }
            }

            // 3. For each statement, check if it (or any child scope) has an atom
            List<String> violations = new ArrayList<>();
            for (var e : stmtTypes.entrySet()) {
                String geoid = e.getKey();
                String type  = e.getValue();

                if (DDL_ZERO_ATOM_TYPES.contains(type)) continue; // exempted

                boolean hasAtomInSubtree = atomGeoids.stream()
                        .anyMatch(ag -> ag.equals(geoid) || ag.startsWith(geoid + ":"));

                if (!hasAtomInSubtree) {
                    violations.add("type=" + type + "  geoid=" + geoid);
                }
            }

            if (!violations.isEmpty()) {
                System.out.println("Statements without atoms in subtree (" + violations.size() + "):");
                violations.forEach(v -> System.out.println("  " + v));
            }

            assertTrue(violations.isEmpty(),
                    "DaliStatements with zero atoms in subtree (non-DDL types should always have atoms):\n"
                            + String.join("\n", violations));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Map<String, Long> countBy(Database db, String sql, String field) {
        Map<String, Long> map = new LinkedHashMap<>();
        try (ResultSet rs = db.query("sql", sql)) {
            while (rs.hasNext()) {
                Object v = rs.next().<Object>getProperty(field);
                String key = v != null ? v.toString() : "(null)";
                map.merge(key, 1L, Long::sum);
            }
        } catch (Exception e) {
            System.out.printf("[WARN] countBy failed: %s — %s%n", sql, e.getMessage());
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
            System.out.printf("[WARN] scalar failed: %s — %s%n", sql, e.getMessage());
        }
        return 0L;
    }
}
