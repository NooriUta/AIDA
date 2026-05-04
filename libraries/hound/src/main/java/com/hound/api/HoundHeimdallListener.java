package com.hound.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link HoundEventListener} that sends parse events to the HEIMDALL backend.
 *
 * <p>Activated when the system property {@code heimdall.url} is set, e.g.:
 * <pre>
 *   -Dheimdall.url=http://localhost:9093
 * </pre>
 *
 * <p>All HTTP calls are fire-and-forget (async). This listener NEVER throws —
 * any failure is logged at DEBUG and silently swallowed.
 *
 * <p>Throttle: {@link #onAtomExtracted} emits to HEIMDALL every
 * {@value ATOM_THROTTLE} atoms per file to avoid event spam.
 *
 * <p>Used in the no-listener overloads of {@link com.hound.HoundParserImpl}
 * so that standalone Hound (CLI / batch) automatically reports to HEIMDALL
 * without requiring Dali's CDI-wired {@code HeimdallEmitter}.
 */
public class HoundHeimdallListener implements HoundEventListener {

    private static final Logger log = LoggerFactory.getLogger(HoundHeimdallListener.class);

    /** Emit ATOM_EXTRACTED to HEIMDALL once per this many atoms per file. */
    private static final int ATOM_THROTTLE = 100;

    private static final ObjectMapper JSON   = new ObjectMapper();
    private static final HttpClient   CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    /** URL of the HEIMDALL events endpoint, e.g. {@code http://localhost:9093}. */
    private final String heimdallBase;

    /** SessionId of the active parse session — propagated to every event. */
    private final String sessionId;

    /**
     * Tenant alias for the active parse job — injected into every event payload
     * so the HEIMDALL EventLog can show the Tenant column for hound events.
     */
    private final String tenantAlias;

    /**
     * Source root directory (absolute). When set, file events report paths
     * relative to this root (e.g. {@code ERP_CORE/DWH/pkg.sql}) instead of
     * just the filename. Null-safe: falls back to last-3-components heuristic.
     */
    private final String sourceRoot;

    /**
     * Per-file last-emitted atom count. Used for throttle logic.
     * Key = file path, value = atom count at last HEIMDALL emission.
     */
    private final ConcurrentHashMap<String, Integer> lastEmitted = new ConcurrentHashMap<>();

    /** Legacy constructor — no session / tenant context (CLI / standalone use). */
    public HoundHeimdallListener(String heimdallBase) {
        this(heimdallBase, null, null, null);
    }

    /**
     * Full constructor used by Dali's {@code ParseJob}.
     *
     * @param heimdallBase  base URL of the HEIMDALL backend
     * @param sessionId     active parse session id (nullable)
     * @param tenantAlias   tenant alias for this job (nullable — skipped if blank)
     * @param sourceRoot    absolute source root path for relative-path display (nullable)
     */
    public HoundHeimdallListener(String heimdallBase, String sessionId,
                                  String tenantAlias, String sourceRoot) {
        this.heimdallBase = heimdallBase.endsWith("/")
                ? heimdallBase.substring(0, heimdallBase.length() - 1)
                : heimdallBase;
        this.sessionId   = sessionId;
        this.tenantAlias = tenantAlias;
        this.sourceRoot  = sourceRoot;
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Returns a {@link HoundHeimdallListener} if {@code heimdall.url} system property
     * is set, otherwise returns {@link NoOpHoundEventListener#INSTANCE}.
     */
    public static HoundEventListener fromSystemProperty() {
        String url = System.getProperty("heimdall.url");
        if (url == null || url.isBlank()) return NoOpHoundEventListener.INSTANCE;
        log.info("[HoundHeimdallListener] HEIMDALL reporting enabled → {}", url);
        return new HoundHeimdallListener(url);
    }

    // ── HoundEventListener ────────────────────────────────────────────────────

    @Override
    public void onFileParseStarted(String file, String dialect) {
        send("FILE_PARSING_STARTED", "INFO", 0, Map.of(
                "file",    relPath(file),
                "dialect", dialect));
    }

    @Override
    public void onAtomExtracted(String file, int atomCount, String atomType) {
        int prev = lastEmitted.getOrDefault(file, 0);
        if (atomCount - prev >= ATOM_THROTTLE) {
            lastEmitted.put(file, atomCount);
            send("ATOM_EXTRACTED", "INFO", 0, Map.of(
                    "file",      relPath(file),
                    "atomCount", atomCount));
        }
    }

    @Override
    public void onFileParseCompleted(String file, ParseResult result) {
        lastEmitted.remove(file);

        send("FILE_PARSING_COMPLETED", "INFO", result.durationMs(), Map.of(
                "file",           relPath(file),
                "atomCount",      result.atomCount(),
                "vertexCount",    result.vertexCount(),
                "edgeCount",      result.edgeCount(),
                "resolutionRate", result.resolutionRate(),
                "durationMs",     result.durationMs()));

        if (result.atomCount() > 0) {
            send("RESOLUTION_COMPLETED", "INFO", result.durationMs(), Map.of(
                    "file",           relPath(file),
                    "atomCount",      result.atomCount(),
                    "resolutionRate", result.resolutionRate()));
        }
    }

    @Override
    public void onParseError(String file, int line, int charPos, String msg) {
        send("PARSE_ERROR", "ERROR", 0, Map.of(
                "file", relPath(file),
                "line", line,
                "col",  charPos,
                "msg",  truncate(msg, 300)));
    }

    @Override
    public void onParseWarning(String file, int line, int charPos, String msg) {
        send("PARSE_WARNING", "WARN", 0, Map.of(
                "file", relPath(file),
                "line", line,
                "col",  charPos,
                "msg",  truncate(msg, 300)));
    }

    @Override
    public void onError(String file, Throwable error) {
        lastEmitted.remove(file);
        send("FILE_PARSING_FAILED", "ERROR", 0, Map.of(
                "file",  relPath(file),
                "error", error != null ? truncate(error.getMessage(), 400) : "unknown"));
    }

    @Override
    public void onSemanticWarning(String file, String category, String message) {
        send("SEMANTIC_WARNING", "WARN", 0, Map.of(
                "file",     relPath(file),
                "category", category,
                "msg",      truncate(message, 400)));
    }

    @Override
    public void onSemanticError(String file, String category, String message) {
        send("SEMANTIC_ERROR", "ERROR", 0, Map.of(
                "file",     relPath(file),
                "category", category,
                "msg",      truncate(message, 400)));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void send(String eventType, String level,
                      long durationMs,
                      Map<String, Object> payload) {
        try {
            // Enrich payload with tenantAlias so the HEIMDALL EventLog Tenant column is populated.
            Map<String, Object> enrichedPayload = new java.util.LinkedHashMap<>(payload);
            if (tenantAlias != null && !tenantAlias.isBlank()) {
                enrichedPayload.putIfAbsent("tenantAlias", tenantAlias);
            }

            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("timestamp",       System.currentTimeMillis());
            body.put("sourceComponent", "hound");
            body.put("eventType",       eventType);
            body.put("level",           level);
            body.put("sessionId",       sessionId);  // propagated from ParseJob
            body.put("userId",          null);
            body.put("correlationId",   null);
            body.put("durationMs",      durationMs);
            body.put("payload",         enrichedPayload);

            String json = JSON.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(heimdallBase + "/events"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            CLIENT.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                  .exceptionally(e -> {
                      log.debug("[HoundHeimdallListener] send failed ({}): {}", eventType, e.getMessage());
                      return null;
                  });

        } catch (Exception e) {
            log.debug("[HoundHeimdallListener] build/send error: {}", e.getMessage());
        }
    }

    /**
     * Returns a human-readable file path for HEIMDALL events.
     *
     * <ul>
     *   <li>If {@code sourceRoot} is set: returns the path relative to the source root
     *       using forward slashes (e.g. {@code ERP_CORE/DWH/PKG_ETL.sql}).
     *   <li>Otherwise: returns the last 3 path components joined by {@code /}
     *       (e.g. {@code DWH/PKG_ETL.sql} when only 2 are available).
     * </ul>
     */
    private String relPath(String file) {
        if (file == null) return "";
        try {
            Path p = Paths.get(file);
            if (sourceRoot != null && !sourceRoot.isBlank()) {
                Path root = Paths.get(sourceRoot);
                // relativize() throws if p is not under root — fall through on error
                try {
                    Path rel = root.relativize(p);
                    return rel.toString().replace('\\', '/');
                } catch (Exception ignored) { /* fall through */ }
            }
            // Heuristic: show up to the last 3 components
            int n = p.getNameCount();
            int start = Math.max(0, n - 3);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < n; i++) {
                if (i > start) sb.append('/');
                sb.append(p.getName(i).toString());
            }
            return sb.toString();
        } catch (Exception e) {
            return file;
        }
    }

    /** Truncates a string to {@code maxLen} characters, appending "…" if cut. */
    private static String truncate(String s, int maxLen) {
        if (s == null)          return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "…";
    }
}
