package com.hound.api;

import com.hound.HoundParserImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Diagnostic test: parse PKG_ETL_07_ANALYTICS.sql and report unresolved atoms.
 */
class PkgEtlAnalyticsTest {

    private static final String FILE = "C:/AIDA/AIDA_DOCS/ScriptsBase/Claude_sripts/ERP_CORE/PKG_ETL_07_ANALYTICS.sql";

    @Test
    void analyzeUnresolvedAtoms() throws Exception {
        Path path = Paths.get(FILE);
        if (!path.toFile().exists()) {
            System.out.println("SKIP: file not found: " + FILE);
            return;
        }

        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        List<String> semanticWarnings = new ArrayList<>();
        List<String> parseErrors = new ArrayList<>();
        AtomicInteger atomEvents = new AtomicInteger(0);
        Map<String, Integer> atomTypes = new LinkedHashMap<>();

        HoundEventListener listener = new HoundEventListener() {
            @Override
            public void onAtomExtracted(String file, int atomCount, String atomType) {
                atomEvents.set(atomCount);
                atomTypes.merge(atomType, 1, Integer::sum);
            }

            @Override
            public void onSemanticWarning(String file, String category, String message) {
                semanticWarnings.add(category + ": " + message);
            }

            @Override
            public void onParseError(String file, int line, int charPos, String msg) {
                parseErrors.add("L" + line + ":" + charPos + " " + msg);
            }
        };

        ParseResult result = new HoundParserImpl().parse(path, config, listener);

        System.out.println("=== PKG_ETL_07_ANALYTICS PARSE RESULT ===");
        System.out.println("atomCount: " + result.atomCount());
        System.out.println("atomsResolved: " + result.atomsResolved());
        System.out.println("atomsUnresolved: " + result.atomsUnresolved());
        System.out.println("resolutionRate: " + String.format("%.1f%%", result.resolutionRate() * 100));
        System.out.println("vertexCount: " + result.vertexCount());
        System.out.println("edgeCount: " + result.edgeCount());
        System.out.println("droppedEdgeCount: " + result.droppedEdgeCount());
        System.out.println("errors: " + result.errors().size());
        System.out.println("warnings: " + result.warnings().size());
        System.out.println("atomEvents total: " + atomEvents.get());

        System.out.println("\n=== ATOM TYPES ===");
        atomTypes.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));

        if (!result.errors().isEmpty()) {
            System.out.println("\n=== PARSE ERRORS (first 10) ===");
            result.errors().stream().limit(10).forEach(e -> System.out.println("  " + e));
        }

        if (!result.warnings().isEmpty()) {
            System.out.println("\n=== PARSE WARNINGS (first 10) ===");
            result.warnings().stream().limit(10).forEach(w -> System.out.println("  " + w));
        }

        if (!parseErrors.isEmpty()) {
            System.out.println("\n=== ANTLR PARSE ERRORS (first 10) ===");
            parseErrors.stream().limit(10).forEach(e -> System.out.println("  " + e));
        }

        // Group semantic warnings by category
        Map<String, Integer> warnCats = new LinkedHashMap<>();
        for (String w : semanticWarnings) {
            String cat = w.contains(":") ? w.substring(0, w.indexOf(':')) : w;
            warnCats.merge(cat, 1, Integer::sum);
        }
        System.out.println("\n=== SEMANTIC WARNINGS BY CATEGORY (" + semanticWarnings.size() + " total) ===");
        warnCats.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));

        // Show first 20 ATOM_UNRESOLVED warnings
        System.out.println("\n=== UNRESOLVED ATOMS (first 30) ===");
        semanticWarnings.stream()
                .filter(w -> w.startsWith("ATOM_UNRESOLVED"))
                .limit(30)
                .forEach(w -> System.out.println("  " + w));
    }
}
