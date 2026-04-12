// src/main/java/com/hound/HoundParserImpl.java
package com.hound;

import com.hound.api.*;
import com.hound.metrics.PipelineTimer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.processor.ThreadPoolManager;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.SemanticResult;
import com.hound.semantic.model.Structure;
import com.hound.storage.ArcadeDBSemanticWriter;
import com.hound.storage.CanonicalPool;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Primary implementation of {@link HoundParser}.
 *
 * <p>Thread-safe — each {@code analyzeFile} call creates its own engine/listener instances.
 *
 * <p>Dali registers this as a CDI {@code @ApplicationScoped} bean (configured in services/dali):
 * <pre>{@code
 * @Inject HoundParser houndParser;  // resolves to HoundParserImpl
 * }</pre>
 */
public class HoundParserImpl implements HoundParser {

    private static final Logger logger = LoggerFactory.getLogger(HoundParserImpl.class);

    // ─── HoundParser API ─────────────────────────────────────────

    @Override
    public ParseResult parse(Path file, HoundConfig config) {
        return parse(file, config, NoOpHoundEventListener.INSTANCE);
    }

    @Override
    public ParseResult parse(Path file, HoundConfig config, HoundEventListener listener) {
        listener.onFileParseStarted(file.toString(), config.dialect());
        try (ArcadeDBSemanticWriter writer = createWriter(config)) {
            ParseResult result = doParseFile(file, config, writer, null, null);
            listener.onFileParseCompleted(file.toString(), result);
            return result;
        } catch (Exception e) {
            listener.onError(file.toString(), e);
            throw new RuntimeException("Hound parse failed: " + file, e);
        }
    }

    @Override
    public List<ParseResult> parseBatch(List<Path> files, HoundConfig config) {
        return parseBatch(files, config, NoOpHoundEventListener.INSTANCE);
    }

    @Override
    public List<ParseResult> parseBatch(List<Path> files, HoundConfig config, HoundEventListener listener) {
        if (files.isEmpty()) return List.of();

        try (ArcadeDBSemanticWriter writer = createWriter(config)) {
            // CanonicalPool for namespace isolation (null if no writer)
            String schema = config.targetSchema();
            CanonicalPool pool = (writer != null && schema != null && !schema.isBlank())
                    ? writer.ensureCanonicalPool(schema, schema, schema)
                    : null;

            AtomicLong sessionSeq = new AtomicLong(System.currentTimeMillis());

            // ── Phase 1: parallel parse ──
            int threads = config.workerThreads();
            List<Future<AnalysisResult>> futures = new ArrayList<>(files.size());
            ThreadPoolManager pool2 = ThreadPoolManager.newFixedThreadPool(threads);
            for (Path file : files) {
                futures.add(pool2.submit(() -> {
                    listener.onFileParseStarted(file.toString(), config.dialect());
                    return analyzeFile(file, config, sessionSeq, schema);
                }));
            }
            pool2.shutdownAndWait();

            // ── Phase 2: sequential write + build results ──
            List<ParseResult> results = new ArrayList<>(files.size());
            for (int i = 0; i < futures.size(); i++) {
                Path file = files.get(i);
                try {
                    AnalysisResult ar = futures.get(i).get();
                    ParseResult pr;
                    if (ar.semantic() != null) {
                        if (writer != null) {
                            writer.saveResult(ar.semantic(), ar.timer(), pool, schema);
                        }
                        pr = toParseResult(ar.semantic(), ar.timer());
                    } else {
                        pr = emptyResult(file.toString());
                    }
                    listener.onFileParseCompleted(file.toString(), pr);
                    results.add(pr);
                } catch (ExecutionException e) {
                    logger.error("Parse failed: {} — {}", file, e.getCause().getMessage(), e.getCause());
                    listener.onError(file.toString(), e.getCause());
                    results.add(errorResult(file.toString(), e.getCause().getMessage()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    listener.onError(file.toString(), e);
                    results.add(errorResult(file.toString(), "Interrupted"));
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Hound batch failed", e);
        }
    }

    // ─── Core parse logic ─────────────────────────────────────────

    /**
     * Single-file parse: read → ANTLR → semantic walk → resolve.
     * Does NOT write to DB — caller decides when/how to write.
     */
    private AnalysisResult analyzeFile(Path file, HoundConfig config,
                                        AtomicLong sessionSeq, String defaultSchema)
            throws IOException {
        String sql = readFileWithFallback(file);
        if (sql.isBlank()) {
            logger.warn("Empty file: {}", file);
            return new AnalysisResult(file, null, new PipelineTimer());
        }

        long lineCount = sql.lines().count();
        PipelineTimer timer = new PipelineTimer();
        timer.count("lines", (int) lineCount);

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        Object listener = createDialectListener(config.dialect(), engine, defaultSchema);
        parseAndWalk(sql, config.dialect(), listener, timer);

        timer.start("resolve");
        engine.resolvePendingColumns();
        timer.stop("resolve");

        long parseWalkResolveMs = timer.ms("parse") + timer.ms("walk") + timer.ms("resolve");
        SemanticResult result = engine.getResult(
                "session-" + sessionSeq.getAndIncrement(),
                file.toString(),
                config.dialect(),
                parseWalkResolveMs
        ).withRawScript(sql);

        return new AnalysisResult(file, result, timer);
    }

    /**
     * Single-file parse + optional write (for {@link #parse(Path, HoundConfig, HoundEventListener)}).
     */
    private ParseResult doParseFile(Path file, HoundConfig config,
                                     ArcadeDBSemanticWriter writer,
                                     CanonicalPool pool, String dbName) throws IOException {
        AnalysisResult ar = analyzeFile(file, config, new AtomicLong(System.currentTimeMillis()), dbName);
        if (ar.semantic() == null) return emptyResult(file.toString());

        if (writer != null) {
            writer.saveResult(ar.semantic(), ar.timer(), pool, dbName);
        }
        return toParseResult(ar.semantic(), ar.timer());
    }

    // ─── SemanticResult → ParseResult ────────────────────────────

    private static ParseResult toParseResult(SemanticResult sem, PipelineTimer timer) {
        Structure s = sem.getStructure();

        int atomCount = sem.getAtoms() != null ? sem.getAtoms().size() : 0;

        int vertexCount = s == null ? 0 :
                mapSize(s.getTables()) + mapSize(s.getColumns()) + mapSize(s.getRoutines())
                + mapSize(s.getStatements()) + mapSize(s.getPackages())
                + mapSize(s.getSchemas()) + mapSize(s.getDatabases());

        int edgeCount = sem.getLineage() != null ? sem.getLineage().size() : 0;

        double resolutionRate = calcResolutionRate(sem);

        long durationMs = timer.ms("parse") + timer.ms("walk")
                + timer.ms("resolve") + timer.writeMs();

        return new ParseResult(sem.getFilePath(), atomCount, vertexCount, edgeCount,
                resolutionRate, List.of(), List.of(), durationMs);
    }

    private static double calcResolutionRate(SemanticResult sem) {
        List<Map<String, Object>> log = sem.getResolutionLog();
        if (log == null || log.isEmpty()) return 1.0;
        long resolved = log.stream()
                .filter(e -> "RESOLVED".equals(e.get("result_kind")))
                .count();
        return (double) resolved / log.size();
    }

    private static ParseResult emptyResult(String file) {
        return new ParseResult(file, 0, 0, 0, 1.0, List.of(), List.of(), 0L);
    }

    private static ParseResult errorResult(String file, String message) {
        return new ParseResult(file, 0, 0, 0, 0.0,
                List.of(), List.of("ERROR: " + message), 0L);
    }

    // ─── Writer factory ──────────────────────────────────────────

    private static ArcadeDBSemanticWriter createWriter(HoundConfig config) {
        if (config.writeMode() == ArcadeWriteMode.DISABLED
                || config.writeMode() == ArcadeWriteMode.EMBEDDED) {
            return null;
        }
        URI uri = URI.create(config.arcadeUrl());
        String host = uri.getHost();
        int    port = uri.getPort() > 0 ? uri.getPort() : 2480;
        boolean batch = config.writeMode() == ArcadeWriteMode.REMOTE_BATCH;
        return new ArcadeDBSemanticWriter(
                host, port, config.arcadeDbName(),
                config.arcadeUser(), config.arcadePassword(), batch);
    }

    // ─── Dialect listener + ANTLR ────────────────────────────────

    private static Object createDialectListener(String dialect, UniversalSemanticEngine engine,
                                                 String defaultSchema) {
        return switch (dialect.toLowerCase()) {
            case "plsql" -> {
                PlSqlSemanticListener l = new PlSqlSemanticListener(engine);
                if (defaultSchema != null && !defaultSchema.isBlank()) {
                    l.setDefaultSchema(defaultSchema);
                }
                yield l;
            }
            default -> throw new IllegalArgumentException("Dialect not implemented: " + dialect);
        };
    }

    private static void parseAndWalk(String sql, String dialect, Object listener,
                                      PipelineTimer timer) {
        switch (dialect.toLowerCase()) {
            case "plsql" -> {
                timer.start("parse");
                PlSqlLexer lexer             = new PlSqlLexer(CharStreams.fromString(sql));
                CommonTokenStream tokens     = new CommonTokenStream(lexer);
                PlSqlParser parser           = new PlSqlParser(tokens);
                PlSqlParser.Sql_scriptContext tree = parser.sql_script();
                timer.stop("parse");
                timer.count("tokens", tokens.getNumberOfOnChannelTokens());

                timer.start("walk");
                ParseTreeWalker.DEFAULT.walk((PlSqlSemanticListener) listener, tree);
                timer.stop("walk");
            }
            default -> throw new IllegalArgumentException("Parser not implemented: " + dialect);
        }
    }

    // ─── File reading ─────────────────────────────────────────────

    private static String readFileWithFallback(Path file) throws IOException {
        try {
            return java.nio.file.Files.readString(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            logger.debug("UTF-8 decode failed for {}, trying Windows-1251", file.getFileName());
        }
        try {
            String content = java.nio.file.Files.readString(file, Charset.forName("Windows-1251"));
            logger.warn("File read as Windows-1251 (non-UTF-8): {}", file.getFileName());
            return content;
        } catch (MalformedInputException e) {
            logger.debug("Windows-1251 failed for {}, using ISO-8859-1", file.getFileName());
        }
        logger.warn("File read as ISO-8859-1 (fallback): {}", file.getFileName());
        return java.nio.file.Files.readString(file, StandardCharsets.ISO_8859_1);
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private static int mapSize(Map<?, ?> m) { return m != null ? m.size() : 0; }

    /** Internal result of the parse phase (before DB write). */
    private record AnalysisResult(Path file, SemanticResult semantic, PipelineTimer timer) {}
}
