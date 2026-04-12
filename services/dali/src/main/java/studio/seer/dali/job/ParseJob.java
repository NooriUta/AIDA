package studio.seer.dali.job;

import com.hound.api.ArcadeWriteMode;
import com.hound.api.HoundConfig;
import com.hound.api.HoundParser;
import com.hound.api.ParseResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.service.SessionService;
import studio.seer.shared.ParseSessionInput;
import studio.seer.shared.SessionStatus;

import java.nio.file.Path;

/**
 * JobRunr background job — parses a SQL file/directory via Hound in-JVM.
 *
 * <p>This class is CDI-managed ({@code @ApplicationScoped}) and resolved by
 * {@code JobRunrProducer}'s {@code JobActivator} at execution time, so all
 * {@code @Inject} fields are available inside {@link #execute}.
 *
 * <p>Retry policy: 3 attempts (configurable via {@code @Job}).
 */
@ApplicationScoped
public class ParseJob {

    private static final Logger log = LoggerFactory.getLogger(ParseJob.class);

    @Inject HoundParser  houndParser;
    @Inject SessionService sessionService;

    /**
     * Executes the parse session.
     *
     * @param sessionId UUID string — matches the session tracked in {@link SessionService}
     * @param input     dialect, source path, and preview flag
     */
    @Job(name = "Parse SQL files", retries = 3)
    public void execute(String sessionId, ParseSessionInput input) {
        log.info("[{}] ParseJob starting: source={} dialect={} preview={}",
                sessionId, input.source(), input.dialect(), input.preview());

        sessionService.updateStatus(sessionId, SessionStatus.RUNNING);

        try {
            HoundConfig config = input.preview()
                    ? HoundConfig.defaultDisabled(input.dialect())
                    : HoundConfig.defaultDisabled(input.dialect()); // TODO(Д9): use REMOTE when FRIGG is configured

            DaliHoundListener listener = new DaliHoundListener(sessionId);
            ParseResult result = houndParser.parse(Path.of(input.source()), config, listener);

            log.info("[{}] ParseJob completed: atoms={} vertices={} duration={}ms",
                    sessionId, result.atomCount(), result.vertexCount(), result.durationMs());
            sessionService.updateStatus(sessionId, SessionStatus.COMPLETED);

        } catch (Exception e) {
            log.error("[{}] ParseJob failed: {}", sessionId, e.getMessage(), e);
            sessionService.updateStatus(sessionId, SessionStatus.FAILED);
            throw new RuntimeException("ParseJob failed for session " + sessionId, e);
        }
    }
}
