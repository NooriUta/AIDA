package com.hound.storage;

import com.hound.metrics.PipelineTimer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.SemanticResult;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.hound.storage.CanonicalPool;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Runs all PL/SQL files from a corpus directory through HOUND and writes results
 * to the production ArcadeDB {@value #DB_NAME} database via REMOTE_BATCH.
 *
 * <h3>Run</h3>
 * <pre>
 *   ./gradlew :libraries:hound:test \
 *     --tests "com.hound.storage.CorpusBatchIT" \
 *     -Dcorpus=true -Dintegration=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "corpus", matches = "true")
class CorpusBatchIT {

    private static final String HOST = "localhost";
    private static final int    PORT = 2480;
    private static final String USER = "root";
    private static final String PASS = "playwithdata";
    private static final String DB_NAME = "hound";

    private static final String CORPUS_ROOT =
            System.getProperty("corpus.dir", "C:/Dali_tests/test_plsql");

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Test
    void runCorpusIntoBatch() throws Exception {
        // Collect all .pck/.sql files
        List<Path> files = collectFiles(Path.of(CORPUS_ROOT));
        System.out.printf("%nCorpus: %d files from %s%n", files.size(), CORPUS_ROOT);

        int processed = 0, skipped = 0, errors = 0;
        int totalRecords = 0, totalStatements = 0, totalTables = 0;
        List<String> filesWithRecords = new ArrayList<>();

        // ── Single writer for the entire corpus run ──────────────────────────
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(
                HOST, PORT, DB_NAME, USER, PASS, true)) {

            // Clean DB so cross-file duplicate keys (e.g. DUAL) don't cause failures
            System.out.printf("Cleaning hound DB before corpus run...%n");
            writer.cleanAll();
            System.out.printf("Clean done.%n%n");

            // Shared canonical pool — deduplicates schemas/tables across all files
            CanonicalPool pool = writer.ensureCanonicalPool(DB_NAME);

            for (Path file : files) {
                String label = CORPUS_ROOT.length() < file.toString().length()
                        ? file.toString().substring(CORPUS_ROOT.length() + 1)
                        : file.getFileName().toString();
                try {
                    String sql = Files.readString(file);
                    UniversalSemanticEngine engine = new UniversalSemanticEngine();
                    PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
                    PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
                    PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
                    new ParseTreeWalker().walk(listener, parser.sql_script());
                    engine.resolvePendingColumns();

                    String sid = "corpus-" + System.nanoTime();
                    SemanticResult result = engine.getResult(sid, file.getFileName().toString(), "plsql", 0L);

                    if (result.getStructure() == null
                            || result.getStructure().getStatements().isEmpty()) {
                        skipped++;
                        continue;
                    }

                    int recCount = result.getStructure().getRecords().size();
                    int stmtCount = result.getStructure().getStatements().size();
                    int tblCount  = result.getStructure().getTables().size();
                    totalStatements += stmtCount;
                    totalTables     += tblCount;
                    totalRecords    += recCount;
                    if (recCount > 0) filesWithRecords.add(label + " (" + recCount + " records)");

                    writer.saveResult(result, new PipelineTimer(), pool, DB_NAME);
                    processed++;

                    if (processed % 20 == 0)
                        System.out.printf("  [%d/%d] processed=%d skipped=%d errors=%d DaliRecord=%d%n",
                                processed + skipped + errors, files.size(),
                                processed, skipped, errors, totalRecords);

                } catch (Exception e) {
                    errors++;
                    System.err.printf("  ERROR %s: %s%n", label, e.getMessage());
                }
            }
        }

        // ── Summary ──────────────────────────────────────────────────────────
        System.out.printf("%n=== Corpus run complete ===%n");
        System.out.printf("  Files         : %d total  processed=%d  skipped=%d  errors=%d%n",
                files.size(), processed, skipped, errors);
        System.out.printf("  DaliStatement : %d%n", totalStatements);
        System.out.printf("  DaliTable     : %d%n", totalTables);
        System.out.printf("  DaliRecord    : %d  (in %d files)%n",
                totalRecords, filesWithRecords.size());

        if (!filesWithRecords.isEmpty()) {
            System.out.printf("%n  Files with BULK COLLECT:%n");
            filesWithRecords.forEach(f -> System.out.printf("    - %s%n", f));
        }

        // ── Query hound DB for DaliRecord count ───────────────────────────────
        System.out.printf("%n=== hound DB counts ===%n");
        for (String type : new String[]{
                "DaliSession", "DaliStatement", "DaliTable", "DaliRecord",
                "DaliRecordField", "BULK_COLLECTS_INTO", "RECORD_HAS_FIELD", "PLTYPE_HAS_FIELD"}) {
            long cnt = dbCount(type);
            System.out.printf("  %-25s %d%n", type, cnt);
        }

        // Test passes regardless of DaliRecord count — this is an informational run
        System.out.printf("%n✓ Corpus run finished. Check hound DB in ArcadeDB Studio.%n");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<Path> collectFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            System.err.println("WARN: corpus directory not found: " + root);
            return List.of();
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.endsWith(".pck") || name.endsWith(".sql");
            }).sorted().forEach(result::add);
        }
        return result;
    }

    private static long dbCount(String type) {
        try {
            String body = "{\"language\":\"sql\",\"command\":\"SELECT count(*) AS cnt FROM " + type + "\"}";
            String auth = "Basic " + Base64.getEncoder()
                    .encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/command/" + DB_NAME))
                    .header("Authorization", auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            String resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
            int idx = resp.indexOf("\"cnt\":");
            if (idx < 0) return -1L;
            int start = idx + 6, end = start;
            while (end < resp.length() &&
                   (Character.isDigit(resp.charAt(end)) || resp.charAt(end) == '-')) end++;
            return Long.parseLong(resp.substring(start, end));
        } catch (Exception e) {
            return -1L;
        }
    }
}
