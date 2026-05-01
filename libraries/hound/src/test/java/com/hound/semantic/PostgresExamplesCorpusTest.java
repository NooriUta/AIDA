package com.hound.semantic;

import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLLexer;
import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLParser;
import com.hound.semantic.dialect.postgresql.PostgreSQLSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-HOUND-PG-CORPUS-01..N
 * Baseline test for the ANTLR4 grammars-v4 PostgreSQL example corpus shipped
 * under {@code grammars/sql/postgresql/examples/}. The corpus IS the upstream
 * PostgreSQL regression test suite — these are real production-scale .sql
 * files (PL/pgSQL functions, ALTER TABLE matrices, joins, triggers, ...).
 *
 * <p>Goal: capture parse-error + walker-completion baseline for the heaviest
 * files. Acts as a regression guard — if a grammar update breaks one of these
 * fixtures, this test surfaces it immediately.
 *
 * <p>Acceptance: each fixture parses with ≤ {@code MAX_ERRORS_PER_FILE}
 * recoverable ANTLR errors and the walker completes without throwing.
 *
 * <p>Tag {@code "postgresql_parse"} groups all assertions for selective
 * test runs:
 *   {@code ./gradlew :libraries:hound:test --tests '*PostgresExamplesCorpusTest'}
 *   or {@code ./gradlew test -DincludeTags=postgresql_parse}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("postgresql_parse")
class PostgresExamplesCorpusTest {

    /**
     * Top-N "production-scale" PG fixtures from upstream regression tests.
     * Sized 1k+ lines each — exercise PL/pgSQL bodies, partition matrices,
     * row-level security, JSON ops, triggers, foreign-key checks.
     */
    private static final List<String> PG_HEAVY_FIXTURES = List.of(
            "plpgsql.sql",        // 4651 lines — PL/pgSQL functions corpus (analog PKG_ETL_FACT_FINANCE)
            "alter_table.sql",    // 2917 lines — ALTER TABLE matrix
            "triggers.sql",       // 2277 lines
            "join.sql",           // 2173 lines
            "rowsecurity.sql",    // 1834 lines
            "foreign_key.sql",    // 1741 lines
            "jsonb.sql",          // 1282 lines
            "partition_join.sql"  // 1144 lines
    );

    /** Soft cap: each fixture should parse with no more than this many recoverable errors. */
    private static final int MAX_ERRORS_PER_FILE = 100;

    /** Per-file parse outcome. */
    private record CorpusResult(String file, int antlrErrors, List<String> firstErrors,
                                long durationMs, boolean walked) {}

    private final Map<String, CorpusResult> results = new LinkedHashMap<>();

    // ── @BeforeAll: parse every fixture once ──────────────────────────────────

    @BeforeAll
    void parseCorpusOnce() throws Exception {
        for (String file : PG_HEAVY_FIXTURES) {
            results.put(file, parseSingle("grammars/sql/postgresql/examples/" + file));
        }
    }

    private CorpusResult parseSingle(String classpathRes) throws Exception {
        long t0 = System.currentTimeMillis();
        String sql;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(classpathRes)) {
            if (is == null) {
                return new CorpusResult(classpathRes, -1, List.of("(fixture missing)"), 0, false);
            }
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> errs = new ArrayList<>();
        BaseErrorListener errListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> r, Object offending,
                                    int line, int charPos, String msg, RecognitionException ex) {
                errs.add("line " + line + ":" + charPos + " — " + msg);
            }
        };

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PostgreSQLSemanticListener listener = new PostgreSQLSemanticListener(engine);
        listener.setDefaultSchema("public");

        PostgreSQLLexer lexer = new PostgreSQLLexer(CharStreams.fromString(sql));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errListener);

        PostgreSQLParser parser = new PostgreSQLParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errListener);

        boolean walked = false;
        try {
            new ParseTreeWalker().walk(listener, parser.root());
            walked = true;
        } catch (Exception e) {
            errs.add("walk threw: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return new CorpusResult(classpathRes, errs.size(),
                errs.stream().limit(5).collect(Collectors.toList()),
                System.currentTimeMillis() - t0, walked);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-PG-CORPUS-01: heavy fixtures parse with bounded errors
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(1)
    @DisplayName("PG-CORPUS-01: top-8 heavy fixtures parse within MAX_ERRORS_PER_FILE")
    void pgCorpus01_heavyFixturesParse() {
        StringBuilder summary = new StringBuilder("\n[PG-CORPUS-01] Per-file results:\n");
        int filesOver = 0;
        for (var e : results.entrySet()) {
            CorpusResult r = e.getValue();
            summary.append(String.format("  %-30s errs=%d walked=%-5s %dms%n",
                    e.getKey(), r.antlrErrors(), r.walked(), r.durationMs()));
            if (r.antlrErrors() > MAX_ERRORS_PER_FILE) {
                filesOver++;
                for (String err : r.firstErrors()) {
                    summary.append("      ").append(err).append('\n');
                }
            }
        }
        System.out.println(summary);

        assertEquals(0, filesOver,
                "Files exceeding MAX_ERRORS_PER_FILE=" + MAX_ERRORS_PER_FILE
                        + ": " + filesOver + summary);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-PG-CORPUS-02: walker completes for every heavy fixture
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(2)
    @DisplayName("PG-CORPUS-02: semantic walker completes for every heavy fixture")
    void pgCorpus02_heavyFixturesWalk() {
        for (var e : results.entrySet()) {
            assertTrue(e.getValue().walked(),
                    "Listener walk failed for: " + e.getKey() + " — first errors: " + e.getValue().firstErrors());
        }
    }
}
