package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * S2.Q probe — measures atom resolution quality on real .pck files.
 *
 * Run with:  ./gradlew test --tests "*AtomQualityProbeTest*" --info
 */
class AtomQualityProbeTest {

    private static final Path[] CORPUS_DIRS = {
        Path.of("C:/AIDA/Dali4/HOUND/logs/ODS"),
        Path.of("C:/AIDA/Dali4/HOUND/logs/DWH"),
    };

    record FileStats(
        String fileName, int total, int resolved, int unresolved,
        int constants, int functions, int other
    ) {
        double quality()    { return total == 0 ? 1.0 : (resolved + constants + functions) / (double) total; }
        double unresPct()   { return total == 0 ? 0.0 : unresolved / (double) total * 100; }
    }

    private FileStats probe(Path file) throws IOException {
        String sql = Files.readString(file);
        if (sql.isBlank()) return new FileStats(file.getFileName().toString(), 0, 0, 0, 0, 0, 0);

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();

        List<Map<String, Object>> log = engine.getResolutionLog();
        int total      = log.size();
        int resolved   = (int) log.stream().filter(e -> "Обработано"   .equals(e.get("result_kind"))).count();
        int unresolved = (int) log.stream().filter(e -> "unresolved"   .equals(e.get("result_kind"))).count();
        int constants  = (int) log.stream().filter(e -> "constant"     .equals(e.get("result_kind"))).count();
        int functions  = (int) log.stream().filter(e -> "function_call".equals(e.get("result_kind"))).count();
        int other      = total - resolved - unresolved - constants - functions;
        return new FileStats(file.getFileName().toString(), total, resolved, unresolved, constants, functions, other);
    }

    @Test
    void measureAtomQualityOnCorpus() throws Exception {
        List<FileStats> all = new ArrayList<>();

        for (Path dir : CORPUS_DIRS) {
            if (!Files.isDirectory(dir)) {
                System.out.println("[SKIP] not found: " + dir);
                continue;
            }
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.filter(p -> {
                    String n = p.getFileName().toString().toLowerCase();
                    return n.endsWith(".pck") || n.endsWith(".sql") || n.endsWith(".pls");
                }).sorted().forEach(p -> {
                    try { all.add(probe(p)); }
                    catch (Exception e) {
                        System.out.println("[ERROR] " + p.getFileName() + " — " + e.getMessage());
                    }
                });
            }
        }

        if (all.isEmpty()) {
            System.out.println("No files found in corpus dirs.");
            return;
        }

        // Per-file report
        System.out.println();
        System.out.printf("%-55s %6s %10s %10s %10s %10s%n",
                "FILE", "TOTAL", "RESOLVED%", "UNRES%", "CONST%", "FUNC%");
        System.out.println("-".repeat(105));

        for (FileStats s : all) {
            if (s.total() == 0) continue;
            System.out.printf("%-55s %6d   %7.1f%%   %7.1f%%   %7.1f%%   %7.1f%%%n",
                    s.fileName(),
                    s.total(),
                    s.resolved()  / (double) s.total() * 100,
                    s.unresPct(),
                    s.constants() / (double) s.total() * 100,
                    s.functions() / (double) s.total() * 100);
        }

        // Aggregate
        long totalAtoms    = all.stream().mapToLong(FileStats::total)     .sum();
        long totalResolved = all.stream().mapToLong(FileStats::resolved)  .sum();
        long totalUnres    = all.stream().mapToLong(FileStats::unresolved).sum();
        long totalConst    = all.stream().mapToLong(FileStats::constants) .sum();
        long totalFuncs    = all.stream().mapToLong(FileStats::functions) .sum();
        double avgQuality  = all.stream().mapToDouble(FileStats::quality) .average().orElse(0);
        double minQuality  = all.stream().mapToDouble(FileStats::quality) .min()    .orElse(0);
        int filesWithAtoms = (int) all.stream().filter(s -> s.total() > 0).count();

        System.out.println("=".repeat(105));
        System.out.printf("%-55s %6d   %7.1f%%   %7.1f%%   %7.1f%%   %7.1f%%%n",
                "TOTAL (" + filesWithAtoms + " files)", totalAtoms,
                totalResolved / (double) totalAtoms * 100,
                totalUnres    / (double) totalAtoms * 100,
                totalConst    / (double) totalAtoms * 100,
                totalFuncs    / (double) totalAtoms * 100);
        System.out.printf("%nAvg quality: %.2f   Min quality: %.2f%n", avgQuality, minQuality);
        System.out.printf("Unresolved atoms: %d / %d  (%.1f%%)%n",
                totalUnres, totalAtoms, totalUnres / (double) totalAtoms * 100);
    }
}
