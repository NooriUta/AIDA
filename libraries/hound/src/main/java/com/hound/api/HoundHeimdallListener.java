package com.hound.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    /**
     * Per-file last-emitted atom count. Used for throttle logic.
     * Key = file path, value = atom count at last HEIMDALL emission.
     */
    private final ConcurrentHashMap<String, Integer> lastEmitted = new ConcurrentHashMap<>();

    public HoundHeimdallListener(String heimdallBase) {
        this.heimdallBase = heimdallBase.endsWith("/")
                ? heimdallBase.substring(0, heimdallBase.length() - 1)
                : heimdallBase;
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
        send("FILE_PARSING_STARTED", "INFO", null, 0, Map.of(
                "file",    basename(file),
                "dialect", dialect));
    }

    @Override
    public void onAtomExtracted(String file, int atomCount, String atomType) {
        int prev = lastEmitted.getOrDefault(file, 0);
        if (atomCount - prev >= ATOM_THROTTLE) {
            lastEmitted.put(file, atomCount);
            send("ATOM_EXTRACTED", "INFO", null, 0, Map.of(
                    "file",      basename(file),
                    "atomCount", atomCount));
        }
    }

    @Override
    public void onFileParseCompleted(String file, ParseResult result) {
        lastEmitted.remove(file);

        send("FILE_PARSING_COMPLETED", "INFO", null, result.durationMs(), Map.of(
                "file",           basename(file),
                "atomCount",      result.atomCount(),
                "vertexCount",    result.vertexCount(),
                "edgeCount",      result.edgeCount(),
                "resolutionRate", result.resolutionRate(),
                "durationMs",     result.durationMs()));

        if (result.atomCount() > 0) {
            send("RESOLUTION_COMPLETED", "INFO", null, result.durationMs(), Map.of(
                    "file",           basename(file),
                    "atomCount",      result.atomCount(),
                    "resolutionRate", result.resolutionRate()));
        }
    }

    @Override
    public void onParseError(String file, int line, int charPos, String msg) {
        send("PARSE_ERROR", "WARN", null, 0, Map.of(
                "file", basename(file),
                "line", line,
                "col",  charPos,
                "msg",  truncate(msg, 300)));
    }

    @Override
    public void onError(String file, Throwable error) {
        lastEmitted.remove(file);
        send("FILE_PARSING_FAILED", "ERROR", null, 0, Map.of(
                "file",  basename(file),
                "error", error != null ? truncate(error.getMessage(), 200) : "unknown"));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void send(String eventType, String level,
                      String sessionId, long durationMs,
                      Map<String, Object> payload) {
        try {
            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("timestamp",       System.currentTimeMillis());
            body.put("sourceComponent", "hound");
            body.put("eventType",       eventType);
            body.put("level",           level);
            body.put("sessionId",       sessionId);
            body.put("userId",          null);
            body.put("correlationId",   null);
            body.put("durationMs",      durationMs);
            body.put("payload",         payload);

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

    /** Returns the last path component of the file path. */
    private static String basename(String file) {
        try {
            return Paths.get(file).getFileName().toString();
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
