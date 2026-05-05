package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.AtomInfo;
import com.hound.semantic.model.StatementInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * H1.9.Q — Regression quality threshold.
 *
 * Parses all PL/SQL fixture files under src/test/resources/plsql/ and asserts:
 *   1. Each file produces at least one statement and one atom.
 *   2. Atom resolution quality (resolved + constants + functions) / total ≥ QUALITY_THRESHOLD.
 *
 * QUALITY_THRESHOLD was calibrated against the fixture files during S2.Q.
 * Update it whenever new fixture files are added or the resolution logic changes significantly.
 */
class HoundRegressionTest {

    private static final Logger LOG = LoggerFactory.getLogger(HoundRegressionTest.class);

    /**
     * S2.Q baseline: minimum acceptable atom-resolution quality per file.
     * Calibrated on 3 fixture files (19-04-2026):
     *   pkg_dml_basic.pck   = 1.00
     *   pkg_merge_using.pck = 1.00
     *   pkg_select_cte.pck  = 0.887
     *   → threshold = min - 0.08 = 0.80
     */
    static final double QUALITY_THRESHOLD = 0.80;

    // ═══════════════════════════════════════════════════════════════
    // Test infrastructure
    // ═══════════════════════════════════════════════════════════════

    /** Loads and parses a PL/SQL file, returns the engine after resolution. */
    private static UniversalSemanticEngine parseFile(Path path) throws IOException {
        String sql = Files.readString(path);
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    /** Computes atom quality: (resolved + constants + functions) / total. */
    static double quality(List<Map<String, Object>> log) {
        if (log.isEmpty()) return 1.0;
        long resolved  = log.stream().filter(e -> "RESOLVED".equals(e.get("result_kind"))).count();
        long constants = log.stream().filter(e -> "CONSTANT"  .equals(e.get("result_kind"))).count();
        long functions = log.stream().filter(e -> "FUNCTION_CALL".equals(e.get("result_kind"))).count();
        return (resolved + constants + functions) / (double) log.size();
    }

    /** Lists all .pck / .sql / .pls fixture files in src/test/resources/plsql/. */
    static Stream<Path> testFiles() throws Exception {
        URL dir = HoundRegressionTest.class.getClassLoader().getResource("plsql");
        if (dir == null) {
            // Fallback: look relative to project root
            Path fallback = Path.of("src/test/resources/plsql");
            if (Files.isDirectory(fallback)) {
                return Files.list(fallback).filter(p -> {
                    String n = p.getFileName().toString();
                    return n.endsWith(".pck") || n.endsWith(".sql") || n.endsWith(".pls");
                }).sorted();
            }
            return Stream.empty();
        }
        return Files.list(Path.of(dir.toURI()))
                .filter(p -> {
                    String n = p.getFileName().toString();
                    return n.endsWith(".pck") || n.endsWith(".sql") || n.endsWith(".pls");
                })
                .sorted();
    }

    // ═══════════════════════════════════════════════════════════════
    // Per-file structural assertions
    // ═══════════════════════════════════════════════════════════════

    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void fileProducesStatements(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        int stmtCount = engine.getBuilder().getStatements().size();
        assertTrue(stmtCount > 0,
                file.getFileName() + ": expected at least 1 statement, got " + stmtCount);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void fileProducesAtoms(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        int atomCount = engine.getResolutionLog().size();
        assertTrue(atomCount > 0,
                file.getFileName() + ": expected at least 1 atom in resolution log, got " + atomCount);
    }

    // ═══════════════════════════════════════════════════════════════
    // Quality threshold assertion
    // ═══════════════════════════════════════════════════════════════

    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void qualityAboveThreshold(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        List<Map<String, Object>> log = engine.getResolutionLog();

        double q = quality(log);
        LOG.info("Quality [{} atoms]: {}  file={}",
                log.size(), String.format("%.2f", q), file.getFileName());

        assertTrue(q >= QUALITY_THRESHOLD,
                "%s: quality %.2f below threshold %.2f  (total=%d, unresolved=%d)"
                        .formatted(file.getFileName(), q, QUALITY_THRESHOLD,
                                log.size(),
                                log.stream().filter(e -> "UNRESOLVED".equals(e.get("result_kind"))).count()));
    }

    // ═══════════════════════════════════════════════════════════════
    // Domain-specific regression checks
    // ═══════════════════════════════════════════════════════════════

    @Test
    void mergeUsingSubquery_noUnresolvedSourceAlias() throws Exception {
        Path f = testFiles()
                .filter(p -> p.getFileName().toString().contains("merge"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No merge fixture found"));

        UniversalSemanticEngine engine = parseFile(f);
        List<Map<String, Object>> log = engine.getResolutionLog();

        long unresolvedSourceAlias = log.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("SOURCE.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSourceAlias,
                "SOURCE.* refs in MERGE USING (SELECT...) must be resolved — BUG S1.BUG-4 regression");
    }

    // ═══════════════════════════════════════════════════════════════
    // G1/G2 — affected_columns per-file assertions
    // ═══════════════════════════════════════════════════════════════

    /**
     * G1: Every fixture file must produce at least one DaliAffectedColumn entry.
     * Ensures the affected-column pipeline runs and produces output.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void affectedColumns_nonEmpty(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        long total = engine.getBuilder().getStatements().values().stream()
                .mapToLong(s -> s.getAffectedColumns().size())
                .sum();
        assertTrue(total > 0,
                file.getFileName() + ": expected > 0 affected columns, got 0");
    }

    /**
     * G2: All DML target affected columns must have order_affect > 0.
     * Ensures the poliage_update ordinal counter is wired correctly.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void insertPoliage_orderAffectPositive(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        List<String> violations = new ArrayList<>();
        for (var e : engine.getBuilder().getStatements().entrySet()) {
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> poliage = (List<Map<String, Object>>) ac.get("poliage_update");
                if (poliage == null || poliage.isEmpty()) continue;
                String typeAffect = (String)  poliage.get(0).get("type_affect");
                Object orderAffect = poliage.get(0).get("order_affect");
                if (typeAffect != null
                        && (orderAffect == null || ((Number) orderAffect).intValue() <= 0)) {
                    violations.add(e.getKey() + "." + ac.get("column_name") + "[" + typeAffect + "]");
                }
            }
        }
        assertTrue(violations.isEmpty(),
                file.getFileName() + ": DML affected columns with order_affect <= 0: " + violations);
    }

    /**
     * G4: DaliAffectedColumn must contain ONLY target table columns.
     * source_type must be one of: INSERT, UPDATE, MERGE.
     * SELECT, WHERE, JOIN, ORDER, HAVING, DELETE, VALUES, etc. must not appear.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void affectedColumns_onlyTargetColumns(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        Set<String> allowed = Set.of("INSERT", "UPDATE", "MERGE");
        List<String> violations = new ArrayList<>();
        for (var e : engine.getBuilder().getStatements().entrySet()) {
            for (Map<String, Object> ac : e.getValue().getAffectedColumns()) {
                String st = (String) ac.get("source_type");
                if (!allowed.contains(st)) {
                    violations.add(e.getKey() + "." + ac.get("column_name") + "[" + st + "]");
                }
            }
        }
        assertTrue(violations.isEmpty(),
                file.getFileName() + ": non-target source_type in affectedColumns: " + violations);
    }

    /**
     * G3 / MERGE: Files with MERGE statements must have MERGE affected columns (source_type="MERGE").
     * Columns appear once (deduplicated); merge_clause on ATOM_PRODUCES edges distinguishes UPDATE/INSERT.
     * Regression guard for MERGE column-binding (enterMerge_element / paren_column_list).
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void mergeStatements_bindTargetColumns(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        boolean hasMerge = engine.getBuilder().getStatements().values().stream()
                .anyMatch(s -> "MERGE".equals(s.getType()));
        if (!hasMerge) return;   // skip non-MERGE fixtures

        boolean hasMergeAffected = engine.getBuilder().getStatements().values().stream()
                .flatMap(s -> s.getAffectedColumns().stream())
                .anyMatch(ac -> "MERGE".equals(ac.get("source_type")));
        assertTrue(hasMergeAffected,
                file.getFileName() + ": MERGE statement missing any MERGE affected columns");
    }

    // ═══════════════════════════════════════════════════════════════
    // T7 — output-column and atom-sequence assertions
    // ═══════════════════════════════════════════════════════════════

    /**
     * T7.1: Every fixture file must produce at least one output column across
     * all its SELECT statements.  A fixture with zero output columns indicates
     * that the SELECT-list listener is not firing.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void outputColumns_nonEmpty(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);
        long totalOutputCols = engine.getBuilder().getStatements().values().stream()
                .mapToLong(s -> s.getColumnsOutput().size())
                .sum();
        assertTrue(totalOutputCols > 0,
                file.getFileName() + ": expected > 0 output columns across all statements, got 0");
    }

    /**
     * T7.2: Resolved column-reference atoms that belong to a SELECT statement
     * must carry output_column_sequence (i.e. they are wired to an output column).
     * Atoms in CTEs / SUBQUERYs or non-SELECT statements are excluded.
     *
     * This is a soft check: we assert that at least one resolved atom in at least
     * one SELECT has output_column_sequence set — not that ALL atoms have it,
     * because WHERE/HAVING atoms are intentionally excluded from output columns.
     *
     * Note: atoms live in AtomProcessor.atomsByStatement, not in StatementInfo.atoms.
     * Use engine.getAtomProcessor().getAtomsForStatement(geoid) to access them.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void resolvedAtoms_selectHasAtLeastOneWithOutputColumnSequence(Path file) throws IOException {
        UniversalSemanticEngine engine = parseFile(file);

        boolean found = false;
        for (var e : engine.getBuilder().getStatements().entrySet()) {
            StatementInfo s = e.getValue();
            if (!"SELECT".equals(s.getType())) continue;
            for (var atom : engine.getAtomProcessor().getAtomsForStatement(s.getGeoid()).values()) {
                if (AtomInfo.STATUS_RESOLVED.equals(atom.get("status"))
                        && atom.get("output_column_sequence") != null) {
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        // Only assert if the file contains at least one SELECT with output columns —
        // pure DML files (only INSERT/UPDATE/DELETE) are exempt.
        boolean hasSelectWithOutputCols = engine.getBuilder().getStatements().values().stream()
                .anyMatch(s -> "SELECT".equals(s.getType()) && !s.getColumnsOutput().isEmpty());

        if (hasSelectWithOutputCols) {
            assertTrue(found,
                    file.getFileName() + ": file has SELECT statements with output columns but no "
                            + "resolved atom carries output_column_sequence — DATA_FLOW wiring broken");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // S2.Q — summary (informational, non-blocking)
    // ═══════════════════════════════════════════════════════════════

    @Test
    void regressionSummary() throws Exception {
        Map<String, Double> qualities = new LinkedHashMap<>();
        for (Path file : (Iterable<Path>) testFiles()::iterator) {
            UniversalSemanticEngine engine = parseFile(file);
            qualities.put(file.getFileName().toString(), quality(engine.getResolutionLog()));
        }

        if (qualities.isEmpty()) {
            LOG.warn("regressionSummary: no fixture files found");
            return;
        }

        double avg = qualities.values().stream().mapToDouble(d -> d).average().orElse(0);
        double min = qualities.values().stream().mapToDouble(d -> d).min().orElse(0);
        double max = qualities.values().stream().mapToDouble(d -> d).max().orElse(0);

        LOG.info("=== S2.Q Quality Summary ===");
        qualities.forEach((name, q) ->
                LOG.info("  {}  quality={}",
                        String.format("%-50s", name), String.format("%.2f", q)));
        LOG.info("  avg={}  min={}  max={}  files={}",
                String.format("%.2f", avg), String.format("%.2f", min),
                String.format("%.2f", max), qualities.size());
        LOG.info("  Suggested QUALITY_THRESHOLD = {}  (min - 0.10)",
                String.format("%.2f", Math.max(0, min - 0.10)));

        // ── G1/G2/G4 aggregate summary ──
        long totalAC = 0, totalInsert = 0, totalUpdate = 0, totalMergeU = 0;
        Map<String, Long> bySourceType = new LinkedHashMap<>();
        for (Path file : (Iterable<Path>) testFiles()::iterator) {
            UniversalSemanticEngine engine = parseFile(file);
            for (StatementInfo s : engine.getBuilder().getStatements().values()) {
                for (Map<String, Object> ac : s.getAffectedColumns()) {
                    totalAC++;
                    String src = ac.get("source_type") != null ? ac.get("source_type").toString() : "(null)";
                    bySourceType.merge(src, 1L, Long::sum);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> poliage = (List<Map<String, Object>>) ac.get("poliage_update");
                    if (poliage != null && !poliage.isEmpty()) {
                        String ta = (String) poliage.get(0).get("type_affect");
                        if ("INSERT".equals(ta)) totalInsert++;
                        else if ("UPDATE".equals(ta)) totalUpdate++;
                    }
                    if ("MERGE".equals(src)) totalMergeU++; // unified MERGE source_type (deduped)
                }
            }
        }
        LOG.info("=== G1/G2/G4 AffectedColumn Summary (target columns only) ===");
        LOG.info("  Total DaliAffectedColumn : {}", totalAC);
        bySourceType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> LOG.info("    {:<28} {}", e.getKey(), e.getValue()));
        LOG.info("  type_affect — INSERT:{} UPDATE:{}", totalInsert, totalUpdate);
        LOG.info("  MERGE targets (deduped): {}", totalMergeU);

        // Non-blocking structural assertion
        assertTrue(avg > 0,
                "Average quality must be > 0 across all fixture files");
        assertTrue(totalAC > 0,
                "Total affected columns must be > 0 across all fixture files");
    }
}
