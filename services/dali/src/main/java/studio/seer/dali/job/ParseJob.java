package studio.seer.dali.job;

import com.hound.api.ArcadeWriteMode;
import com.hound.api.CompositeListener;
import com.hound.api.HoundConfig;
import com.hound.api.HoundEventListener;
import com.hound.api.HoundHeimdallListener;
import com.hound.api.HoundParser;
import com.hound.api.NoOpHoundEventListener;
import com.hound.api.ParseResult;
import com.hound.api.SqlSource;
import com.skadi.SkadiFetchConfig;
import com.skadi.SkadiFetchException;
import com.skadi.SkadiFetchResult;
import com.skadi.SkadiFetcher;
import studio.seer.dali.skadi.DaliSkadiFetchListener;
import studio.seer.dali.skadi.SkadiFetcherRegistry;
import studio.seer.dali.skadi.SourceArchiveService;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.dali.service.SessionService;
import studio.seer.shared.FileResult;
import studio.seer.shared.ParseSessionInput;
import studio.seer.shared.VertexTypeStat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * JobRunr background job — parses a SQL file or directory via Hound in-JVM.
 *
 * <p>Single file  → {@link HoundParser#parse}, progress: 0→1.
 * <p>Directory    → collects all SQL files recursively, parses file-by-file,
 *                   progress updated after each file completes.
 *
 * <p>Write modes:
 * <ul>
 *   <li>{@code preview=true}  — {@link ArcadeWriteMode#DISABLED}, nothing written to YGG
 *   <li>{@code preview=false} — {@link ArcadeWriteMode#REMOTE_BATCH}, writes to YGG
 * </ul>
 *
 * <p>If {@code clearBeforeWrite=true} and {@code preview=false}, all Dali types in YGG
 * are truncated before parsing begins (via {@link HoundParser#cleanAll}).
 */
@Unremovable
@ApplicationScoped
public class ParseJob {

    private static final Logger log = LoggerFactory.getLogger(ParseJob.class);

    private static final Set<String> SQL_EXTENSIONS = Set.of(
            ".sql", ".pck", ".prc", ".pkb", ".pks", ".fnc", ".trg", ".vw"
    );

    @Inject HoundParser          houndParser;
    @Inject SessionService       sessionService;
    @Inject HeimdallEmitter      emitter;
    @Inject SkadiFetcherRegistry skadiFetcherRegistry;
    @Inject SourceArchiveService sourceArchiveService;

    @ConfigProperty(name = "ygg.url")      String yggUrl;
    @ConfigProperty(name = "ygg.db")       String yggDb;
    @ConfigProperty(name = "ygg.user")     String yggUser;
    @ConfigProperty(name = "ygg.password") String yggPassword;
    // Optional — absent when HEIMDALL_URL env var is not set (e.g. CI, local dev without Heimdall).
    // SmallRye Config treats empty string "" as null; Optional maps null → empty without throwing.
    @ConfigProperty(name = "heimdall.url") Optional<String> heimdallUrl;

    @Job(name = "Parse SQL files", retries = 3)
    public void execute(String sessionId, ParseSessionInput input) {
        String src = input.source().strip();
        log.info("[{}] ParseJob starting: source={} dialect={} preview={} clearBeforeWrite={}",
                sessionId, src, input.dialect(), input.preview(), input.clearBeforeWrite());

        long startMs = System.currentTimeMillis();

        try {
            HoundConfig config = buildConfig(input);
            emitter.sessionStarted(sessionId, src, input.dialect(), input.preview(),
                    input.clearBeforeWrite(), config.workerThreads());

            // Optional YGG cleanup before first write
            if (!input.preview() && input.clearBeforeWrite()) {
                log.info("[{}] clearBeforeWrite=true — truncating YGG...", sessionId);
                houndParser.cleanAll(config);
                log.info("[{}] YGG truncated", sessionId);
            }

            // JDBC source: harvest from database via SKADI, then parse in-memory
            if (src.startsWith("jdbc:")) {
                runJdbc(sessionId, src, config, input);
                return;
            }

            Path sourcePath = Path.of(src);
            if (!Files.exists(sourcePath)) {
                throw new IllegalArgumentException("Source path does not exist: " + src);
            }
            if (Files.isDirectory(sourcePath)) {
                runBatch(sessionId, sourcePath, config, input);
            } else {
                runSingle(sessionId, sourcePath, config);
            }

        } catch (Exception e) {
            log.error("[{}] ParseJob failed: {}", sessionId, e.getMessage(), e);
            String errorMsg = buildErrorMessage(e);
            sessionService.failSession(sessionId, errorMsg);
            emitter.sessionFailed(sessionId, errorMsg, System.currentTimeMillis() - startMs);
            throw new RuntimeException("ParseJob failed for session " + sessionId, e);
        } finally {
            if (input.uploaded()) {
                deleteTempDir(Path.of(src));
            }
        }
    }

    // ── Config builder ─────────────────────────────────────────────────────────

    private HoundConfig buildConfig(ParseSessionInput input) {
        if (input.preview()) {
            return HoundConfig.defaultDisabled(input.dialect());
        }
        return new HoundConfig(
                input.dialect(),
                null,                           // targetSchema — no namespace isolation
                ArcadeWriteMode.REMOTE_BATCH,
                yggUrl, yggDb, yggUser, yggPassword,
                Runtime.getRuntime().availableProcessors(),
                false, 5000, null);
    }

    // ── Single file ────────────────────────────────────────────────────────────

    private void runSingle(String sessionId, Path file, HoundConfig config) {
        sessionService.startSession(sessionId, false, 1);

        HoundEventListener listener = buildListener(sessionId, config);
        ParseResult result = houndParser.parse(file, config, listener);

        FileResult fr = toFileResult(result);
        sessionService.completeSession(sessionId, result, List.of(fr));
        emitter.sessionCompleted(sessionId, result.atomCount(), result.resolutionRate(),
                result.durationMs(), 1);
    }

    // ── JDBC source (SKADI harvest) ───────────────────────────────────────────

    /**
     * Harvests SQL objects from a live database via SKADI, then parses them in-memory.
     *
     * <p>Flow:
     * <ol>
     *   <li>Detect SKADI adapter from JDBC URL prefix</li>
     *   <li>{@code SkadiFetcher.fetchScripts()} — DDL text in memory (no temp files)</li>
     *   <li>{@link SourceArchiveService#upsertAll} — filter changed files → {@link SqlSource.FromText} list</li>
     *   <li>{@link HoundParser#parseSources} — parallel parse + optional YGG write</li>
     * </ol>
     *
     * <p>The {@code clearBeforeWrite} flag is respected (applied before this method is called
     * by the outer {@link #execute} frame). The {@code uploaded} flag is ignored for JDBC sources.
     */
    private void runJdbc(String sessionId, String jdbcUrl, HoundConfig config,
                         ParseSessionInput input) throws SkadiFetchException {

        // 1 — detect adapter
        SkadiFetcher fetcher = skadiFetcherRegistry.detectByUrl(jdbcUrl)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No SKADI adapter registered for URL: " + jdbcUrl));

        // 2 — build SKADI config
        SkadiFetchConfig skConfig = SkadiFetchConfig.fullHarvest(
                jdbcUrl,
                input.jdbcUser(),
                input.jdbcPassword(),
                input.jdbcSchema());

        // 3 — fetch scripts from source DB
        DaliSkadiFetchListener fetchListener = new DaliSkadiFetchListener(emitter, sessionId);
        fetchListener.onFetchStarted(fetcher.adapterName(), skConfig.schema(), skConfig.objectTypes());
        SkadiFetchResult fetchResult = fetcher.fetchScripts(skConfig);
        fetchListener.onFetchCompleted(fetchResult.stats());

        // 4 — convert to SqlSource.FromText (MVP: all files; full: hash-dedup)
        List<SqlSource> sources = sourceArchiveService.upsertAll(fetchResult.files());

        if (sources.isEmpty()) {
            log.info("[{}] SKADI: no SQL sources to parse (adapter={}, fetched={})",
                    sessionId, fetcher.adapterName(), fetchResult.stats().totalFetched());
            sessionService.startSession(sessionId, false, 0);
            ParseResult empty = new ParseResult(jdbcUrl, 0, 0, 0, 0,
                    java.util.Map.of(), 1.0, 0, 0, List.of(), List.of(), 0L);
            sessionService.completeSession(sessionId, empty, List.of());
            return;
        }

        // 5 — parse via Hound
        log.info("[{}] SKADI: parsing {} source(s) via Hound (dialect={})",
                sessionId, sources.size(), config.dialect());
        sessionService.startSession(sessionId, true, sources.size());

        HoundEventListener listener = buildListener(sessionId, config);
        List<ParseResult> parseResults = houndParser.parseSources(sources, config, listener);

        // 6 — aggregate and complete session
        List<FileResult> fileResults = parseResults.stream()
                .map(ParseJob::toFileResult)
                .collect(java.util.stream.Collectors.toList());

        fileResults.forEach(fr -> sessionService.recordFileComplete(sessionId, fr));

        ParseResult merged = merge(jdbcUrl, fileResults);
        sessionService.completeSession(sessionId, merged, fileResults);
        emitter.sessionCompleted(sessionId, merged.atomCount(), merged.resolutionRate(),
                merged.durationMs(), fileResults.size());
    }

    // ── Batch (directory) ─────────────────────────────────────────────────────

    /** Max per-file retry attempts for transient write failures (e.g. ArcadeDB MVCC conflicts). */
    private static final int  FILE_MAX_RETRIES    = 3;
    /** Base delay (ms) for per-file retry backoff: 2 s → 4 s on successive attempts. */
    private static final long FILE_RETRY_BASE_MS  = 2_000;

    private void runBatch(String sessionId, Path dir, HoundConfig config,
                          ParseSessionInput input) throws IOException {
        List<Path> files = collectSqlFiles(dir);
        if (files.isEmpty()) {
            throw new IllegalArgumentException("No SQL files found in directory: " + dir);
        }

        log.info("[{}] Batch parse: {} files in {}", sessionId, files.size(), dir);
        sessionService.startSession(sessionId, true, files.size());

        List<FileResult> fileResults = new ArrayList<>();

        // Parse file-by-file so we can update progress after each one.
        // Per-file retry absorbs transient ArcadeDB MVCC conflicts without aborting the entire
        // batch — a single file failure no longer cascades into a full session retry (JobRunr).
        for (Path file : files) {
            FileResult fr = parseFileWithRetry(sessionId, file, config);
            fileResults.add(fr);
            sessionService.recordFileComplete(sessionId, fr);
            if (fr.success()) {
                log.debug("[{}] File done: {} atoms={} dur={}ms",
                        sessionId, file.getFileName(), fr.atomCount(), fr.durationMs());
            } else {
                log.warn("[{}] File permanently failed (skipped): {}",
                        sessionId, file.getFileName());
            }
        }

        ParseResult merged = merge(input.source(), fileResults);
        sessionService.completeSession(sessionId, merged, fileResults);
        emitter.sessionCompleted(sessionId, merged.atomCount(), merged.resolutionRate(),
                merged.durationMs(), fileResults.size());
    }

    /**
     * Parses a single file with per-file retry fallback.
     *
     * <p>If the write phase throws (e.g. ArcadeDB MVCC conflict manifesting as an HTTP 5xx after
     * {@link com.hound.storage.HttpBatchClient}'s own 3-attempt retry), this method waits and
     * retries up to {@value FILE_MAX_RETRIES} times before giving up.
     *
     * <p>On permanent failure the file is recorded as {@code success=false} in the batch
     * result, but processing continues for the remaining files.
     */
    private FileResult parseFileWithRetry(String sessionId, Path file, HoundConfig config) {
        Exception lastEx = null;
        for (int attempt = 1; attempt <= FILE_MAX_RETRIES; attempt++) {
            try {
                HoundEventListener listener = buildListener(sessionId, config);
                ParseResult result = houndParser.parse(file, config, listener);
                if (attempt > 1) {
                    log.info("[{}] File recovered on attempt {}/{}: {}",
                            sessionId, attempt, FILE_MAX_RETRIES, file.getFileName());
                }
                return toFileResult(result);
            } catch (Exception e) {
                lastEx = e;
                String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                if (attempt < FILE_MAX_RETRIES) {
                    long delay = FILE_RETRY_BASE_MS << (attempt - 1); // 2 s, 4 s
                    log.warn("[{}] File write failed (attempt {}/{}), retrying in {} ms: {} — {}",
                            sessionId, attempt, FILE_MAX_RETRIES, delay, file.getFileName(), reason);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("[{}] File failed after {} attempts, skipping: {} — {}",
                            sessionId, FILE_MAX_RETRIES, file.getFileName(), reason, e);
                }
            }
        }
        return failedFileResult(file, lastEx);
    }

    /** Creates a {@link FileResult} representing a file that could not be written. */
    private static FileResult failedFileResult(Path file, Exception cause) {
        String error = cause != null
                ? (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName())
                : "Unknown write error";
        return new FileResult(
                file.toString(),
                false,            // success = false
                0, 0, 0, 0,
                List.of(),
                0.0, 0, 0,
                0L,
                List.of(),
                List.of("Write failed after " + FILE_MAX_RETRIES + " attempts: " + error));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Builds the effective listener: DaliHoundListener (persistence) + HoundHeimdallListener
     *  (observability) via CompositeListener. Falls back to NoOp if HEIMDALL_URL is not set. */
    private HoundEventListener buildListener(String sessionId, HoundConfig config) {
        HoundEventListener heimdall = heimdallUrl
                .filter(url -> !url.isBlank())
                .<HoundEventListener>map(HoundHeimdallListener::new)
                .orElse(NoOpHoundEventListener.INSTANCE);
        return new CompositeListener(
                new DaliHoundListener(sessionId, config.dialect(), emitter),
                heimdall);
    }

    private List<Path> collectSqlFiles(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return SQL_EXTENSIONS.stream().anyMatch(name::endsWith);
                    })
                    .sorted()
                    .toList();
        }
    }

    private static FileResult toFileResult(ParseResult r) {
        return new FileResult(
                r.file(),
                r.errors().isEmpty(),
                r.atomCount(), r.vertexCount(), r.edgeCount(),
                r.droppedEdgeCount(),
                toVertexTypeStats(r.vertexStats()),
                r.resolutionRate(), r.atomsResolved(), r.atomsUnresolved(),
                r.durationMs(),
                r.warnings(), r.errors());
    }

    /** Converts ParseResult's internal Map<type,[inserted,dup]> to a shared VertexTypeStat list. */
    private static List<VertexTypeStat> toVertexTypeStats(java.util.Map<String, int[]> map) {
        if (map == null || map.isEmpty()) return List.of();
        List<VertexTypeStat> out = new ArrayList<>(map.size());
        map.forEach((type, counts) -> out.add(new VertexTypeStat(type, counts[0], counts[1])));
        return List.copyOf(out);
    }

    /** Builds a readable error string including cause chain, deduplicating repeated messages. */
    private static String buildErrorMessage(Throwable t) {
        var seen = new java.util.LinkedHashSet<String>();
        Throwable cur = t;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && !msg.isBlank()) seen.add(msg.trim());
            cur = cur.getCause();
        }
        if (seen.isEmpty()) return t.getClass().getSimpleName();
        return String.join(" → ", seen);
    }

    private static void deleteTempDir(Path dir) {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            log.debug("Deleted temp upload dir: {}", dir);
        } catch (IOException e) {
            log.warn("Failed to delete temp upload dir {}: {}", dir, e.getMessage());
        }
    }

    private static ParseResult merge(String source, List<FileResult> results) {
        int atoms        = results.stream().mapToInt(FileResult::atomCount).sum();
        int vertices     = results.stream().mapToInt(FileResult::vertexCount).sum();
        int edges        = results.stream().mapToInt(FileResult::edgeCount).sum();
        int droppedEdges = results.stream().mapToInt(FileResult::droppedEdgeCount).sum();
        long duration    = results.stream().mapToLong(FileResult::durationMs).sum();

        // Aggregate per-type stats across all files
        var aggMap = new LinkedHashMap<String, int[]>();
        for (FileResult fr : results) {
            for (VertexTypeStat s : fr.vertexStats()) {
                aggMap.computeIfAbsent(s.type(), k -> new int[2]);
                aggMap.get(s.type())[0] += s.inserted();
                aggMap.get(s.type())[1] += s.duplicate();
            }
        }

        // Weighted average by atomCount — gives true resolved/total ratio
        double rate = atoms > 0
                ? results.stream().mapToDouble(r -> r.resolutionRate() * r.atomCount()).sum() / atoms
                : 0.0;

        int atomsResolved   = results.stream().mapToInt(FileResult::atomsResolved).sum();
        int atomsUnresolved = results.stream().mapToInt(FileResult::atomsUnresolved).sum();

        List<String> warnings = results.stream()
                .flatMap(r -> r.warnings().stream())
                .toList();
        List<String> errors = results.stream()
                .flatMap(r -> r.errors().stream())
                .toList();

        return new ParseResult(source, atoms, vertices, edges, droppedEdges,
                aggMap, rate, atomsResolved, atomsUnresolved, warnings, errors, duration);
    }
}
