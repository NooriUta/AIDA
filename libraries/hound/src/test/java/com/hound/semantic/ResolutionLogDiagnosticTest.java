package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S1.0 — diagnostic run across PL/SQL example files.
 *
 * Processes all .sql files under grammars/sql/plsql/examples,
 * aggregates the resolution log from each engine run, then prints
 * the top unresolved patterns to stdout for S1.BUG triage.
 *
 * Run with: gradlew test --tests "*ResolutionLogDiagnostic*"
 *
 * Not tagged @Disabled — always runs, never fails (diagnostic only).
 * Assertion: unresolved rate < 80% of column-reference atoms
 * (catches catastrophic regressions, not normal unresolved patterns).
 */
@Tag("diagnostic")
class ResolutionLogDiagnosticTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        // Suppress ANTLR console error output during diagnostic scan
        lexer.removeErrorListeners();
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    @Test
    void s1_0_resolutionLogDiagnostic() throws IOException {
        // grammars/** are excluded from classpath processing — use filesystem path directly
        Path basePath = Paths.get("src/main/resources/grammars/sql/plsql/examples");
        if (!Files.isDirectory(basePath)) {
            System.out.println("[S1.0] plsql/examples directory not found — skipping");
            return;
        }

        // Collect all .sql files recursively
        List<Path> sqlFiles;
        try (Stream<Path> walk = Files.walk(basePath)) {
            sqlFiles = walk
                    .filter(p -> p.toString().endsWith(".sql"))
                    .sorted()
                    .collect(Collectors.toList());
        }

        System.out.printf("%n[S1.0] Processing %d PL/SQL files...%n%n", sqlFiles.size());

        // Aggregate all resolution log entries across all files
        List<Map<String, Object>> allLog = new ArrayList<>();
        int filesProcessed = 0;
        int filesSkipped = 0;

        for (Path file : sqlFiles) {
            try {
                String sql = Files.readString(file);
                UniversalSemanticEngine engine = parse(sql);
                List<Map<String, Object>> log = engine.getResolutionLog();
                if (!log.isEmpty()) {
                    allLog.addAll(log);
                    filesProcessed++;
                } else {
                    filesSkipped++;
                }
            } catch (Exception e) {
                filesSkipped++;
            }
        }

        System.out.printf("  Files processed: %d  |  Skipped (no atoms): %d%n%n", filesProcessed, filesSkipped);

        if (allLog.isEmpty()) {
            System.out.println("  No resolution log entries — nothing to analyze.");
            return;
        }

        // ── Counts by result_kind ──
        Map<String, Long> byKind = allLog.stream()
                .collect(Collectors.groupingBy(
                        e -> String.valueOf(e.get("result_kind")),
                        Collectors.counting()));

        long total     = allLog.size();
        long unresolved = byKind.getOrDefault("unresolved", 0L);
        long resolved  = byKind.getOrDefault("Обработано", 0L);
        long constants = byKind.getOrDefault("constant", 0L);
        long functions = byKind.getOrDefault("function_call", 0L);

        System.out.println("══════════════════════════════════════════════════════");
        System.out.println("  RESOLUTION SUMMARY");
        System.out.println("══════════════════════════════════════════════════════");
        System.out.printf("  Total atoms:    %d%n", total);
        System.out.printf("  Resolved:       %d  (%.1f%%)%n", resolved,   pct(resolved, total));
        System.out.printf("  Unresolved:     %d  (%.1f%%)%n", unresolved, pct(unresolved, total));
        System.out.printf("  Constants:      %d  (%.1f%%)%n", constants,  pct(constants, total));
        System.out.printf("  Function calls: %d  (%.1f%%)%n", functions,  pct(functions, total));

        // ── Top unresolved raw_input patterns ──
        List<Map<String, Object>> unresolvedEntries = allLog.stream()
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .collect(Collectors.toList());

        if (!unresolvedEntries.isEmpty()) {
            System.out.println();
            System.out.println("══════════════════════════════════════════════════════");
            System.out.println("  TOP-30 UNRESOLVED raw_input  (S1.BUG candidates)");
            System.out.println("══════════════════════════════════════════════════════");

            unresolvedEntries.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.get("raw_input") + " | " + e.get("parent_context"),
                            Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(30)
                    .forEach(e -> System.out.printf("  %4d  %s%n", e.getValue(), e.getKey()));

            // ── Unresolved by parent_context ──
            System.out.println();
            System.out.println("── Unresolved by parent_context ──");
            unresolvedEntries.stream()
                    .collect(Collectors.groupingBy(
                            e -> String.valueOf(e.get("parent_context")),
                            Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> System.out.printf("  %4d  %s%n", e.getValue(), e.getKey()));

            // ── Atoms with '(' in raw_input — bracket parking bug ──
            long bracketBug = unresolvedEntries.stream()
                    .filter(e -> {
                        String raw = String.valueOf(e.get("raw_input"));
                        return raw.contains("(") || raw.endsWith("(");
                    }).count();
            if (bracketBug > 0) {
                System.out.println();
                System.out.printf("── raw_input contains '(' : %d entries (bracket parking bug) ──%n", bracketBug);
                unresolvedEntries.stream()
                        .filter(e -> String.valueOf(e.get("raw_input")).contains("("))
                        .collect(Collectors.groupingBy(
                                e -> String.valueOf(e.get("raw_input")),
                                Collectors.counting()))
                        .entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(20)
                        .forEach(e -> System.out.printf("  %4d  [%s]%n", e.getValue(), e.getKey()));
            }

            // ── Atoms with note breakdown ──
            System.out.println();
            System.out.println("── Unresolved by note (resolver reason) ──");
            unresolvedEntries.stream()
                    .collect(Collectors.groupingBy(
                            e -> String.valueOf(e.get("note")),
                            Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(15)
                    .forEach(e -> System.out.printf("  %4d  %s%n", e.getValue(), e.getKey()));
        }

        System.out.println();
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println("  END S1.0 DIAGNOSTIC");
        System.out.println("══════════════════════════════════════════════════════");

        // ── Soft assertion: unresolved rate among non-trivial atoms ──
        long nonTrivial = resolved + unresolved;
        if (nonTrivial > 0) {
            double unresolvedRate = (double) unresolved / nonTrivial;
            assertTrue(unresolvedRate < 0.80,
                    String.format("Unresolved rate %.1f%% exceeds 80%% — catastrophic regression detected", unresolvedRate * 100));
        }
    }

    private static double pct(long part, long total) {
        return total == 0 ? 0.0 : 100.0 * part / total;
    }
}
