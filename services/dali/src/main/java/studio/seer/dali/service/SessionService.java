package studio.seer.dali.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.job.ParseJob;
import studio.seer.shared.ParseSessionInput;
import studio.seer.shared.Session;
import studio.seer.shared.SessionStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages Dali parse sessions.
 *
 * <p>Session state is held in a {@link ConcurrentHashMap} for now.
 * In Д8–Д9 this will be backed by FRIGG (ArcadeDB) so sessions survive
 * service restarts and are visible across instances.
 */
@ApplicationScoped
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    @Inject JobScheduler jobScheduler;
    @Inject ParseJob     parseJob;   // CDI proxy — JobRunr serialises the call to ParseJob.execute

    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Enqueues a new parse session and returns the initial {@link Session} record.
     *
     * @param input dialect + source path + preview flag
     * @return {@code Session} with status {@code QUEUED}
     */
    public Session enqueue(ParseSessionInput input) {
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Session session = new Session(sessionId, SessionStatus.QUEUED,
                0, 0, input.dialect(), now, now);
        sessions.put(sessionId, session);

        jobScheduler.<ParseJob>enqueue(j -> j.execute(sessionId, input));

        log.info("Session enqueued: id={} dialect={} source={}", sessionId, input.dialect(), input.source());
        return session;
    }

    /**
     * Returns the current state of a session, or empty if not found.
     */
    public Optional<Session> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    /**
     * Updates session status — called by {@link ParseJob} as it progresses.
     */
    public void updateStatus(String id, SessionStatus status) {
        sessions.computeIfPresent(id, (k, s) ->
                new Session(s.id(), status, s.progress(), s.total(),
                        s.dialect(), s.startedAt(), Instant.now()));
        log.debug("Session status updated: id={} status={}", id, status);
    }
}
