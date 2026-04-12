// src/main/java/com/hound/HoundApplication.java
package com.hound;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.*;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.storage.ArcadeDBSemanticWriter;
import com.hound.storage.CanonicalPool;
import com.hound.diagnostic.DiagnosticRunner;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.cli.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hound.metrics.PipelineTimer;

import com.hound.processor.ThreadPoolManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Главный класс приложения Hound.
 *
 * Использование:
 *   hound --input path/to/dir --language plsql
 *   hound --input path/to/dir --language plsql --arcade-db ./data/hound-db
 *   hound --input path/to/dir --language plsql --db-type arcadedb --db-host localhost --db-port 2480 --db-name hound
 *   hound path/to/file.sql
 */
public class HoundApplication {

    private static final Logger logger = LoggerFactory.getLogger(HoundApplication.class);

    private static final String[] SQL_EXTENSIONS = {
            ".sql", ".plsql", ".pls",
            ".pks", ".pkb", ".pkh", ".pck" ,
            ".prc", ".fnc",
            ".trg", ".vw", ".typ",
            ".ddl", ".dml"
    };

    public static void main(String[] args) {
        try {
            RunConfig config = parseArguments(args);
            new HoundApplication().run(config);
        } catch (ParseException e) {
            System.err.println("Ошибка аргументов: " + e.getMessage());
            printHelp();
            System.exit(1);
        } catch (Exception e) {
            logger.error("Application error", e);
            System.exit(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Run
    // ═══════════════════════════════════════════════════════════════

    public void run(RunConfig config) throws Exception {
        logger.info("=== Hound Semantic Engine v1.0.0 ===");
        logger.info("Input    : {}", config.inputPath);
        logger.info("Language : {}", config.language);

        // Создаём writer (embedded или remote или null)
        ArcadeDBSemanticWriter writer = createWriter(config);

        // --clean: удалить все данные перед запуском
        if (config.clean && writer != null) {
            logger.info("CLEAN    : deleting all Dali records...");
            writer.cleanAll();
        }

        Path target = Path.of(config.inputPath);
        if (!Files.exists(target)) {
            if (writer != null) writer.close();
            throw new IllegalArgumentException("Не найдено: " + target);
        }

        try {
            if (Files.isDirectory(target)) {
                processDirectory(target, config, writer);
            } else {
                processFile(target, config, writer);
            }

            // --diag: запустить диагностику после обработки
            if (config.diag && writer != null) {
                runDiagnostics(config, writer);
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    /**
     * Запускает DiagnosticRunner после обработки файлов.
     * Для embedded — открывает БД напрямую.
     * Для remote — использует remote query API.
     */
    private void runDiagnostics(RunConfig config, ArcadeDBSemanticWriter writer) {
        System.out.println();
        if (config.arcadeDbPath != null) {
            // Embedded: close current writer, reopen read-only for diagnostics
            writer.close();
            com.arcadedb.database.DatabaseFactory factory =
                    new com.arcadedb.database.DatabaseFactory(config.arcadeDbPath);
            if (factory.exists()) {
                try (com.arcadedb.database.Database diagDb = factory.open()) {
                    new DiagnosticRunner(diagDb).runAll();
                }
            }
        } else if ("arcadedb".equalsIgnoreCase(config.dbType)) {
            // Remote: use DiagnosticRunner with RemoteDatabase
            var remoteDb = new com.arcadedb.remote.RemoteDatabase(
                    config.dbHost, config.dbPort, config.dbName,
                    config.dbUser, config.dbPassword);
            try {
                new DiagnosticRunner(remoteDb).runAll();
            } finally {
                try { remoteDb.close(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Создаёт writer в зависимости от параметров:
     *   --arcade-db path          → embedded mode
     *   --db-type arcadedb        → remote mode (к Docker серверу)
     *   ничего                     → null (без записи)
     */
    private ArcadeDBSemanticWriter createWriter(RunConfig config) {
        // Embedded mode
        if (config.arcadeDbPath != null) {
            logger.info("ArcadeDB : EMBEDDED {}", config.arcadeDbPath);
            return new ArcadeDBSemanticWriter(config.arcadeDbPath);
        }

        // Remote mode
        if ("arcadedb".equalsIgnoreCase(config.dbType)) {
            logger.info("ArcadeDB : REMOTE{} {}:{}/{} user={}",
                    config.useBatch ? "_BATCH" : "",
                    config.dbHost, config.dbPort, config.dbName, config.dbUser);
            return new ArcadeDBSemanticWriter(
                    config.dbHost, config.dbPort, config.dbName,
                    config.dbUser, config.dbPassword,
                    config.useBatch);
        }

        // Нет записи
        logger.info("Storage  : disabled (используйте --arcade-db или --db-type arcadedb)");
        return null;
    }

    /** Результат анализа одного файла (без записи в БД). */
    private record AnalysisResult(Path file, SemanticResult semantic, PipelineTimer timer) {}

    /**
     * Группа файлов, принадлежащих одной базе данных (namespace).
     *
     * <ul>
     *   <li>{@code dbName = null} — ad-hoc режим (одиночные файлы без namespace)
     *   <li>{@code dbName != null} — имя папки = имя БД (DWH, ODS, DWH2, ...)
     * </ul>
     */
    private record DatabaseBatch(String dbName, List<Path> files) {}

    // ═══════════════════════════════════════════════════════════════
    // Directory
    // ═══════════════════════════════════════════════════════════════

    /**
     * Обрабатывает директорию с автоматическим определением режима namespace:
     * <ul>
     *   <li><b>Multi-DB</b>: поддиректории присутствуют → корень = DaliApplication,
     *       каждая поддиректория (уровень 2) = DaliDatabase; SQL-файлы уровней 3+
     *       рекурсивно относятся к своей level-2 базе данных.
     *   <li><b>Single-DB</b>: нет поддиректорий (или есть файлы прямо в корне) →
     *       корень = и DaliApplication и DaliDatabase (одно имя).
     * </ul>
     *
     * <p>Имя приложения: {@code --app-name} как override; иначе — имя корневой директории.
     */
    private void processDirectory(Path dir, RunConfig config, ArcadeDBSemanticWriter writer)
            throws IOException {
        // Имя приложения: явный --app-name или имя корневой директории
        String appName = (config.appName != null && !config.appName.isBlank())
                ? config.appName
                : dir.getFileName().toString();

        List<DatabaseBatch> batches = detectDatabases(dir);
        if (batches.isEmpty()) {
            logger.warn("SQL-файлы не найдены в: {}", dir);
            return;
        }

        int totalFiles = batches.stream().mapToInt(b -> b.files().size()).sum();
        logger.info("App: '{}' | {} баз | {} файлов", appName, batches.size(), totalFiles);

        List<PipelineTimer> allTimers = new ArrayList<>();
        List<String>        allNames  = new ArrayList<>();
        int totalSuccess = 0, totalFailed = 0;

        for (DatabaseBatch batch : batches) {
            logger.info("==> Database: '{}' ({} файлов)", batch.dbName(), batch.files().size());
            int[] sf = processBatch(batch, appName, config, writer, allTimers, allNames);
            totalSuccess += sf[0];
            totalFailed  += sf[1];
        }

        printSummary(allTimers, allNames, totalSuccess, totalFailed);
    }

    /**
     * Определяет разбивку директории на DatabaseBatch.
     *
     * <p>Правило:
     * <ol>
     *   <li>Есть поддиректории и нет SQL-файлов прямо в {@code dir} →
     *       Multi-DB: каждый поддир (уровень 2) = БД; файлы уровней 3+ рекурсивно
     *       читаются под своей level-2 БД и не создают отдельных DaliDatabase.
     *   <li>Нет поддиректорий, или есть SQL-файлы прямо в корне →
     *       Single-DB: dbName = dir.getFileName(); все файлы рекурсивно.
     *   <li>Нет SQL-файлов нигде → пустой список.
     * </ol>
     */
    private List<DatabaseBatch> detectDatabases(Path dir) throws IOException {
        // Файлы SQL прямо в dir (не рекурсивно)
        List<Path> rootSql;
        try (Stream<Path> s = Files.list(dir)) {
            rootSql = s.filter(Files::isRegularFile)
                       .filter(HoundApplication::isSqlFile)
                       .sorted()
                       .toList();
        }

        // Поддиректории уровня 2
        List<Path> subdirs;
        try (Stream<Path> s = Files.list(dir)) {
            subdirs = s.filter(Files::isDirectory).sorted().toList();
        }

        if (!subdirs.isEmpty()) {
            // Multi-DB: поддиректории есть → всегда Multi-DB.
            // Файлы прямо в корне игнорируются (они не принадлежат ни одной DB).
            if (!rootSql.isEmpty()) {
                logger.warn("Multi-DB режим: {} файл(ов) в корне '{}' пропущены (не принадлежат ни одной DB)",
                        rootSql.size(), dir.getFileName());
            }
            List<DatabaseBatch> batches = new ArrayList<>();
            for (Path sub : subdirs) {
                List<Path> files = findAllSql(sub);  // рекурсивно — уровни 3+ просто вычитываются
                if (!files.isEmpty()) {
                    String dbName = sub.getFileName().toString();
                    logger.debug("Multi-DB: db='{}', files={}", dbName, files.size());
                    batches.add(new DatabaseBatch(dbName, files));
                }
            }
            return batches;
        }

        // Single-DB: нет поддиректорий → вся папка = одна база данных
        List<Path> allFiles = findAllSql(dir);
        if (allFiles.isEmpty()) return List.of();
        String dbName = dir.getFileName().toString();
        logger.debug("Single-DB: db='{}', files={}", dbName, allFiles.size());
        return List.of(new DatabaseBatch(dbName, allFiles));
    }

    /** Рекурсивно собирает все SQL-файлы в директории, отсортированные. */
    private static List<Path> findAllSql(Path dir) throws IOException {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(HoundApplication::isSqlFile)
                    .sorted()
                    .toList();
        }
    }

    /**
     * Обрабатывает один DatabaseBatch: Phase 1 (параллельный разбор) + Phase 2 (запись).
     * Создаёт CanonicalPool если есть db_name и writer.
     *
     * @param appName имя приложения (DaliApplication): корневая директория или --app-name override
     * @return int[]{success, failed}
     */
    private int[] processBatch(DatabaseBatch batch, String appName, RunConfig config,
                                ArcadeDBSemanticWriter writer,
                                List<PipelineTimer> allTimers, List<String> allNames)
            throws IOException {
        List<Path> sqlFiles = batch.files();
        int threads = config.threads;

        // Создаём CanonicalPool для этой базы данных.
        // appName = имя корневой директории (или --app-name override).
        // dbName  = имя поддиректории (Multi-DB) или корневой директории (Single-DB).
        CanonicalPool pool = (writer != null)
                ? writer.ensureCanonicalPool(batch.dbName(), appName, appName)
                : null;

        // session-id counter — уникален при параллельном запуске
        AtomicLong sessionSeq = new AtomicLong(System.currentTimeMillis());

        // ── Phase 1: параллельный parse + walk + resolve ──
        List<Future<AnalysisResult>> futures = new ArrayList<>(sqlFiles.size());
        ThreadPoolManager threadPool = ThreadPoolManager.newFixedThreadPool(threads);
        for (Path file : sqlFiles) {
            futures.add(threadPool.submit(() -> analyzeFile(file, config, sessionSeq, batch.dbName())));
        }
        threadPool.shutdownAndWait();

        // ── Phase 2: последовательная запись в БД + сбор статистики ──
        int success = 0, failed = 0;

        for (Future<AnalysisResult> f : futures) {
            try {
                AnalysisResult ar = f.get();
                if (ar.semantic() != null) {
                    if (writer != null) {
                        writer.saveResult(ar.semantic(), ar.timer(), pool, batch.dbName());
                    }
                    printResult(ar.file(), ar.semantic(), ar.timer().count("lines"), ar.timer());
                }
                allTimers.add(ar.timer());
                allNames.add(ar.file().getFileName().toString());
                success++;
            } catch (ExecutionException e) {
                logger.error("Ошибка: {} — {}", "<file>", e.getCause().getMessage(), e.getCause());
                failed++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Прерывание при ожидании результата", e);
                failed++;
            }
        }

        if (pool != null) {
            logger.info("CanonicalPool '{}': {} схем, {} таблиц, {} колонок", batch.dbName(),
                    pool.schemaCount(), pool.tableCount(), pool.columnCount());
        }
        return new int[]{success, failed};
    }

    /**
     * Parse + walk + resolve для одного файла.
     * Не пишет в БД — результат собирается в Phase 2.
     * Потокобезопасен: каждый вызов создаёт свои engine/listener.
     */
    private AnalysisResult analyzeFile(Path file, RunConfig config, AtomicLong sessionSeq,
                                        String defaultSchema)
            throws IOException {
        String sql = readFileWithFallback(file);
        if (sql.isBlank()) {
            logger.warn("Пустой файл: {}", file);
            return new AnalysisResult(file, null, new PipelineTimer());
        }

        long lineCount = sql.lines().count();
        PipelineTimer timer = new PipelineTimer();
        timer.count("lines", (int) lineCount);

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        Object listener = createListener(config.language, engine, defaultSchema);
        parseAndWalk(sql, config.language, listener, timer);

        timer.start("resolve");
        engine.resolvePendingColumns();
        timer.stop("resolve");

        long parseWalkResolveMs = timer.ms("parse") + timer.ms("walk") + timer.ms("resolve");
        SemanticResult result = engine.getResult(
                "session-" + sessionSeq.getAndIncrement(),
                file.toString(),
                config.language,
                parseWalkResolveMs
        ).withRawScript(sql);

        return new AnalysisResult(file, result, timer);
    }

    // ═══════════════════════════════════════════════════════════════
    // File
    // ═══════════════════════════════════════════════════════════════

    private PipelineTimer processFile(Path file, RunConfig config, ArcadeDBSemanticWriter writer)
            throws IOException {
        String sql = readFileWithFallback(file);
        if (sql.isBlank()) {
            logger.warn("Пустой файл: {}", file);
            return new PipelineTimer();
        }

        long lineCount = sql.lines().count();
        PipelineTimer timer = new PipelineTimer();
        timer.count("lines", (int) lineCount);

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        Object listener = createListener(config.language, engine);
        parseAndWalk(sql, config.language, listener, timer);

        timer.start("resolve");
        engine.resolvePendingColumns();
        timer.stop("resolve");

        long parseWalkResolveMs = timer.ms("parse") + timer.ms("walk") + timer.ms("resolve");
        SemanticResult result = engine.getResult(
                "session-" + System.currentTimeMillis(),
                file.toString(),
                config.language,
                parseWalkResolveMs
        ).withRawScript(sql);

        if (writer != null) {
            // Ad-hoc mode: single file, no database namespace (pool=null, dbName=null)
            writer.saveResult(result, timer, null, null);
        }

        printResult(file, result, lineCount, timer);
        return timer;
    }

    // ═══════════════════════════════════════════════════════════════
    // Dialect
    // ═══════════════════════════════════════════════════════════════

    private Object createListener(String language, UniversalSemanticEngine engine) {
        return createListener(language, engine, null);
    }

    private Object createListener(String language, UniversalSemanticEngine engine, String defaultSchema) {
        return switch (language.toLowerCase()) {
            case "plsql" -> {
                PlSqlSemanticListener l = new PlSqlSemanticListener(engine);
                if (defaultSchema != null && !defaultSchema.isBlank()) {
                    l.setDefaultSchema(defaultSchema);
                }
                yield l;
            }
            default -> throw new IllegalArgumentException("Диалект не реализован: " + language);
        };
    }

    private void parseAndWalk(String sql, String language, Object listener, PipelineTimer timer) {
        switch (language.toLowerCase()) {
            case "plsql" -> {
                timer.start("parse");
                PlSqlLexer lexer       = new PlSqlLexer(CharStreams.fromString(sql));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                PlSqlParser parser      = new PlSqlParser(tokens);
                PlSqlParser.Sql_scriptContext tree = parser.sql_script();
                timer.stop("parse");
                timer.count("tokens", tokens.getNumberOfOnChannelTokens());

                timer.start("walk");
                ParseTreeWalker.DEFAULT.walk((PlSqlSemanticListener) listener, tree);
                timer.stop("walk");
            }
            default -> throw new IllegalArgumentException("Парсер не реализован: " + language);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Output
    // ═══════════════════════════════════════════════════════════════

    private void printResult(Path file, SemanticResult result, long lineCount, PipelineTimer timer) {
        var s = result.getStructure();
        int tables  = s != null ? size(s.getTables()) : 0;
        int cols    = s != null ? size(s.getColumns()) : 0;
        int stmts   = s != null ? size(s.getStatements()) : 0;
        int routs   = s != null ? size(s.getRoutines()) : 0;
        int joins   = s != null ? s.getStatements().values().stream().mapToInt(st -> st.getJoins().size()).sum() : 0;
        int lineage = size(result.getLineage());
        int atoms   = size(result.getAtoms());

        long p  = timer.ms("parse");
        long w  = timer.ms("walk");
        long rv = timer.ms("resolve");
        long db = timer.writeMs();

        System.out.printf("  %-30s %5d lines  P:%-4d W:%-5d Rv:%-4d DB:%-5d  T:%-3d C:%-4d S:%-3d Rt:%-2d J:%-2d A:%-3d L:%-3d%n",
                file.getFileName(), lineCount,
                p, w, rv, db,
                tables, cols, stmts, routs, joins, atoms, lineage);
    }

    // ═══════════════════════════════════════════════════════════════
    // CLI
    // ═══════════════════════════════════════════════════════════════

    private static RunConfig parseArguments(String[] args) throws ParseException {
        if (args.length == 1 && !args[0].startsWith("-")) {
            RunConfig c = new RunConfig();
            c.inputPath = args[0];
            return c;
        }

        Options options = new Options();
        options.addOption("i", "input", true, "Input file or directory path");
        options.addOption("l", "language", true, "SQL dialect: plsql (default)");
        options.addOption("d", "dialect", true, "Alias for --language");
        options.addOption(null, "arcade-db", true, "ArcadeDB embedded path");
        options.addOption("t", "db-type", true, "Database type: arcadedb");
        options.addOption(null, "db-host", true, "Database host (default: localhost)");
        options.addOption("p", "db-port", true, "Database port (default: 2480)");
        options.addOption("n", "db-name", true, "Database name (default: hound)");
        options.addOption("u", "db-user", true, "Database user (default: root)");
        options.addOption(null, "db-password", true, "Database password");
        options.addOption(null, "app-name", true, "Application name (DaliApplication vertex, groups databases)");
        options.addOption(null, "clean", false, "Delete all Dali records before processing");
        options.addOption(null, "diag", false, "Run diagnostics after processing");
        options.addOption(null, "db-batch", false,
            "Use HTTP Batch endpoint for ArcadeDB remote (один POST вместо N запросов)");
        options.addOption(null, "threads", true, "Number of parallel parse threads (default: CPU cores)");
        options.addOption(null, "help", false, "Show help");

        CommandLine cmd = new DefaultParser().parse(options, args);

        if (cmd.hasOption("help")) {
            printHelp();
            System.exit(0);
        }

        RunConfig config = new RunConfig();

        config.inputPath = cmd.getOptionValue("input");
        if (config.inputPath == null || config.inputPath.isBlank()) {
            throw new ParseException("--input обязателен");
        }

        if (cmd.hasOption("language")) config.language = cmd.getOptionValue("language");
        else if (cmd.hasOption("dialect")) config.language = cmd.getOptionValue("dialect");

        // ArcadeDB embedded
        if (cmd.hasOption("arcade-db")) {
            config.arcadeDbPath = cmd.getOptionValue("arcade-db");
        }

        // ArcadeDB remote
        if (cmd.hasOption("db-type")) config.dbType = cmd.getOptionValue("db-type");
        if (cmd.hasOption("db-host")) config.dbHost = cmd.getOptionValue("db-host");
        if (cmd.hasOption("db-port")) config.dbPort = Integer.parseInt(cmd.getOptionValue("db-port"));
        if (cmd.hasOption("db-name")) config.dbName = cmd.getOptionValue("db-name");
        if (cmd.hasOption("db-user")) config.dbUser = cmd.getOptionValue("db-user");
        if (cmd.hasOption("db-password")) config.dbPassword = cmd.getOptionValue("db-password");

        // Namespace / hierarchy
        if (cmd.hasOption("app-name")) config.appName = cmd.getOptionValue("app-name");

        // Flags
        config.clean    = cmd.hasOption("clean");
        config.diag     = cmd.hasOption("diag");
        config.useBatch = cmd.hasOption("db-batch");
        if (cmd.hasOption("threads")) {
            config.threads = Math.max(1, Integer.parseInt(cmd.getOptionValue("threads")));
        }

        return config;
    }

    private static void printHelp() {
        System.out.println("Hound Semantic Engine v1.0.0");
        System.out.println();
        System.out.println("Использование:");
        System.out.println("  hound --input <path> [--language plsql] [storage options]");
        System.out.println("  hound <path>");
        System.out.println();
        System.out.println("Storage (выберите один):");
        System.out.println("  --arcade-db <path>           ArcadeDB embedded (файл)");
        System.out.println("  --db-type arcadedb           ArcadeDB remote (Docker/сервер)");
        System.out.println("    --db-host localhost");
        System.out.println("    --db-port 2480");
        System.out.println("    --db-name hound");
        System.out.println("    --db-user root");
        System.out.println("    --db-password playwithdata");
        System.out.println("    --db-batch                 Batch mode: 1 HTTP POST вместо N запросов/файл");
        System.out.println("                               Требует ArcadeDB 23.x+");
        System.out.println();
        System.out.println("Namespace / hierarchy:");
        System.out.println("  --app-name <name>            Application name (DaliApplication, groups databases)");
        System.out.println();
        System.out.println("Flags:");
        System.out.println("  --clean                      Очистить все Dali-данные перед обработкой");
        System.out.println("  --diag                       Запустить диагностику после обработки");
        System.out.println("  --threads N                  Потоков для параллельного разбора (default: CPU cores)");
    }

    // ═══════════════════════════════════════════════════════════════
    // Performance Summary
    // ═══════════════════════════════════════════════════════════════

    private void printSummary(List<PipelineTimer> timers, List<String> names, int success, int failed) {
        long totalLines = timers.stream().mapToLong(t -> t.count("lines")).sum();
        long parseTotal = timers.stream().mapToLong(t -> t.ms("parse")).sum();
        long walkTotal  = timers.stream().mapToLong(t -> t.ms("walk")).sum();
        long rvTotal    = timers.stream().mapToLong(t -> t.ms("resolve")).sum();
        long vtxTotal   = timers.stream().mapToLong(t -> t.ms("write.vtx")).sum();
        long edgeTotal  = timers.stream().mapToLong(t -> t.ms("write.edge")).sum();
        long batchTotal = timers.stream().mapToLong(t -> t.ms("write.batch")).sum();
        long dbTotal    = batchTotal > 0 ? batchTotal : vtxTotal + edgeTotal;
        long total      = parseTotal + walkTotal + rvTotal + dbTotal;

        int n = timers.size();

        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.printf ("  PERFORMANCE SUMMARY (%d files, %,d lines)%n", success, totalLines);
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("  Phase          Total        Avg/file     % total");
        System.out.println("  ────────────   ──────────   ──────────   ───────");
        printPhaseLine("Parse",    parseTotal, n, total);
        printPhaseLine("Walk",     walkTotal,  n, total);
        printPhaseLine("Resolve",  rvTotal,    n, total);
        printPhaseLine("DB Write", dbTotal,    n, total);
        if (batchTotal > 0) {
            printSubPhaseLine("batch", batchTotal, total);
        } else if (edgeTotal > 0) {
            printSubPhaseLine("vtx",  vtxTotal,  total);
            printSubPhaseLine("edge", edgeTotal, total);
        }
        System.out.println("  ────────────   ──────────   ──────────");
        printPhaseLine("TOTAL",    total,      n, total);

        if (n > 1) {
            List<Integer> idx = new ArrayList<>();
            for (int i = 0; i < n; i++) idx.add(i);
            idx.sort((a, b) -> Long.compare(timers.get(b).totalMs(), timers.get(a).totalMs()));

            System.out.println();
            System.out.println("  Slowest files:");
            int show = Math.min(5, n);
            for (int rank = 0; rank < show; rank++) {
                int i = idx.get(rank);
                PipelineTimer t = timers.get(i);
                System.out.printf("    %d. %-35s %,6d lines  %s%n",
                        rank + 1, names.get(i), t.count("lines"), formatTime(t.totalMs()));
            }
        }

        if (failed > 0) {
            System.out.println();
            System.out.println("  Файлов с ошибками: " + failed);
        }
        System.out.println("════════════════════════════════════════════════════════════════");
    }

    private void printPhaseLine(String phase, long ms, int n, long total) {
        int pct = total == 0 ? 0 : (int) (ms * 100 / total);
        String avg = n == 0 ? "0ms" : formatTime(ms / n);
        System.out.printf("  %-13s  %-10s  %-10s  %3d%%%n", phase, formatTime(ms), avg, pct);
    }

    private void printSubPhaseLine(String sub, long ms, long total) {
        int pct = total == 0 ? 0 : (int) (ms * 100 / total);
        System.out.printf("    └─ %-7s  %-10s  %-10s  %3d%%%n", sub, formatTime(ms), "", pct);
    }

    // ═══════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Reads a file trying UTF-8 first, then Windows-1251 (Cyrillic Oracle files),
     * then ISO-8859-1 as a lossless last resort.
     * Logs a warning when falling back so encoding issues are visible in logs.
     */
    private static String readFileWithFallback(Path file) throws IOException {
        // 1st attempt — UTF-8 (standard)
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            logger.debug("UTF-8 decode failed for {}, trying Windows-1251", file.getFileName());
        }
        // 2nd attempt — Windows-1251 (Cyrillic PL/SQL files saved by TOAD/SQL Developer)
        try {
            String content = Files.readString(file, Charset.forName("Windows-1251"));
            logger.warn("File read as Windows-1251 (non-UTF-8): {}", file.getFileName());
            return content;
        } catch (MalformedInputException e) {
            logger.debug("Windows-1251 decode failed for {}, falling back to ISO-8859-1", file.getFileName());
        }
        // 3rd attempt — ISO-8859-1 (lossless: every byte is a valid character)
        logger.warn("File read as ISO-8859-1 (fallback, may have garbled chars): {}", file.getFileName());
        return Files.readString(file, StandardCharsets.ISO_8859_1);
    }

    private static String formatTime(long ms) {
        if (ms < 1000) return ms + "ms";
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        if (min > 0) return String.format("%dm %02ds", min, sec);
        return String.format("%d.%ds", sec, (ms % 1000) / 100);
    }

    private static int size(java.util.Map<?, ?> m) { return m != null ? m.size() : 0; }
    private static int size(java.util.List<?> l) { return l != null ? l.size() : 0; }

    private static boolean isSqlFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        for (String ext : SQL_EXTENSIONS) if (name.endsWith(ext)) return true;
        return false;
    }

    // ═══════════════════════════════════════════════════════════════
    // Config
    // ═══════════════════════════════════════════════════════════════

    static class RunConfig {
        String inputPath;
        String language = "plsql";

        // Embedded ArcadeDB
        String arcadeDbPath = null;

        // Remote ArcadeDB (Docker)
        String dbType = null;
        String dbHost = "localhost";
        int dbPort = 2480;
        String dbName = "hound";
        String dbUser = "root";
        String dbPassword = "playwithdata";

        // Namespace / application hierarchy
        // --app-name groups multiple databases under one DaliApplication vertex.
        // Example: --app-name CRM  (creates CRM → DWH, CRM → ODS edges if both processed)
        String appName = null;

        // Flags
        boolean clean    = false;
        boolean diag     = false;
        boolean useBatch = false;
        int     threads  = Runtime.getRuntime().availableProcessors();
    }
}