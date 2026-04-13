package studio.seer.dali.service;

import com.hound.api.ParseResult;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.job.ParseJob;
import studio.seer.dali.storage.SessionRepository;
import studio.seer.shared.FileResult;
import studio.seer.shared.ParseSessionInput;
import studio.seer.shared.Session;
import studio.seer.shared.SessionStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    // Instance<> defers resolution until first use — JobScheduler is not available
    // at StartupEvent time (JobRunrLifecycle initialises it in its own onStart handler).
    @Inject Instance<JobScheduler> jobScheduler;
    @Inject SessionRepository      repository;

    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Loads persisted sessions from FRIGG into the in-memory cache on startup.
     * Runs after FriggSchemaInitializer (both observe StartupEvent; schema init
     * is a separate bean that fires first in practice since it is declared earlier,
     * but the load is wrapped in try-catch so it degrades gracefully if FRIGG is down).
     */
    void onStart(@Observes StartupEvent ev) {
        try {
            List<Session> persisted = repository.findAll(500);
            int loaded = 0, reset = 0;
            for (Session s : persisted) {
                // JobRunr uses InMemoryStorageProvider — all queued jobs are lost on restart.
                // Mark any QUEUED or RUNNING sessions as FAILED so they don't get stuck forever.
                if (s.status() == SessionStatus.QUEUED || s.status() == SessionStatus.RUNNING) {
                    Session failed = new Session(
                            s.id(), SessionStatus.FAILED,
                            s.progress(), s.total(), s.batch(),
                            s.dialect(), s.source(), s.startedAt(), Instant.now(),
                            s.atomCount(), s.vertexCount(), s.edgeCount(),
                            s.resolutionRate(), s.durationMs(),
                            s.warnings(),
                            List.of("Server restarted — job was not executed (in-memory queue lost)"),
                            s.fileResults());
                    sessions.put(failed.id(), failed);
                    persist(failed);
                    reset++;
                } else {
                    sessions.put(s.id(), s);
                    loaded++;
                }
            }
            log.info("SessionService: loaded {} session(s) from FRIGG, reset {} stale QUEUED/RUNNING → FAILED",
                    loaded, reset);
        } catch (Exception e) {
            log.warn("SessionService: could not load sessions from FRIGG (FRIGG may be unavailable): {}",
                    e.getMessage());
        }
    }

    /** Enqueue a new parse session. */
    public Session enqueue(ParseSessionInput input) {
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Session session = new Session(
                sessionId, SessionStatus.QUEUED,
                0, 0, false,
                input.dialect(), input.source(),
                now, now,
                null, null, null, null, null,
                List.of(), List.of(), List.of());
        sessions.put(sessionId, session);
        persist(session);
        jobScheduler.get().<ParseJob>enqueue(j -> j.execute(sessionId, input));
        log.info("Session enqueued: id={} dialect={} source={}", sessionId, input.dialect(), input.source());
        return session;
    }

    /** Returns the current state, or empty if unknown. */
    public Optional<Session> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    /** Called by ParseJob when it begins — sets RUNNING and marks batch/total. */
    public void startSession(String id, boolean batch, int total) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), SessionStatus.RUNNING,
                0, total, batch,
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                null, null, null, null, null,
                List.of(), List.of(), List.of()));
        if (updated != null) persist(updated);
        log.debug("Session started: id={} batch={} total={}", id, batch, total);
    }

    /** Called after each file completes in a batch — increments progress, appends FileResult. */
    public void recordFileComplete(String id, FileResult fileResult) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> {
            var list = new java.util.ArrayList<>(s.fileResults());
            list.add(fileResult);
            return new Session(
                    s.id(), s.status(),
                    s.progress() + 1, s.total(), s.batch(),
                    s.dialect(), s.source(), s.startedAt(), Instant.now(),
                    s.atomCount(), s.vertexCount(), s.edgeCount(),
                    s.resolutionRate(), s.durationMs(),
                    s.warnings(), s.errors(), List.copyOf(list));
        });
        if (updated != null) persist(updated);
    }

    /** Called by ParseJob for RUNNING→FAILED transition. */
    public void updateStatus(String id, SessionStatus status) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), status, s.progress(), s.total(), s.batch(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                s.atomCount(), s.vertexCount(), s.edgeCount(),
                s.resolutionRate(), s.durationMs(),
                s.warnings(), s.errors(), s.fileResults()));
        if (updated != null) persist(updated);
        log.debug("Session status updated: id={} status={}", id, status);
    }

    /**
     * Called by ParseJob when parse fails — stores error message in session errors list.
     * Preserves any partial file results already recorded.
     */
    public void failSession(String id, String errorMessage) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> {
            var errors = new java.util.ArrayList<>(s.errors() != null ? s.errors() : List.of());
            if (errorMessage != null && !errorMessage.isBlank()) {
                errors.add(errorMessage);
            }
            return new Session(
                    s.id(), SessionStatus.FAILED, s.progress(), s.total(), s.batch(),
                    s.dialect(), s.source(), s.startedAt(), Instant.now(),
                    s.atomCount(), s.vertexCount(), s.edgeCount(),
                    s.resolutionRate(), s.durationMs(),
                    s.warnings(), List.copyOf(errors), s.fileResults());
        });
        if (updated != null) persist(updated);
        log.debug("Session failed: id={} error={}", id, errorMessage);
    }

    /** Called when parse finishes successfully — stores aggregate result. */
    public void completeSession(String id, ParseResult result, List<FileResult> fileResults) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), SessionStatus.COMPLETED,
                s.total() > 0 ? s.total() : 1,
                s.total() > 0 ? s.total() : 1,
                s.batch(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                result.atomCount(),
                result.vertexCount(),
                result.edgeCount(),
                result.resolutionRate(),
                result.durationMs(),
                result.warnings() != null ? result.warnings() : List.of(),
                result.errors()   != null ? result.errors()   : List.of(),
                fileResults));
        if (updated != null) persist(updated);
        log.info("Session completed: id={} atoms={} files={} duration={}ms",
                id, result.atomCount(), fileResults.size(), result.durationMs());
    }

    /** Returns the most recent sessions, newest first. */
    public List<Session> listRecent(int limit) {
        return sessions.values().stream()
                .sorted(Comparator.comparing(Session::startedAt).reversed())
                .limit(limit)
                .toList();
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private void persist(Session session) {
        try {
            repository.save(session);
        } catch (Exception e) {
            log.warn("SessionService: failed to persist session {} to FRIGG: {}", session.id(), e.getMessage());
        }
    }
}
