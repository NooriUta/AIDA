// src/main/java/com/hound/HoundParserImpl.java
package com.hound;

import com.hound.api.*;
import com.hound.metrics.PipelineTimer;
import com.hound.parser.AntlrErrorCollector;
import com.hound.semantic.model.AtomInfo;
import com.hound.parser.base.grammars.sql.clickhouse.ClickHouseLexer;
import com.hound.parser.base.grammars.sql.clickhouse.ClickHouseParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLLexer;
import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLParser;
import com.hound.processor.ThreadPoolManager;
import com.hound.semantic.dialect.clickhouse.ClickHouseSemanticListener;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.dialect.postgresql.PostgreSQLSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.SemanticResult;
import com.hound.semantic.model.Structure;
import com.hound.parser.ConditionalCompilationPreprocessor;
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
import com.hound.storage.WriteStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
        return parse(file, config, HoundHeimdallListener.fromSystemProperty());
    }

    @Override
    public ParseResult parse(Path file, HoundConfig config, HoundEventListener listener) {
        listener.onFileParseStarted(file.toString(), config.dialect());
        try (ArcadeDBSemanticWriter writer = createWriter(config)) {
            ParseResult result = doParseFile(file, config, writer, null, null, listener);
            listener.onFileParseCompleted(file.toString(), result);
            return result;
        } catch (Exception e) {
            listener.onError(file.toString(), e);
            throw new RuntimeException("Hound parse failed: " + file, e);
        }
    }

    @Override
    public ParseResult parse(Path file, HoundConfig config, String dbName, String appName,
                             HoundEventListener listener) {
        listener.onFileParseStarted(file.toString(), config.dialect());
        try (ArcadeDBSemanticWriter writer = createWriter(config)) {
            CanonicalPool pool = null;
            if (writer != null && dbName != null && !dbName.isBlank()) {
                String resolvedApp = (appName != null && !appName.isBlank()) ? appName : dbName;
                pool = writer.ensureCanonicalPool(dbName, resolvedApp, resolvedApp);
            }
            ParseResult result = doParseFile(file, config, writer, pool, dbName, listener);
            listener.onFileParseCompleted(file.toString(), result);
            return result;
        } catch (Exception e) {
            listener.onError(file.toString(), e);
            throw new RuntimeException("Hound parse failed: " + file, e);
        }
    }

    @Override
    public List<ParseResult> parseBatch(List<Path> files, HoundConfig config) {
        return parseBatch(files, config, HoundHeimdallListener.fromSystemProperty());
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
                    return analyzeFile(file, config, sessionSeq, schema, listener);
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
                        WriteStats ws = null;
                        if (writer != null) {
                            ws = writer.saveResult(ar.semantic(), ar.timer(), pool, schema);
                        }
                        pr = toParseResult(ar.semantic(), ar.timer(), ws, ar.parseErrors(), ar.parseWarnings());
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

    @Override
    public List<ParseResult> parseSources(List<SqlSource> sources, HoundConfig config,
                                           HoundEventListener listener) {
        if (sources.isEmpty()) return List.of();

        try (ArcadeDBSemanticWriter writer = createWriter(config)) {
            String schema = config.targetSchema();
            CanonicalPool pool = (writer != null && schema != null && !schema.isBlank())
                    ? writer.ensureCanonicalPool(schema, schema, schema)
                    : null;

            AtomicLong sessionSeq = new AtomicLong(System.currentTimeMillis());
            int threads = config.workerThreads();
            List<Future<AnalysisResult>> futures = new ArrayList<>(sources.size());
            ThreadPoolManager tm = ThreadPoolManager.newFixedThreadPool(threads);

            for (SqlSource source : sources) {
                futures.add(tm.submit(() -> {
                    listener.onFileParseStarted(source.sourceName(), config.dialect());
                    return switch (source) {
                        case SqlSource.FromFile f ->
                                analyzeFile(f.path(), config, sessionSeq, schema, listener);
                        case SqlSource.FromText t ->
                                analyzeSqlText(t.sql(), t.sourceName(), config, sessionSeq, schema, listener);
                    };
                }));
            }
            tm.shutdownAndWait();

            List<ParseResult> results = new ArrayList<>(sources.size());
            for (int i = 0; i < futures.size(); i++) {
                SqlSource source = sources.get(i);
                try {
                    AnalysisResult ar = futures.get(i).get();
                    ParseResult pr;
                    if (ar.semantic() != null) {
                        WriteStats ws = null;
                        if (writer != null) {
                            ws = writer.saveResult(ar.semantic(), ar.timer(), pool, schema);
                        }
                        pr = toParseResult(ar.semantic(), ar.timer(), ws,
                                ar.parseErrors(), ar.parseWarnings());
                    } else {
                        pr = emptyResult(source.sourceName());
                    }
                    listener.onFileParseCompleted(source.sourceName(), pr);
                    results.add(pr);
                } catch (ExecutionException e) {
                    logger.error("parseSources failed: {} — {}", source.sourceName(),
                            e.getCause().getMessage(), e.getCause());
                    listener.onError(source.sourceName(), e.getCause());
                    results.add(errorResult(source.sourceName(), e.getCause().getMessage()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    listener.onError(source.sourceName(), e);
                    results.add(errorResult(source.sourceName(), "Interrupted"));
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Hound parseSources failed", e);
        }
    }

    @Override
    public void cleanAll(HoundConfig config) {
        try (ArcadeDBSemanticWriter writer = createWriter(config)) {
            if (writer != null) {
                writer.cleanAll();
                logger.info("cleanAll: YGG truncated (writeMode={})", config.writeMode());
            }
        } catch (Exception e) {
            logger.warn("cleanAll failed: {}", e.getMessage());
        }
    }

    // ─── Core parse logic ─────────────────────────────────────────

    /**
     * Single-file parse: read → SQL*Plus strip → ANTLR → semantic walk → resolve.
     * Does NOT write to DB — caller decides when/how to write.
     */
    private AnalysisResult analyzeFile(Path file, HoundConfig config,
                                        AtomicLong sessionSeq, String defaultSchema,
                                        HoundEventListener listener)
            throws IOException {
        String rawSql = readFileWithFallback(file);
        if (rawSql.isBlank()) {
            logger.warn("Empty file: {}", file);
            return new AnalysisResult(file, null, new PipelineTimer(), List.of(), List.of());
        }

        // SQL*Plus directives (SET, PROMPT, WHENEVER, SHOW, CLEAR, START, EXIT, TIMING)
        // are handled by the grammar's sql_plus_command rule (PlSqlParser.g4:7174).
        // Pass raw SQL — ANTLR's error recovery handles unknown commands gracefully.
        String sql = rawSql;

        long lineCount = sql.lines().count();
        PipelineTimer timer = new PipelineTimer();
        timer.count("lines", (int) lineCount);

        // Pass listener so AtomProcessor/StructureAndLineageBuilder fire events (C.1.3)
        UniversalSemanticEngine engine = new UniversalSemanticEngine(listener, file.toString());
        Object dialectListener = createDialectListener(config.dialect(), engine, defaultSchema);
        ParseOutcome outcome = parseAndWalk(sql, config.dialect(), dialectListener,
                                            file.toString(), listener, timer);

        timer.start("resolve");
        engine.resolvePendingColumns();
        timer.stop("resolve");

        long parseWalkResolveMs = timer.ms("parse") + timer.ms("walk") + timer.ms("resolve");
        // Use caller-supplied session ID (e.g. Dali UUID) when provided via extra map,
        // otherwise fall back to timestamp-counter for backward-compat batch runs.
        String explicitSid = config.extra().get("hound.session.id");
        String sid = (explicitSid != null && !explicitSid.isBlank())
                ? explicitSid
                : "session-" + sessionSeq.getAndIncrement();
        SemanticResult result = engine.getResult(
                sid,
                file.toString(),
                config.dialect(),
                parseWalkResolveMs
        ).withRawScript(rawSql);

        return new AnalysisResult(file, result, timer, outcome.errors(), outcome.warnings());
    }

    /**
     * Single-file parse + optional write (for {@link #parse(Path, HoundConfig, HoundEventListener)}).
     */
    private ParseResult doParseFile(Path file, HoundConfig config,
                                     ArcadeDBSemanticWriter writer,
                                     CanonicalPool pool, String dbName,
                                     HoundEventListener listener) throws IOException {
        AnalysisResult ar = analyzeFile(file, config,
                new AtomicLong(System.currentTimeMillis()), dbName, listener);
        if (ar.semantic() == null) return emptyResult(file.toString());

        WriteStats ws = null;
        if (writer != null) {
            ws = writer.saveResult(ar.semantic(), ar.timer(), pool, dbName);
        }
        return toParseResult(ar.semantic(), ar.timer(), ws, ar.parseErrors(), ar.parseWarnings());
    }

    // ─── SemanticResult → ParseResult ────────────────────────────

    private static ParseResult toParseResult(SemanticResult sem, PipelineTimer timer, WriteStats ws,
                                              List<String> parseErrors, List<String> parseWarnings) {
        Structure s = sem.getStructure();

        int atomCount;
        int vertexCount;
        int edgeCount;
        int droppedEdgeCount;
        Map<String, int[]> vertexStats;

        if (ws != null && ws.totalInserted() > 0) {
            // REMOTE_BATCH mode: use actual per-type stats from the batch write
            atomCount        = ws.atomsInserted();
            vertexCount      = ws.totalInserted();
            edgeCount        = ws.edgeCount();
            droppedEdgeCount = ws.droppedEdgeCount();
            vertexStats      = ws.snapshot();
        } else {
            // DISABLED or REMOTE mode: use semantic model as best approximation
            // Count individual atoms across statement + unattached containers only.
            // "routine" containers are a duplicate view of statement atoms — exclude them
            // to avoid double-counting. "summary" has no "atoms" key — skipped automatically.
            int totalAtoms = 0;
            if (sem.getAtoms() != null) {
                for (var entry : sem.getAtoms().entrySet()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> cont = (Map<String, Object>) entry.getValue();
                    String srcType = (String) cont.get("source_type");
                    if ("routine".equals(srcType)) continue; // duplicate view
                    @SuppressWarnings("unchecked")
                    Map<?, ?> atoms = (Map<?, ?>) cont.get("atoms");
                    if (atoms != null) totalAtoms += atoms.size();
                }
            }
            atomCount = totalAtoms;
            vertexCount = s == null ? 0 :
                    mapSize(s.getTables()) + mapSize(s.getColumns()) + mapSize(s.getRoutines())
                    + mapSize(s.getStatements()) + mapSize(s.getPackages())
                    + mapSize(s.getSchemas()) + mapSize(s.getDatabases());
            edgeCount        = sem.getLineage() != null ? sem.getLineage().size() : 0;
            droppedEdgeCount = 0;
            vertexStats      = Map.of();
        }

        ResStats res = calcResStats(sem);

        long durationMs = timer.ms("parse") + timer.ms("walk")
                + timer.ms("resolve") + timer.writeMs();

        // Genuine ANTLR4 syntax errors → ParseResult.errors() (isSuccess = false).
        // Known grammar limitations (DATE literal in DEFAULT, UPDATE alias, etc.) →
        // ParseResult.warnings() (isSuccess stays true — parse completed, result is usable).
        List<String> fileErrors   = parseErrors   != null ? parseErrors   : List.of();
        List<String> fileWarnings = parseWarnings != null ? parseWarnings : List.of();
        return new ParseResult(sem.getFilePath(), atomCount, vertexCount, edgeCount,
                droppedEdgeCount, vertexStats, res.rate(), res.resolved(), res.unresolved(),
                fileWarnings, fileErrors, durationMs);
    }

    /**
     * Column-level resolution stats: rate + resolved/unresolved counts.
     *
     * <p>Only column-reference atoms are counted (constants and function-calls are excluded —
     * they never require name resolution and would artificially dilute the denominator).
     *
     * <p>Returns rate=1.0, counts=0 when there are no column-reference atoms.
     */
    private record ResStats(double rate, int resolved, int unresolved) {}

    private static ResStats calcResStats(SemanticResult sem) {
        List<Map<String, Object>> log = sem.getResolutionLog();
        if (log == null || log.isEmpty()) return new ResStats(1.0, 0, 0);

        int resolved   = (int) log.stream()
                .filter(e -> AtomInfo.STATUS_RESOLVED.equals(e.get("result_kind")))
                .count();
        int unresolved = (int) log.stream()
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .count();
        int denominator = resolved + unresolved;
        double rate = denominator == 0 ? 1.0 : (double) resolved / denominator;
        return new ResStats(rate, resolved, unresolved);
    }

    private static ParseResult emptyResult(String file) {
        return new ParseResult(file, 0, 0, 0, 0, Map.of(), 1.0, 0, 0, List.of(), List.of(), 0L);
    }

    private static ParseResult errorResult(String file, String message) {
        return new ParseResult(file, 0, 0, 0, 0, Map.of(), 0.0, 0, 0,
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
            case "postgresql" -> {
                PostgreSQLSemanticListener l = new PostgreSQLSemanticListener(engine);
                if (defaultSchema != null && !defaultSchema.isBlank()) {
                    l.setDefaultSchema(defaultSchema);
                }
                yield l;
            }
            case "clickhouse" -> {
                ClickHouseSemanticListener l = new ClickHouseSemanticListener(engine);
                if (defaultSchema != null && !defaultSchema.isBlank()) {
                    l.setDefaultSchema(defaultSchema);
                }
                yield l;
            }
            default -> throw new IllegalArgumentException("Dialect not implemented: " + dialect);
        };
    }

    private static ParseOutcome parseAndWalk(String sql, String dialect, Object listener,
                                              String filePath, HoundEventListener eventListener,
                                              PipelineTimer timer) {
        return switch (dialect.toLowerCase()) {
            case "plsql" -> {
                AntlrErrorCollector errorCollector =
                        new AntlrErrorCollector(filePath, eventListener);

                // KI-CONDCOMP-1: strip $IF/$ELSIF/$ELSE/$END directives before lexing
                String preprocessed = new ConditionalCompilationPreprocessor().expand(sql);

                timer.start("parse");
                PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(preprocessed));
                lexer.removeErrorListeners();
                lexer.addErrorListener(errorCollector);

                CommonTokenStream tokens = new CommonTokenStream(lexer);
                PlSqlParser parser = new PlSqlParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(errorCollector);

                PlSqlParser.Sql_scriptContext tree = parser.sql_script();
                timer.stop("parse");
                timer.count("tokens", tokens.getNumberOfOnChannelTokens());

                if (errorCollector.hasErrors()) {
                    logger.warn("[ANTLR4] {} syntax error(s) in: {}",
                            errorCollector.getErrors().size(), basename(filePath));
                }

                timer.start("walk");
                ParseTreeWalker.DEFAULT.walk((PlSqlSemanticListener) listener, tree);
                timer.stop("walk");

                yield new ParseOutcome(errorCollector.getErrors(), errorCollector.getGrammarLimitations());
            }
            case "postgresql" -> {
                AntlrErrorCollector pgCollector =
                        new AntlrErrorCollector(filePath, eventListener);

                timer.start("parse");
                PostgreSQLLexer pgLexer = new PostgreSQLLexer(CharStreams.fromString(sql));
                pgLexer.removeErrorListeners();
                pgLexer.addErrorListener(pgCollector);

                CommonTokenStream pgTokens = new CommonTokenStream(pgLexer);
                PostgreSQLParser pgParser = new PostgreSQLParser(pgTokens);
                pgParser.removeErrorListeners();
                pgParser.addErrorListener(pgCollector);

                PostgreSQLParser.RootContext pgTree = pgParser.root();
                timer.stop("parse");
                timer.count("tokens", pgTokens.getNumberOfOnChannelTokens());

                if (pgCollector.hasErrors()) {
                    logger.warn("[ANTLR4] {} syntax error(s) in: {}",
                            pgCollector.getErrors().size(), basename(filePath));
                }

                timer.start("walk");
                ParseTreeWalker.DEFAULT.walk((PostgreSQLSemanticListener) listener, pgTree);
                timer.stop("walk");

                yield new ParseOutcome(pgCollector.getErrors(), pgCollector.getGrammarLimitations());
            }
            case "clickhouse" -> {
                AntlrErrorCollector chCollector =
                        new AntlrErrorCollector(filePath, eventListener);

                timer.start("parse");
                ClickHouseLexer chLexer = new ClickHouseLexer(CharStreams.fromString(sql));
                chLexer.removeErrorListeners();
                chLexer.addErrorListener(chCollector);

                CommonTokenStream chTokens = new CommonTokenStream(chLexer);
                ClickHouseParser chParser = new ClickHouseParser(chTokens);
                chParser.removeErrorListeners();
                chParser.addErrorListener(chCollector);

                ClickHouseParser.ClickhouseFileContext chTree = chParser.clickhouseFile();
                timer.stop("parse");
                timer.count("tokens", chTokens.getNumberOfOnChannelTokens());

                if (chCollector.hasErrors()) {
                    logger.warn("[ANTLR4] {} syntax error(s) in: {}",
                            chCollector.getErrors().size(), basename(filePath));
                }

                timer.start("walk");
                ParseTreeWalker.DEFAULT.walk((ClickHouseSemanticListener) listener, chTree);
                timer.stop("walk");

                yield new ParseOutcome(chCollector.getErrors(), chCollector.getGrammarLimitations());
            }
            default -> throw new IllegalArgumentException("Parser not implemented: " + dialect);
        };
    }

    /**
     * Parses in-memory SQL text — the {@link com.hound.api.SqlSource.FromText} counterpart of
     * {@link #analyzeFile}.
     *
     * <p>No disk I/O. SQL is already in memory from a SKADI/ULLR harvest.
     * SQL*Plus stripping is applied only for the {@code "plsql"} dialect.
     */
    private AnalysisResult analyzeSqlText(String rawSql, String sourceName, HoundConfig config,
                                           AtomicLong sessionSeq, String defaultSchema,
                                           HoundEventListener listener) {
        if (rawSql == null || rawSql.isBlank()) {
            logger.warn("Empty SQL text for source: {}", sourceName);
            return new AnalysisResult(null, null, new PipelineTimer(), List.of(), List.of());
        }

        String sql = rawSql;

        long lineCount = sql.lines().count();
        PipelineTimer timer = new PipelineTimer();
        timer.count("lines", (int) lineCount);

        UniversalSemanticEngine engine = new UniversalSemanticEngine(listener, sourceName);
        Object dialectListener = createDialectListener(config.dialect(), engine, defaultSchema);
        ParseOutcome outcome = parseAndWalk(sql, config.dialect(), dialectListener,
                sourceName, listener, timer);

        timer.start("resolve");
        engine.resolvePendingColumns();
        timer.stop("resolve");

        long parseWalkResolveMs = timer.ms("parse") + timer.ms("walk") + timer.ms("resolve");
        String explicitSidText = config.extra().get("hound.session.id");
        String sidText = (explicitSidText != null && !explicitSidText.isBlank())
                ? explicitSidText
                : "session-" + sessionSeq.getAndIncrement();
        SemanticResult result = engine.getResult(
                sidText,
                sourceName,
                config.dialect(),
                parseWalkResolveMs
        ).withRawScript(rawSql);

        return new AnalysisResult(null, result, timer, outcome.errors(), outcome.warnings());
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

    private static String basename(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /** Internal result of the parse phase (before DB write). */
    private record AnalysisResult(Path file, SemanticResult semantic, PipelineTimer timer,
                                   List<String> parseErrors, List<String> parseWarnings) {}

    /** Errors + grammar-limitation warnings returned from {@link #parseAndWalk}. */
    private record ParseOutcome(List<String> errors, List<String> warnings) {
        static ParseOutcome empty() { return new ParseOutcome(List.of(), List.of()); }
    }
}
