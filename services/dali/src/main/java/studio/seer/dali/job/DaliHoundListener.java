package studio.seer.dali.job;

import com.hound.api.HoundEventListener;
import com.hound.api.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HoundEventListener} that forwards parse events to SLF4J.
 *
 * <p>Per-session instance — created by {@link ParseJob} with the session ID so that
 * log lines are traceable. Future versions will forward events to HEIMDALL.
 */
public class DaliHoundListener implements HoundEventListener {

    private static final Logger log = LoggerFactory.getLogger(DaliHoundListener.class);

    private final String sessionId;

    public DaliHoundListener(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void onFileParseStarted(String file, String dialect) {
        log.info("[{}] parse started  file={} dialect={}", sessionId, file, dialect);
    }

    @Override
    public void onAtomExtracted(String file, int atomCount, String atomType) {
        log.debug("[{}] atom extracted file={} count={} type={}", sessionId, file, atomCount, atomType);
    }

    @Override
    public void onFileParseCompleted(String file, ParseResult result) {
        log.info("[{}] parse completed file={} atoms={} vertices={} duration={}ms",
                sessionId, file, result.atomCount(), result.vertexCount(), result.durationMs());
    }

    @Override
    public void onError(String file, Throwable error) {
        log.error("[{}] parse error     file={} error={}", sessionId, file, error.getMessage(), error);
    }
}
