package studio.seer.dali.skadi;

import com.skadi.SkadiFetchConfig;
import com.skadi.SkadiFetchListener;
import com.skadi.SkadiFetchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.shared.EventType;

import java.util.Map;
import java.util.Set;

/**
 * Session-scoped {@link SkadiFetchListener} that forwards SKADI fetch events to HEIMDALL
 * and writes progress to the application log.
 *
 * <p>Not CDI-managed — instantiated per ParseJob execution with a captured {@code sessionId}.
 * This allows each concurrent fetch to carry its own correlation ID.
 *
 * <p>Usage in ParseJob:
 * <pre>{@code
 * DaliSkadiFetchListener fetchListener = new DaliSkadiFetchListener(emitter, sessionId);
 * fetchListener.onFetchStarted(adapterName, schema, objectTypes);
 * SkadiFetchResult result = fetcher.fetchScripts(skConfig);
 * fetchListener.onFetchCompleted(result.stats());
 * }</pre>
 */
public class DaliSkadiFetchListener implements SkadiFetchListener {

    private static final Logger log = LoggerFactory.getLogger(DaliSkadiFetchListener.class);

    private final HeimdallEmitter emitter;
    private final String sessionId;

    public DaliSkadiFetchListener(HeimdallEmitter emitter, String sessionId) {
        this.emitter   = emitter;
        this.sessionId = sessionId;
    }

    @Override
    public void onFetchStarted(String sourceAdapter, String schema,
                                Set<SkadiFetchConfig.ObjectType> objectTypes) {
        log.info("[{}] SKADI fetch started: adapter={} schema={} types={}",
                sessionId, sourceAdapter, schema, objectTypes.size());
        emitter.info(EventType.JOB_ENQUEUED, sessionId, Map.of(
                "action",  "skadi_fetch_started",
                "adapter", sourceAdapter,
                "schema",  schema != null ? schema : ""));
    }

    @Override
    public void onObjectFetched(String name, SkadiFetchConfig.ObjectType objectType, long sizeBytes) {
        // Logged only at TRACE to avoid flooding; HEIMDALL receives aggregate via onFetchCompleted
        log.trace("[{}] SKADI: fetched {} {} ({} bytes)", sessionId, objectType, name, sizeBytes);
    }

    @Override
    public void onFetchError(String name, SkadiFetchConfig.ObjectType objectType, Exception error) {
        log.warn("[{}] SKADI fetch error for {} {}: {}", sessionId, objectType, name,
                error.getMessage());
        emitter.warn(EventType.FILE_PARSING_FAILED, sessionId, Map.of(
                "action",     "skadi_fetch_error",
                "objectType", objectType.name(),
                "object",     name != null ? name : "",
                "error",      error.getMessage() != null ? error.getMessage() : ""));
    }

    @Override
    public void onFetchCompleted(SkadiFetchResult.FetchStats stats) {
        log.info("[{}] SKADI fetch completed: adapter={} fetched={} errors={} durationMs={}",
                sessionId, stats.sourceAdapter(), stats.totalFetched(),
                stats.errors(), stats.durationMs());
        emitter.info(EventType.SESSION_COMPLETED, sessionId, Map.of(
                "action",    "skadi_fetch_completed",
                "adapter",   stats.sourceAdapter(),
                "fetched",   stats.totalFetched(),
                "errors",    stats.errors(),
                "durationMs", stats.durationMs()));
    }
}
