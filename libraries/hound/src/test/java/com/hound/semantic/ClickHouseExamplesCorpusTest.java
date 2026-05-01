package com.hound.semantic;

import com.hound.parser.base.grammars.sql.clickhouse.ClickHouseLexer;
import com.hound.parser.base.grammars.sql.clickhouse.ClickHouseParser;
import com.hound.semantic.dialect.clickhouse.ClickHouseSemanticListener;
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
 * TC-HOUND-CH-CORPUS-01..N
 * Smoke + baseline test for the ANTLR4 grammars-v4 ClickHouse example corpus
 * shipped with our grammar copy under
 *   {@code grammars/sql/clickhouse/examples/}.
 *
 * <p>Goal: make sure the upstream-provided examples parse without errors —
 * if any do, that's a real grammar regression we own.
 *
 * <p>Acceptance: every file parses with ≤ 1 ANTLR error and walks the
 * semantic listener without throwing.
 *
 * <p>Tag {@code "clickhouse_parse"} groups all assertions for selective
 * test runs:
 *   {@code ./gradlew :libraries:hound:test --tests '*ClickHouseExamplesCorpusTest'}
 *   or {@code ./gradlew test -DincludeTags=clickhouse_parse}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("clickhouse_parse")
class ClickHouseExamplesCorpusTest {

    /** All example file basenames shipped under {@code grammars/sql/clickhouse/examples/}. */
    private static final List<String> CH_EXAMPLES = List.of(
            "create_dictionary.sql",
            "create_table.sql",
            "delete.sql",
            "insert.sql",
            "multiple_statements.sql",
            "rename.sql",
            "select.sql",
            "update.sql"
    );

    /** Per-file parse outcome. */
    private record CorpusResult(String file, int antlrErrors, List<String> firstErrors,
                                long durationMs, boolean walked) {}

    private final Map<String, CorpusResult> results = new LinkedHashMap<>();

    // ── @BeforeAll: parse every example once ──────────────────────────────────

    @BeforeAll
    void parseCorpusOnce() throws Exception {
        for (String file : CH_EXAMPLES) {
            results.put(file, parseSingle("grammars/sql/clickhouse/examples/" + file));
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
        ClickHouseSemanticListener listener = new ClickHouseSemanticListener(engine);
        listener.setDefaultSchema("default");

        ClickHouseLexer lexer = new ClickHouseLexer(CharStreams.fromString(sql));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errListener);

        ClickHouseParser parser = new ClickHouseParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errListener);

        boolean walked = false;
        try {
            new ParseTreeWalker().walk(listener, parser.clickhouseFile());
            walked = true;
        } catch (Exception e) {
            errs.add("walk threw: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return new CorpusResult(classpathRes, errs.size(),
                errs.stream().limit(3).collect(Collectors.toList()),
                System.currentTimeMillis() - t0, walked);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-CH-CORPUS-01: every example parses with ≤ 1 ANTLR error and walks
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(1)
    @DisplayName("CH-CORPUS-01: all 8 ClickHouse examples parse and walk")
    void chCorpus01_allExamplesParse() {
        StringBuilder summary = new StringBuilder("\n[CH-CORPUS-01] Per-file results:\n");
        int totalErrors = 0;
        int filesWithErrors = 0;
        for (var e : results.entrySet()) {
            CorpusResult r = e.getValue();
            summary.append(String.format("  %-30s errs=%d walked=%-5s %dms%n",
                    e.getKey(), r.antlrErrors(), r.walked(), r.durationMs()));
            if (r.antlrErrors() > 0) {
                filesWithErrors++;
                totalErrors += r.antlrErrors();
                for (String err : r.firstErrors()) {
                    summary.append("      ").append(err).append('\n');
                }
            }
        }
        System.out.println(summary);

        // Soft assertion: examples are basic CH constructs — they must parse.
        assertEquals(0, filesWithErrors,
                "ClickHouse examples must parse cleanly. " + filesWithErrors + " files had errors. " + summary);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TC-CH-CORPUS-02: walker doesn't throw for any example
    // ─────────────────────────────────────────────────────────────────────────
    @Test @Order(2)
    @DisplayName("CH-CORPUS-02: semantic walker completes for every example")
    void chCorpus02_allExamplesWalk() {
        for (var e : results.entrySet()) {
            assertTrue(e.getValue().walked(),
                    "Listener walk failed for: " + e.getKey() + " — first errors: " + e.getValue().firstErrors());
        }
    }
}
