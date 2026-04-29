package studio.seer.dali.service;

import com.hound.api.ParseResult;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jobrunr.scheduling.JobRequestScheduler;
import studio.seer.dali.job.ParseJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.heimdall.HeimdallEmitter;
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

    @Inject Instance<JobRequestScheduler> jobRequestScheduler;
    @Inject SessionRepository        repository;
    @Inject HeimdallEmitter          emitter;
    @Inject FriggSchemaInitializer   schemaInitializer;

    @ConfigProperty(name = "dali.instance.id")
    Optional<String> instanceId;

    private final ConcurrentMap<String, Session> sessions     = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID>    jobRunrIdMap = new ConcurrentHashMap<>();

    private volatile boolean friggHealthy = false;

    private String myInstanceId() {
        return instanceId.filter(s -> !s.isBlank()).map(String::trim).orElse(null);
    }

    /**
     * Loads persisted sessions from FRIGG into the in-memory cache on startup.
     * Loads only from the "default" tenant DB in single-tenant mode.
     */
    void onStart(@Observes @Priority(20) StartupEvent ev) {
        try {
            List<Session> persisted = repository.findAll("default", 500);
            String myId = myInstanceId();
            int loaded = 0, reset = 0, skipped = 0;
            for (Session s : persisted) {
                if (myId != null && s.instanceId() != null && !myId.equals(s.instanceId())) {
                    skipped++;
                    continue;
                }
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
                            false, s.instanceId(), s.dbName(), s.tenantAlias());
                    sessions.put(failed.id(), failed);
                    persist(failed);
                    reset++;
                } else {
                    Session withFrigg = s.friggPersisted() ? s : new Session(
                            s.id(), s.status(), s.progress(), s.total(), s.batch(),
                            s.clearBeforeWrite(), s.dialect(), s.source(),
                            s.startedAt(), s.updatedAt(),
                            s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                            s.vertexStats(), s.resolutionRate(), s.durationMs(),
                            s.warnings(), s.errors(), s.fileResults(), true, s.instanceId(), s.dbName(),
                            s.tenantAlias());
                    sessions.put(withFrigg.id(), withFrigg);
                    loaded++;
                }
            }
            friggHealthy = true;
            log.info("SessionService: loaded {} session(s) from FRIGG, reset {} stale, skipped {} (other instances)",
                    loaded, reset, skipped);
        } catch (Exception e) {
            friggHealthy = false;
            log.warn("SessionService: could not load sessions from FRIGG: {}", e.getMessage());
        }
    }

    /**
     * Enqueue a new parse session. The tenantAlias in input is derived from TenantContext.
     *
     * <p>MTN-04: fail-fast on missing alias. Caller (SessionResource, FileUploadResource,
     * HarvestJob) MUST populate {@code input.tenantAlias()} from the resolved
     * {@code TenantContext} — no silent "default" fallback here.
     */
    public Session enqueue(ParseSessionInput input) {
        if (!schemaInitializer.isSchemaReady()) {
            throw new IllegalStateException(
                "FRIGG schema is not fully initialised — Dali started with a broken FRIGG connection. " +
                "Check startup logs and restart the service.");
        }
        String tenantAlias = input.tenantAlias();
        if (tenantAlias == null || tenantAlias.isBlank()) {
            throw new IllegalStateException(
                "MTN-04: ParseSessionInput.tenantAlias is required — caller must inject it " +
                "from TenantContext before enqueue(). Refusing to default to 'default' which " +
                "would leak parse data into the wrong tenant database.");
        }

        // Instance-level serialization: workerThreads=1 means JobRunr queues
        // new jobs automatically. No rejection — any submitted job will run
        // once the current one finishes, regardless of tenant.
        if (!input.preview()) {
            boolean hasActive = sessions.values().stream()
                    .anyMatch(s -> s.status() == SessionStatus.QUEUED
                               || s.status() == SessionStatus.RUNNING);
            if (hasActive) {
                log.info("Session will queue — another parse is active on this instance (tenant={})", tenantAlias);
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
                (input.dbName() != null && !input.dbName().isBlank()) ? input.dbName().strip() : null,
                tenantAlias);
        sessions.put(sessionId, session);
        persist(session);
        emitter.jobEnqueued(sessionId, input.source(), input.dialect());
        try {
            org.jobrunr.jobs.JobId jrJobId = scheduleParseJob(sessionId, input);
            jobRunrIdMap.put(sessionId, UUID.fromString(jrJobId.toString()));
        } catch (Exception e) {
            log.error("Failed to enqueue job for session {}: {}", sessionId, e.getMessage(), e);
            throw new IllegalStateException(
                "Job scheduling unavailable (JobRunr did not initialise — check startup logs): " + e.getMessage(), e);
        }
        log.info("Session enqueued: id={} dialect={} source={} tenant={} instance={}",
                sessionId, input.dialect(), input.source(), tenantAlias, myInstanceId());
        return session;
    }

    /** Returns the current state, or empty if unknown. */
    public Optional<Session> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    /** Returns a session only if it belongs to the given tenant (cross-tenant guard). */
    public Optional<Session> findForTenant(String id, String tenantAlias) {
        return Optional.ofNullable(sessions.get(id))
                .filter(s -> tenantAlias.equals(s.tenantAlias()));
    }

    public void startSession(String id, boolean batch, int total) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), SessionStatus.RUNNING,
                0, total, batch,
                s.clearBeforeWrite(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                null, null, null, null, List.of(), null, null,
                List.of(), List.of(), List.of(), false, s.instanceId(), s.dbName(), s.tenantAlias()));
        if (updated != null) persist(updated);
        log.debug("Session started: id={} batch={} total={}", id, batch, total);
    }

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
                    s.warnings(), s.errors(), List.copyOf(list), false, s.instanceId(), s.dbName(),
                    s.tenantAlias());
        });
        if (updated != null) persist(updated);
    }

    public void updateStatus(String id, SessionStatus status) {
        Session updated = sessions.computeIfPresent(id, (k, s) -> new Session(
                s.id(), status, s.progress(), s.total(), s.batch(),
                s.clearBeforeWrite(),
                s.dialect(), s.source(), s.startedAt(), Instant.now(),
                s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                s.vertexStats(),
                s.resolutionRate(), s.durationMs(),
                s.warnings(), s.errors(), s.fileResults(), false, s.instanceId(), s.dbName(),
                s.tenantAlias()));
        if (updated != null) persist(updated);
        log.debug("Session status updated: id={} status={}", id, status);
    }

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
                    s.warnings(), List.copyOf(errors), s.fileResults(), false, s.instanceId(), s.dbName(),
                    s.tenantAlias());
        });
        if (updated != null) persist(updated);
        log.debug("Session failed: id={} error={}", id, errorMessage);
    }

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
                fileResults, false, s.instanceId(), s.dbName(), s.tenantAlias()));
        if (updated != null) persist(updated);
        log.info("Session completed: id={} atoms={} files={} duration={}ms tenant={}",
                id, result.atomCount(), fileResults.size(), result.durationMs(),
                updated != null ? updated.tenantAlias() : "?");
    }

    /**
     * Returns the most recent sessions for a tenant, newest first.
     */
    public List<Session> listRecent(String tenantAlias, int limit) {
        return sessions.values().stream()
                .filter(s -> tenantAlias.equals(s.tenantAlias()))
                .sorted(Comparator.comparing(Session::startedAt).reversed())
                .limit(limit)
                .toList();
    }

    /** Backward-compat overload — returns sessions for "default" tenant. */
    public List<Session> listRecent(int limit) {
        return listRecent("default", limit);
    }

    /**
     * Returns sessions across ALL tenants, newest first.
     * Intended for superadmin multi-tenant view (UC-S07).
     */
    public List<Session> listAllTenants(int limit) {
        return sessions.values().stream()
                .sorted(Comparator.comparing(Session::startedAt).reversed())
                .limit(limit)
                .toList();
    }

    public boolean isFriggHealthy() { return friggHealthy; }

    public void purgeExpired(java.time.Instant cutoff) {
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
        // Purge from all tenant DBs represented in memory
        sessions.values().stream()
                .map(Session::tenantAlias)
                .distinct()
                .forEach(alias -> repository.deleteOlderThan(cutoff, alias));
        int deleted = repository.deleteOlderThan(cutoff, "default");
        log.info("SessionRetention: evicted {} from memory, deleted {} from FRIGG default (older than {})",
                evicted, deleted, cutoff);
    }

    /**
     * Cancels a session. Enforces tenant isolation — only the owning tenant can cancel.
     * Pass {@code null} as tenantAlias to bypass tenant check (superadmin).
     */
    public CancelResult cancelSession(String sessionId, String tenantAlias) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return new CancelResult("NOT_FOUND", "Session not found: " + sessionId);
        }
        // Tenant isolation check (null = superadmin bypass)
        if (tenantAlias != null && !tenantAlias.equals(session.tenantAlias())) {
            return new CancelResult("FORBIDDEN",
                    "Session " + sessionId + " does not belong to tenant " + tenantAlias);
        }
        if (session.status() == SessionStatus.COMPLETED
                || session.status() == SessionStatus.FAILED
                || session.status() == SessionStatus.CANCELLED) {
            return new CancelResult("ALREADY_DONE",
                    "Session already in terminal state: " + session.status());
        }

        UUID jrId = jobRunrIdMap.remove(sessionId);
        if (jrId != null) {
            try {
                jobRequestScheduler.get().delete(jrId);
                log.info("[cancelSession] JobRunr job {} deleted for session {}", jrId, sessionId);
            } catch (Exception e) {
                log.warn("[cancelSession] JobRunr delete failed for session {} (job {}): {}",
                        sessionId, jrId, e.getMessage());
            }
        } else {
            log.warn("[cancelSession] No JobRunr ID tracked for session {} — may have restarted", sessionId);
        }

        sessions.computeIfPresent(sessionId, (k, s) -> new Session(
                s.id(), SessionStatus.CANCELLED, s.progress(), s.total(), s.batch(),
                s.clearBeforeWrite(), s.dialect(), s.source(), s.startedAt(), Instant.now(),
                s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                s.vertexStats(), s.resolutionRate(), s.durationMs(),
                s.warnings(), List.of("Cancelled by user"), s.fileResults(), false, s.instanceId(),
                s.dbName(), s.tenantAlias()));

        Session cancelled = sessions.get(sessionId);
        if (cancelled != null) persist(cancelled);

        emitter.sessionCancelled(sessionId);
        log.info("[cancelSession] Session {} marked CANCELLED (tenant={})", sessionId, tenantAlias);
        return new CancelResult("CANCELLING", "Session marked CANCELLED; JobRunr deletion requested");
    }

    /** Backward-compat — no tenant check. */
    public CancelResult cancelSession(String sessionId) {
        return cancelSession(sessionId, null);
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    /**
     * DMT-ASM-FIX: enqueues a {@link ParseJob} via {@link JobRequestScheduler}
     * to bypass JobRunr's ASM bytecode analyser.
     *
     * <p>The original lambda approach ({@code j -> j.execute(sessionId, input)}) caused
     * JobRunr to throw "Can not find variable 4 in stack" because Quarkus CDI
     * transformation shifts local variable slots in the capturing method, making the
     * virtual-stack tracker unable to locate the captured variables at the expected slot.
     *
     * <p>{@link JobRequestScheduler#enqueue(org.jobrunr.jobs.lambdas.JobRequest)} avoids
     * all lambda analysis: it serialises the {@link ParseJobRequest} object as the single
     * job parameter and calls {@link ParseJob#run(ParseJobRequest)} on the worker side.
     */
    private org.jobrunr.jobs.JobId scheduleParseJob(String sessionId, ParseSessionInput input) {
        return jobRequestScheduler.get().enqueue(new ParseJobRequest(sessionId, input));
    }

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
            if (!session.friggPersisted()) {
                sessions.computeIfPresent(session.id(), (k, s) -> new Session(
                        s.id(), s.status(), s.progress(), s.total(), s.batch(),
                        s.clearBeforeWrite(), s.dialect(), s.source(),
                        s.startedAt(), s.updatedAt(),
                        s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
                        s.vertexStats(), s.resolutionRate(), s.durationMs(),
                        s.warnings(), s.errors(), s.fileResults(), true, s.instanceId(), s.dbName(),
                        s.tenantAlias()));
            }
        } catch (Exception e) {
            friggHealthy = false;
            log.warn("SessionService: failed to persist session {} to FRIGG: {}", session.id(), e.getMessage());
        }
    }
}
