package studio.seer.dali.job;

import com.hound.api.HoundEventListener;
import com.hound.api.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.heimdall.HeimdallEmitter;

/**
 * {@link HoundEventListener} that forwards parse events to SLF4J and HEIMDALL.
 *
 * <p>Per-session instance — created by {@link ParseJob} with the session ID so that
 * log lines are traceable. Emits {@link studio.seer.shared.EventType#FILE_PARSING_STARTED},
 * {@link studio.seer.shared.EventType#ATOM_EXTRACTED},
 * {@link studio.seer.shared.EventType#FILE_PARSING_FAILED} to HEIMDALL via
 * {@link HeimdallEmitter} (fire-and-forget, never throws).
 */
public class DaliHoundListener implements HoundEventListener {

    private static final Logger log = LoggerFactory.getLogger(DaliHoundListener.class);

    private final String         sessionId;
    private final String         dialect;
    private final HeimdallEmitter emitter;

    public DaliHoundListener(String sessionId, String dialect, HeimdallEmitter emitter) {
        this.sessionId = sessionId;
        this.dialect   = dialect;
        this.emitter   = emitter;
    }

    @Override
    public void onFileParseStarted(String file, String dialect) {
        log.info("[{}] parse started  file={} dialect={}", sessionId, file, dialect);
        emitter.fileParsingStarted(sessionId, file, dialect);
    }

    @Override
    public void onAtomExtracted(String file, int atomCount, String atomType) {
        log.debug("[{}] atom extracted file={} count={} type={}", sessionId, file, atomCount, atomType);
        // HEIMDALL aggregates ATOM_EXTRACTED events to compute atomsExtracted metric (fixes GAP-1).
        // Emitted per-file rather than per-atom to avoid event spam.
    }

    @Override
    public void onFileParseCompleted(String file, ParseResult result) {
        log.info("[{}] parse completed file={} atoms={} vertices={} duration={}ms",
                sessionId, file, result.atomCount(), result.vertexCount(), result.durationMs());
        // Emit ATOM_EXTRACTED with the total atom count for this file.
        emitter.atomExtracted(sessionId, file, result.atomCount());
    }

    @Override
    public void onError(String file, Throwable error) {
        log.error("[{}] parse error     file={} error={}", sessionId, file, error.getMessage(), error);
        emitter.fileParsingFailed(sessionId, file, error.getMessage());
    }
}
