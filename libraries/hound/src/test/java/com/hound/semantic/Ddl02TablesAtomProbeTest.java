package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Diagnostic: parse CRM/02_tables.sql and report unresolved atoms.
 */
class Ddl02TablesAtomProbeTest {

    private static final String FILE = "C:/AIDA/AIDA_DOCS/ScriptsBase/Claude_sripts/ERP_CORE/CRM/02_tables.sql";

    @Test
    void analyzeUnresolved() throws Exception {
        Path path = Paths.get(FILE);
        if (!path.toFile().exists()) { System.out.println("SKIP"); return; }

        String sql = Files.readString(path);
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        lexer.removeErrorListeners();
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();

        List<Map<String, Object>> log = engine.getResolutionLog();

        // Group by result_kind
        Map<String, Long> byKind = log.stream()
            .collect(Collectors.groupingBy(
                e -> String.valueOf(e.get("result_kind")),
                Collectors.counting()));
        System.out.println("=== ATOM RESOLUTION SUMMARY ===");
        byKind.entrySet().stream()
            .sorted((a,b) -> Long.compare(b.getValue(), a.getValue()))
            .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
        System.out.println("  TOTAL: " + log.size());

        // Show unresolved
        List<Map<String, Object>> unresolved = log.stream()
            .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
            .toList();

        System.out.println("\n=== UNRESOLVED ATOMS (" + unresolved.size() + ") ===");

        // Group by atom_context
        Map<String, List<Map<String, Object>>> byCtx = unresolved.stream()
            .collect(Collectors.groupingBy(e -> String.valueOf(e.get("atom_context"))));

        byCtx.entrySet().stream()
            .sorted((a,b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
            .forEach(entry -> {
                System.out.println("\n  --- " + entry.getKey() + " (" + entry.getValue().size() + ") ---");
                entry.getValue().stream().limit(15).forEach(e ->
                    System.out.println("    " + e.get("raw_input") + " | stmt=" +
                        Optional.ofNullable((String)e.get("statement_geoid")).map(s -> s.length() > 60 ? "..." + s.substring(s.length()-60) : s).orElse("null"))
                );
                if (entry.getValue().size() > 15)
                    System.out.println("    ... +" + (entry.getValue().size()-15) + " more");
            });
    }
}
