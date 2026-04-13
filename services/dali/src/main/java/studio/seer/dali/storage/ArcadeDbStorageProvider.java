package studio.seer.dali.storage;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.*;
import org.jobrunr.storage.navigation.AmountRequest;
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper;
import org.jobrunr.utils.resilience.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ArcadeDB (FRIGG) backed {@link StorageProvider} for JobRunr.
 *
 * <p>Persists job state in the {@code dali} database on FRIGG so that jobs
 * survive Dali service restarts. Schema is created by {@link FriggSchemaInitializer}.
 *
 * <p>Uses {@link JobMapper} (set by JobRunr via {@link #setJobMapper}) for
 * {@link Job} serialisation, and {@link JacksonJsonMapper} for other objects
 * ({@link BackgroundJobServerStatus}).
 *
 * <p><b>TODO(integration / Д10):</b> Wire this into
 * {@code JobRunrProducer.storageProvider()} to replace {@code InMemoryStorageProvider}:
 * <pre>{@code
 * @Inject FriggGateway frigg;
 *
 * @Produces @ApplicationScoped
 * public StorageProvider storageProvider() {
 *     return new ArcadeDbStorageProvider(frigg);
 * }
 * }</pre>
 * This class is intentionally NOT a CDI bean ({@code @ApplicationScoped}) to avoid
 * ambiguous-dependency errors while {@code InMemoryStorageProvider} is still active.
 */
public class ArcadeDbStorageProvider extends AbstractStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(ArcadeDbStorageProvider.class);

    private final FriggGateway  frigg;
    private final JacksonJsonMapper jsonMapper; // for BackgroundJobServerStatus
    private       JobMapper     jobMapper;       // for Job objects — set by JobRunr

    public ArcadeDbStorageProvider(FriggGateway frigg) {
        super(RateLimiter.Builder.rateLimit().withoutLimits());
        this.frigg       = frigg;
        this.jsonMapper  = new JacksonJsonMapper();
    }

    // ─── JobRunr lifecycle hooks ───────────────────────────────────────────────

    @Override
    public void setJobMapper(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    @Override
    public void setUpStorageProvider(StorageProviderUtils.DatabaseOptions options) {
        // Schema creation handled by FriggSchemaInitializer at StartupEvent
        log.info("ArcadeDbStorageProvider: setUpStorageProvider({})", options);
    }

    // ─── Background server heartbeat ──────────────────────────────────────────

    @Override
    public void announceBackgroundJobServer(BackgroundJobServerStatus status) {
        String json = jsonMapper.serialize(status);
        frigg.sql(
            "UPDATE `jobrunr_servers` SET data = :data, lastHeartbeat = :hb " +
            "UPSERT RETURN AFTER @rid WHERE id = :id",
            Map.of("id",   status.getId().toString(),
                   "data", json,
                   "hb",   status.getLastHeartbeat().toString()));
        log.debug("JobRunr: server announced id={}", status.getId());
    }

    @Override
    public boolean signalBackgroundJobServerAlive(BackgroundJobServerStatus status) {
        announceBackgroundJobServer(status);
        return true;
    }

    @Override
    public void signalBackgroundJobServerStopped(BackgroundJobServerStatus status) {
        frigg.sql("DELETE FROM `jobrunr_servers` WHERE id = :id",
                  Map.of("id", status.getId().toString()));
        log.info("JobRunr: server stopped id={}", status.getId());
    }

    @Override
    public List<BackgroundJobServerStatus> getBackgroundJobServers() {
        List<Map<String, Object>> rows =
                frigg.sql("SELECT data FROM `jobrunr_servers`", Map.of());
        if (rows == null) return List.of();
        return rows.stream()
                .map(r -> jsonMapper.deserialize((String) r.get("data"), BackgroundJobServerStatus.class))
                .collect(Collectors.toList());
    }

    @Override
    public UUID getLongestRunningBackgroundJobServerId() {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT id FROM `jobrunr_servers` ORDER BY lastHeartbeat ASC LIMIT 1", Map.of());
        if (rows == null || rows.isEmpty()) return null;
        Object id = rows.get(0).get("id");
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Override
    public int removeTimedOutBackgroundJobServers(Instant heartbeatOlderThan) {
        List<Map<String, Object>> r = frigg.sql(
            "DELETE FROM `jobrunr_servers` WHERE lastHeartbeat < :before RETURN BEFORE @rid",
            Map.of("before", heartbeatOlderThan.toString()));
        return r != null ? r.size() : 0;
    }

    // ─── Job CRUD ──────────────────────────────────────────────────────────────

    @Override
    public Job save(Job job) throws ConcurrentJobModificationException {
        String id    = job.getId().toString();
        String state = job.getJobState().getName().name();
        String json  = jobMapper.serializeJob(job);
        frigg.sql(
            "UPDATE `jobrunr_jobs` SET state = :state, jobAsJson = :json, updatedAt = sysdate() " +
            "UPSERT RETURN AFTER @rid WHERE id = :id",
            Map.of("id", id, "state", state, "json", json));
        log.debug("JobRunr: saved job {} state={}", id, state);
        return job;
    }

    @Override
    public List<Job> save(List<Job> jobs) throws ConcurrentJobModificationException {
        for (Job job : jobs) save(job);
        return jobs;
    }

    @Override
    public Job getJobById(UUID id) throws JobNotFoundException {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE id = :id",
            Map.of("id", id.toString()));
        if (rows == null || rows.isEmpty()) throw new JobNotFoundException(id);
        return jobMapper.deserializeJob((String) rows.get(0).get("jobAsJson"));
    }

    @Override
    public long countJobs(StateName state) {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT count(*) as cnt FROM `jobrunr_jobs` WHERE state = :state",
            Map.of("state", state.name()));
        if (rows == null || rows.isEmpty()) return 0L;
        Object cnt = rows.get(0).get("cnt");
        return cnt instanceof Number ? ((Number) cnt).longValue() : 0L;
    }

    @Override
    public List<Job> getJobList(StateName state, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = :state LIMIT :limit",
            Map.of("state", state.name(), "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public List<Job> getJobList(StateName state, Instant updatedBefore, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :before LIMIT :limit",
            Map.of("state", state.name(), "before", updatedBefore.toString(),
                   "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public List<Job> getScheduledJobs(Instant scheduledBefore, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = 'SCHEDULED' AND scheduledAt < :before LIMIT :limit",
            Map.of("before", scheduledBefore.toString(), "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public int deletePermanently(UUID id) {
        List<Map<String, Object>> r = frigg.sql(
            "DELETE FROM `jobrunr_jobs` WHERE id = :id RETURN BEFORE @rid",
            Map.of("id", id.toString()));
        return r != null ? r.size() : 0;
    }

    @Override
    public int deleteJobsPermanently(StateName state, Instant updatedBefore) {
        List<Map<String, Object>> r = frigg.sql(
            "DELETE FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :before RETURN BEFORE @rid",
            Map.of("state", state.name(), "before", updatedBefore.toString()));
        return r != null ? r.size() : 0;
    }

    @Override
    public Set<String> getDistinctJobSignatures(StateName... states) {
        return Set.of(); // stub — not critical for basic job flow
    }

    @Override
    public boolean recurringJobExists(String recurringJobId, StateName... states) {
        return false;
    }

    // ─── Recurring jobs (stub — not used in C.2) ──────────────────────────────

    @Override
    public RecurringJob saveRecurringJob(RecurringJob recurringJob) {
        return recurringJob;
    }

    @Override
    public RecurringJobsResult getRecurringJobs() {
        return new RecurringJobsResult(List.of());
    }

    @Override
    public boolean recurringJobsUpdated(Long updatedAt) {
        return false;
    }

    @Override
    public int deleteRecurringJob(String id) {
        return 0;
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    @Override
    public JobStats getJobStats() {
        long scheduled  = countJobs(StateName.SCHEDULED);
        long enqueued   = countJobs(StateName.ENQUEUED);
        long processing = countJobs(StateName.PROCESSING);
        long failed     = countJobs(StateName.FAILED);
        long succeeded  = countJobs(StateName.SUCCEEDED);
        long total      = scheduled + enqueued + processing + failed + succeeded;
        return new JobStats(Instant.now(), 0L, total, scheduled, enqueued,
                processing, failed, succeeded, 0L,
                getBackgroundJobServers().size(), 0);
    }

    @Override
    public void publishTotalAmountOfSucceededJobs(int amount) {
        // no-op for ArcadeDB provider
    }

    // ─── Metadata ─────────────────────────────────────────────────────────────

    @Override
    public void saveMetadata(JobRunrMetadata metadata) {
        // stub — metadata used for cluster lock; not critical for single-node
    }

    @Override
    public List<JobRunrMetadata> getMetadata(String name) {
        return List.of();
    }

    @Override
    public JobRunrMetadata getMetadata(String name, String owner) {
        return null;
    }

    @Override
    public void deleteMetadata(String name) {
        // stub
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<Job> deserializeJobs(List<Map<String, Object>> rows) {
        if (rows == null) return List.of();
        return rows.stream()
                .map(r -> jobMapper.deserializeJob((String) r.get("jobAsJson")))
                .collect(Collectors.toList());
    }
}
