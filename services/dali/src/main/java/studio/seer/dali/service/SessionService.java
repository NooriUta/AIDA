package studio.seer.dali.service;

import com.hound.api.ParseResult;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.dali.job.ParseJob;
import studio.seer.dali.storage.FriggSchemaInitializer;
import studio.seer.dali.storage.SessionRepository;
import studio.seer.shared.FileResult;
import studio.seer.shared.ParseSessionInput;
import studio.seer.shared.Session;
import studio.seer.shared.SessionStatus;
import studio.seer.shared.VertexTypeStat;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Quarkus fires lower @Priority values first. @Priority(20) fires last — after
// FriggSchemaInitializer(5) sets up the schema and JobRunrLifecycle(10) starts JobRunr.
@ApplicationScoped
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    // Instance<> defers resolution until first use — JobScheduler is not available
    // at StartupEvent time (JobRunrLifecycle initialises it in its own onStart handler).
    @Inject Instance<JobScheduler>   jobScheduler;
    @Inject SessionRepository        repository;
    @Inject HeimdallEmitter          emitter;
    @Inject FriggSchemaInitializer   schemaInitializer;   // BUG-SS-012: guard enqueue()

    /**
     * Dali instance identifier — set via {@code dali.instance.id} / {@code DALI_INSTANCE_ID} env.
     * Absent / empty = "untagged" / single-instance mode (backward-compat).
     * Present non-empty = used to isolate sessions when multiple Dali instances share one FRIGG.
     */
    @ConfigProperty(name = "dali.instance.id")
    Optional<String> instanceId;

    private final ConcurrentMap<String, Session> sessions    = new ConcurrentHashMap<>();
    /** Maps sessionId → JobRunr job UUID for cancel support. Not persisted — rebuilt on restart. */
    private final ConcurrentMap<String, UUID>    jobRunrIdMap = new ConcurrentHashMap<>();

    private volatile boolean friggHealthy = false;

    /** Effective instance tag: null when absent or blank (untagged/backward-compat). */
    private String myInstanceId() {
        return instanceId.filter(s -> !s.isBlank()).map(String::trim).orElse(null);
    }

    /**
     * Loads persisted sessions from FRIGG into the in-memory cache on startup.
     * Guaranteed to run after {@link FriggSchemaInitializer} ({@code @Priority(5)}) and
     * {@code JobRunrLifecycle} ({@code @Priority(10)}) due to {@code @Priority(20)} on this bean.
     *
     * <p>Instance isolation: if {@code dali.instance.id} is set, only sessions whose
     * {@code instanceId} matches (or is null for backward-compat untagged sessions) are loaded.
     */
    void onStart(@Observes @Priority(20) StartupEvent ev) {
        try {
            List<Session> persisted = repository.findAll(500);
            String myId = myInstanceId();
            int loaded = 0, reset = 0, skipped = 0;
            for (Session s : persisted) {
                // Instance isolation: skip sessions belonging to other instances
                if (myId != null && s.instanceId() != null && !myId.equals(s.instanceId())) {
                    skipped++;
                    continue;
                }
                // JobRunr jobs survived the restart (ArcadeDB-backed), but sessions in
                // QUEUED or RUNNING state may have been interrupted mid-execution.
                // Mark them FAILED so they don't get stuck forever.
                if (s.status() == SessionStatus.QUEUED || s.status() == SessionStatus.RUNNING) {
                    Session failed = new Session(
                            s.id(), SessionStatus.FAILED,
                            s.progress(), s.total(), s.batch(),
                            s.clearBeforeWrite(),
                            s.dialect(), s.source(), s.startedAt(), Instant.now(),
                            s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                            s.vertexStats(),
                            s.resolutionRate(), s.durationMs(),
                            s.warnings(),
                            List.of("Server restarted — session was QUEUED/RUNNING and could not be recovered"),
                            s.fileResults(),
                            false, s.instanceId(), s.dbName());
                    sessions.put(failed.id(), failed);
                    persist(failed);
                    reset++;
                } else {
                    // Sessions loaded from FRIGG are by definition persisted
                    Session withFrigg = s.friggPersisted() ? s : new Session(
                            s.id(), s.status(), s.progress(), s.total(), s.batch(),
                            s.clearBeforeWrite(), s.dialect(), s.source(),
                            s.startedAt(), s.updatedAt(),
                            s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                            s.vertexStats(), s.resolutionRate(), s.durationMs(),
                            s.warnings(), s.errors(), s.fileResults(), true, s.instanceId(), s.dbName());
                    sessions.put(withFrigg.id(), withFrigg);
                    loaded++;
                }
            }
            friggHealthy = true;
            log.info("SessionService: loaded {} session(s) from FRIGG, reset {} stale QUEUED/RUNNING → FAILED, skipped {} (other instances)",
                    loaded, reset, skipped);
        } catch (Exception e) {
            friggHealthy = false;
            log.warn("SessionService: could not load sessions from FRIGG (FRIGG may be unavailable): {}",
                    e.getMessage());
        }
    }

    /** Enqueue a new parse session. */
    public Session enqueue(ParseSessionInput input) {
        // BUG-SS-012: refuse new jobs if the FRIGG schema is not ready
        if (!schemaInitializer.isSchemaReady()) {
            throw new IllegalStateException(
                "FRIGG schema is not fully initialised — Dali started with a broken FRIGG connection. " +
                "Check startup logs and restart the service.");
        }
        // Concurrency guard for clearBeforeWrite operations
        if (!input.preview()) {
            if (input.clearBeforeWrite()) {
                // clearBeforeWrite=true: no other active session allowed
                boolean conflict = sessions.values().stream()
                        .anyMatch(s -> s.status() == SessionStatus.QUEUED
                                    || s.status() == SessionStatus.RUNNING);
                if (conflict) {
                    throw new IllegalStateException(
                            "Cannot start clearBeforeWrite session: another session is active. " +
                            "Wait for it to complete first.");
                }
            } else {
                // clearBeforeWrite=false: block if a clear-session is active
                boolean clearRunning = sessions.values().stream()
                        .anyMatch(s -> (s.status() == SessionStatus.QUEUED
                                     || s.status() == SessionStatus.RUNNING)
                                     && s.clearBeforeWrite());
                if (clearRunning) {
                    throw new IllegalStateException(
                            "Cannot start session: a clearBeforeWrite operation is in progress. " +
                            "Wait for it to complete first.");
                }
            }
        }
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Session session = new Session(
                sessionId, SessionStatus.QUEUED,
                0, 0, false,
                input.clearBeforeWrite(),
                input.dialect(), input.source(),
                now, now,
                null, null, null, null, List.of(), null, null,
                List.of(), List.of(), List.of(), false, myInstanceId(),
                (input.dbName() != null && !input.dbName().isBlank()) ? input.dbName().strip() : null);
        sessions.put(sessionId, session);
        persist(session);
        emitter.jobEnqueued(sessionId, input.source(), input.dialect());
        // BUG-SS-011: jobScheduler producer throws IllegalStateException if JobRunr
        // failed to initialise at startup; surface as a meaningful error rather than NPE.
        try {
            org.jobrunr.jobs.JobId jrJobId =
                jobScheduler.get().<ParseJob>enqueue(j -> j.execute(sessionId, input));
            jobRunrIdMap.put(sessionId, UUID.fromString(jrJobId.toString()));
        } catch (Exception e) {
            log.error("Failed to enqueue job for session {}: {}", sessionId, e.getMessage());
            throw new IllegalStateException(
                "Job scheduling unavailable (JobRunr did not initialise — check startup logs): " + e.getMessage(), e);
        }
        log.info("Session enqueued: id={} dialect={} source={} instance={}", sessionId, input.dialect(), input.source(), myInstanceId());
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
                s.clearBeforeWrite(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                null, null, null, null, List.of(), null, null,
                List.of(), List.of(), List.of(), false, s.instanceId(), s.dbName()));
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
                    s.clearBeforeWrite(),
                    s.dialect(), s.source(), s.startedAt(), Instant.now(),
                    s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                    s.vertexStats(),
                    s.resolutionRate(), s.durationMs(),
                    s.warnings(), s.errors(), List.copyOf(list), false, s.instanceId(), s.dbName());
        });
        if (updated != null) persist(updated);
    }

    /** Called by ParseJob for RUNNING→FAILED transition. */
    public void updateStatus(String id, SessionStatus status) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), status, s.progress(), s.total(), s.batch(),
                s.clearBeforeWrite(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                s.vertexStats(),
                s.resolutionRate(), s.durationMs(),
                s.warnings(), s.errors(), s.fileResults(), false, s.instanceId(), s.dbName()));
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
                    s.clearBeforeWrite(),
                    s.dialect(), s.source(), s.startedAt(), Instant.now(),
                    s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                    s.vertexStats(),
                    s.resolutionRate(), s.durationMs(),
                    s.warnings(), List.copyOf(errors), s.fileResults(), false, s.instanceId(), s.dbName());
        });
        if (updated != null) persist(updated);
        log.debug("Session failed: id={} error={}", id, errorMessage);
    }

    /** Called when parse finishes successfully — stores aggregate result. */
    public void completeSession(String id, ParseResult result, List<FileResult> fileResults) {
        List<VertexTypeStat> vtxStats = toVertexTypeStats(result.vertexStats());
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), SessionStatus.COMPLETED,
                s.total() > 0 ? s.total() : 1,
                s.total() > 0 ? s.total() : 1,
                s.batch(),
                s.clearBeforeWrite(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                result.atomCount(),
                result.vertexCount(),
                result.edgeCount(),
                result.droppedEdgeCount(),
                vtxStats,
                result.resolutionRate(),
                result.durationMs(),
                result.warnings() != null ? result.warnings() : List.of(),
                result.errors()   != null ? result.errors()   : List.of(),
                fileResults, false, s.instanceId(), s.dbName()));
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

    /** Returns true if the last FRIGG operation succeeded. */
    public boolean isFriggHealthy() { return friggHealthy; }

    /**
     * Purges terminal sessions older than {@code cutoff} from in-memory cache and FRIGG.
     * Active (QUEUED/RUNNING) sessions are never evicted regardless of age.
     */
    public void purgeExpired(java.time.Instant cutoff) {
        // Evict from in-memory cache — skip active sessions
        int evicted = 0;
        for (var it = sessions.entrySet().iterator(); it.hasNext(); ) {
            Session s = it.next().getValue();
            if (s.startedAt().isBefore(cutoff)
                    && s.status() != SessionStatus.QUEUED
                    && s.status() != SessionStatus.RUNNING) {
                it.remove();
                evicted++;
            }
        }
        // Delete from FRIGG
        int deleted = repository.deleteOlderThan(cutoff);
        log.info("SessionRetention: evicted {} from memory, deleted {} from FRIGG (older than {})",
                evicted, deleted, cutoff);
    }

    /**
     * Cancels a session: removes it from the JobRunr queue (if still QUEUED/ENQUEUED)
     * and transitions it to CANCELLED state.
     *
     * <p>If the job is already RUNNING the cancellation is best-effort — the job
     * may still complete, but the session is marked CANCELLED immediately.
     *
     * @return {@code CancelResult} with status CANCELLING | NOT_FOUND | ALREADY_DONE
     */
    public CancelResult cancelSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return new CancelResult("NOT_FOUND", "Session not found: " + sessionId);
        }
        if (session.status() == SessionStatus.COMPLETED
                || session.status() == SessionStatus.FAILED
                || session.status() == SessionStatus.CANCELLED) {
            return new CancelResult("ALREADY_DONE",
                    "Session already in terminal state: " + session.status());
        }

        // Ask JobRunr to delete the job (no-op if already picked up by a worker)
        UUID jrId = jobRunrIdMap.remove(sessionId);
        if (jrId != null) {
            try {
                jobScheduler.get().delete(jrId);
                log.info("[cancelSession] JobRunr job {} deleted for session {}", jrId, sessionId);
            } catch (Exception e) {
                log.warn("[cancelSession] JobRunr delete failed for session {} (job {}): {}",
                        sessionId, jrId, e.getMessage());
            }
        } else {
            log.warn("[cancelSession] No JobRunr ID tracked for session {} — may have restarted", sessionId);
        }

        // Transition session to CANCELLED and persist
        sessions.computeIfPresent(sessionId, (k, s) -> new Session(
                s.id(), SessionStatus.CANCELLED, s.progress(), s.total(), s.batch(),
                s.clearBeforeWrite(), s.dialect(), s.source(), s.startedAt(), java.time.Instant.now(),
                s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                s.vertexStats(), s.resolutionRate(), s.durationMs(),
                s.warnings(), List.of("Cancelled by user"), s.fileResults(), false, s.instanceId(), s.dbName()));

        Session cancelled = sessions.get(sessionId);
        if (cancelled != null) persist(cancelled);

        emitter.sessionCancelled(sessionId);
        log.info("[cancelSession] Session {} marked CANCELLED", sessionId);
        return new CancelResult("CANCELLING", "Session marked CANCELLED; JobRunr deletion requested");
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    /** Converts ParseResult's internal Map<type,[ins,dup]> to shared VertexTypeStat list. */
    private static List<VertexTypeStat> toVertexTypeStats(java.util.Map<String, int[]> map) {
        if (map == null || map.isEmpty()) return List.of();
        var out = new java.util.ArrayList<VertexTypeStat>(map.size());
        map.forEach((type, counts) -> out.add(new VertexTypeStat(type, counts[0], counts[1])));
        return List.copyOf(out);
    }

    private void persist(Session session) {
        try {
            repository.save(session);
            friggHealthy = true;
            // Mark as persisted in the in-memory map (only if the session is still there)
            if (!session.friggPersisted()) {
                sessions.computeIfPresent(session.id(), (k, s) -> new Session(
                        s.id(), s.status(), s.progress(), s.total(), s.batch(),
                        s.clearBeforeWrite(), s.dialect(), s.source(),
                        s.startedAt(), s.updatedAt(),
                        s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                        s.vertexStats(), s.resolutionRate(), s.durationMs(),
                        s.warnings(), s.errors(), s.fileResults(), true, s.instanceId(), s.dbName()));
            }
        } catch (Exception e) {
            friggHealthy = false;
            log.warn("SessionService: failed to persist session {} to FRIGG: {}", session.id(), e.getMessage());
        }
    }
}
