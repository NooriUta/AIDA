package studio.seer.dali.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
import java.util.concurrent.ConcurrentHashMap;
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
 */
@Singleton
public class ArcadeDbStorageProvider extends AbstractStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(ArcadeDbStorageProvider.class);

    private final FriggGateway  frigg;
    private final JacksonJsonMapper jsonMapper; // for BackgroundJobServerStatus
    private       JobMapper     jobMapper;       // for Job objects — set by JobRunr

    // In-memory metadata store — single-node, no clustering needed.
    // Stores JobRunrMetadata by composite key "name/owner".
    // Critical: JobRunr uses "database_version/cluster" to check if data version >= 6.0.0.
    private final ConcurrentHashMap<String, JobRunrMetadata> metadataStore = new ConcurrentHashMap<>();

    @Inject
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
                   "hb",   status.getLastHeartbeat().toEpochMilli()));
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
        if (id == null) return null;
        try {
            return UUID.fromString(id.toString());
        } catch (IllegalArgumentException e) {
            // Corrupt/test record — purge it and retry
            log.warn("getLongestRunningBackgroundJobServerId: removing invalid record id='{}': {}", id, e.getMessage());
            frigg.sql("DELETE FROM `jobrunr_servers` WHERE id = :id", Map.of("id", id.toString()));
            return getLongestRunningBackgroundJobServerId();
        }
    }

    @Override
    public int removeTimedOutBackgroundJobServers(Instant heartbeatOlderThan) {
        // Count then delete — ArcadeDB does not support RETURN BEFORE in DELETE.
        // Compare as epoch-millisecond LONG to avoid DATETIME string parsing issues.
        long cutoff = heartbeatOlderThan.toEpochMilli();
        List<Map<String, Object>> cnt = frigg.sql(
            "SELECT count(*) as cnt FROM `jobrunr_servers` WHERE lastHeartbeat < :cutoff",
            Map.of("cutoff", cutoff));
        int count = (cnt != null && !cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number)
                ? ((Number) cnt.get(0).get("cnt")).intValue() : 0;
        if (count > 0) {
            frigg.sql("DELETE FROM `jobrunr_servers` WHERE lastHeartbeat < :cutoff",
                    Map.of("cutoff", cutoff));
        }
        return count;
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
        // BUG-SS-013 (rev): AmountRequest in JobRunr 7.3.0 exposes getLimit() only — no getOffset().
        // ArcadeDB SQL SKIP with named params is unsupported; pagination is handled by JobRunr internally.
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = :state LIMIT :limit",
            Map.of("state", state.name(), "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public List<Job> getJobList(StateName state, Instant updatedBefore, AmountRequest amountRequest) {
        // BUG-SS-013 (rev): same — getOffset() does not exist in JobRunr 7.3.0 AmountRequest.
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :cutoff LIMIT :limit",
            Map.of("state", state.name(), "cutoff", updatedBefore.toString(),
                   "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public List<Job> getScheduledJobs(Instant scheduledBefore, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = frigg.sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = 'SCHEDULED' AND scheduledAt < :cutoff LIMIT :limit",
            Map.of("cutoff", scheduledBefore.toString(), "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public int deletePermanently(UUID id) {
        // ArcadeDB does not support RETURN BEFORE in DELETE — count then delete
        List<Map<String, Object>> cnt = frigg.sql(
            "SELECT count(*) as cnt FROM `jobrunr_jobs` WHERE id = :id",
            Map.of("id", id.toString()));
        int count = (cnt != null && !cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number)
                ? ((Number) cnt.get(0).get("cnt")).intValue() : 0;
        if (count > 0) {
            frigg.sql("DELETE FROM `jobrunr_jobs` WHERE id = :id", Map.of("id", id.toString()));
        }
        return count;
    }

    @Override
    public int deleteJobsPermanently(StateName state, Instant updatedBefore) {
        List<Map<String, Object>> cnt = frigg.sql(
            "SELECT count(*) as cnt FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :cutoff",
            Map.of("state", state.name(), "cutoff", updatedBefore.toString()));
        int count = (cnt != null && !cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number)
                ? ((Number) cnt.get(0).get("cnt")).intValue() : 0;
        if (count > 0) {
            frigg.sql("DELETE FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :cutoff",
                    Map.of("state", state.name(), "cutoff", updatedBefore.toString()));
        }
        return count;
    }

    @Override
    public Set<String> getDistinctJobSignatures(StateName... states) {
        return Set.of(); // stub — not critical for basic job flow
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

    @Override
    public Instant getRecurringJobLatestScheduledInstant(String recurringJobId, StateName... states) {
        return null;
    }

    @Override
    public List<Job> getCarbonAwareJobList(Instant scheduledBefore, AmountRequest amountRequest) {
        return List.of();
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
        // 8.5.2: (timestamp, queryDur, total, awaiting, scheduled, enqueued,
        //          processing, failed, succeeded, allTimeSucceeded, recurringJobs, bgServers)
        return new JobStats(Instant.now(), 0L, total, 0L, scheduled, enqueued,
                processing, failed, succeeded, 0L,
                0, getBackgroundJobServers().size());
    }

    @Override
    public void publishTotalAmountOfSucceededJobs(int amount) {
        // no-op for ArcadeDB provider
    }

    // ─── Metadata ─────────────────────────────────────────────────────────────

    @Override
    public void saveMetadata(JobRunrMetadata metadata) {
        String key = metadata.getName() + "/" + metadata.getOwner();
        metadataStore.put(key, metadata);
        log.debug("JobRunr: saveMetadata key={} value={}", key, metadata.getValue());
    }

    @Override
    public List<JobRunrMetadata> getMetadata(String name) {
        return metadataStore.values().stream()
                .filter(m -> name.equals(m.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public JobRunrMetadata getMetadata(String name, String owner) {
        return metadataStore.get(name + "/" + owner);
    }

    @Override
    public void deleteMetadata(String name) {
        metadataStore.entrySet().removeIf(e -> e.getKey().startsWith(name + "/"));
        log.debug("JobRunr: deleteMetadata name={}", name);
    }

    @Override
    public void deleteMetadata(String name, String owner) {
        metadataStore.remove(name + "/" + owner);
        log.debug("JobRunr: deleteMetadata name={} owner={}", name, owner);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<Job> deserializeJobs(List<Map<String, Object>> rows) {
        if (rows == null) return List.of();
        return rows.stream()
                .map(r -> jobMapper.deserializeJob((String) r.get("jobAsJson")))
                .collect(Collectors.toList());
    }
}
