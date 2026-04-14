package studio.seer.dali.job;

import com.hound.api.ArcadeWriteMode;
import com.hound.api.HoundConfig;
import com.hound.api.HoundParser;
import com.hound.api.ParseResult;
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

    @Inject HoundParser    houndParser;
    @Inject SessionService sessionService;
    @Inject HeimdallEmitter emitter;

    @ConfigProperty(name = "ygg.url")      String yggUrl;
    @ConfigProperty(name = "ygg.db")       String yggDb;
    @ConfigProperty(name = "ygg.user")     String yggUser;
    @ConfigProperty(name = "ygg.password") String yggPassword;

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

        DaliHoundListener listener = new DaliHoundListener(sessionId, config.dialect(), emitter);
        ParseResult result = houndParser.parse(file, config, listener);

        FileResult fr = toFileResult(result);
        sessionService.completeSession(sessionId, result, List.of(fr));
        emitter.sessionCompleted(sessionId, result.atomCount(), result.resolutionRate(),
                result.durationMs(), 1);
    }

    // ── Batch (directory) ─────────────────────────────────────────────────────

    private void runBatch(String sessionId, Path dir, HoundConfig config,
                          ParseSessionInput input) throws IOException {
        List<Path> files = collectSqlFiles(dir);
        if (files.isEmpty()) {
            throw new IllegalArgumentException("No SQL files found in directory: " + dir);
        }

        log.info("[{}] Batch parse: {} files in {}", sessionId, files.size(), dir);
        sessionService.startSession(sessionId, true, files.size());

        List<FileResult> fileResults = new ArrayList<>();

        // Parse file-by-file so we can update progress after each one
        for (Path file : files) {
            DaliHoundListener listener = new DaliHoundListener(sessionId, config.dialect(), emitter);
            ParseResult result = houndParser.parse(file, config, listener);
            FileResult fr = toFileResult(result);
            fileResults.add(fr);
            sessionService.recordFileComplete(sessionId, fr);
            log.debug("[{}] File done: {} atoms={} dur={}ms",
                    sessionId, file.getFileName(), result.atomCount(), result.durationMs());
        }

        ParseResult merged = merge(input.source(), fileResults);
        sessionService.completeSession(sessionId, merged, fileResults);
        emitter.sessionCompleted(sessionId, merged.atomCount(), merged.resolutionRate(),
                merged.durationMs(), fileResults.size());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

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
