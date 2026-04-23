package studio.seer.heimdall.scheduler;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.*;
import org.jobrunr.storage.navigation.AmountRequest;
import org.jobrunr.utils.resilience.RateLimiter;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ArcadeDB (FRIGG frigg-jobrunr DB) backed {@link StorageProvider} for HEIMDALL's JobRunr.
 *
 * Mirrors Dali's ArcadeDbStorageProvider but targets the shared "frigg-jobrunr" database.
 * In-memory stores for server/metadata state (single HEIMDALL node; no clustering required).
 *
 * Schema created by {@link JobRunrSchemaInitializer} before this provider is used.
 */
@Singleton
public class ArcadeDbSchedulerStorageProvider extends AbstractStorageProvider {

    private static final Logger LOG = Logger.getLogger(ArcadeDbSchedulerStorageProvider.class);

    private final JobRunrFriggGateway frigg;
    private       JobMapper           jobMapper;

    // TCCL captured at CDI construction time — prevents ClassNotFoundException on
    // JobRunr background threads that run with system classloader.
    private final ClassLoader quarkusClassLoader;

    private final ConcurrentHashMap<UUID, BackgroundJobServerStatus> serverStore   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JobRunrMetadata>         metadataStore = new ConcurrentHashMap<>();

    // In-memory recurring job store — HEIMDALL is the sole scheduler node.
    private final ConcurrentHashMap<String, RecurringJob> recurringJobStore = new ConcurrentHashMap<>();

    @Inject
    public ArcadeDbSchedulerStorageProvider(JobRunrFriggGateway frigg) {
        super(RateLimiter.Builder.rateLimit().withoutLimits());
        this.frigg              = frigg;
        this.quarkusClassLoader = Thread.currentThread().getContextClassLoader();
    }

    private List<Map<String, Object>> sql(String query, Map<String, Object> params) {
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        if (saved == quarkusClassLoader) return frigg.sql(query, params);
        try {
            Thread.currentThread().setContextClassLoader(quarkusClassLoader);
            return frigg.sql(query, params);
        } finally {
            Thread.currentThread().setContextClassLoader(saved);
        }
    }

    private List<Map<String, Object>> sql(String query) {
        return sql(query, null);
    }

    // ─── JobRunr lifecycle ────────────────────────────────────────────────────

    @Override
    public void setJobMapper(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    @Override
    public void setUpStorageProvider(StorageProviderUtils.DatabaseOptions options) {
        LOG.infof("ArcadeDbSchedulerStorageProvider: setUpStorageProvider(%s)", options);
    }

    // ─── Background server heartbeat ──────────────────────────────────────────

    @Override
    public void announceBackgroundJobServer(BackgroundJobServerStatus status) {
        serverStore.put(status.getId(), status);
    }

    @Override
    public boolean signalBackgroundJobServerAlive(BackgroundJobServerStatus status) {
        serverStore.put(status.getId(), status);
        return true;
    }

    @Override
    public void signalBackgroundJobServerStopped(BackgroundJobServerStatus status) {
        serverStore.remove(status.getId());
    }

    @Override
    public List<BackgroundJobServerStatus> getBackgroundJobServers() {
        return List.copyOf(serverStore.values());
    }

    @Override
    public UUID getLongestRunningBackgroundJobServerId() {
        return serverStore.values().stream()
                .min(Comparator.comparing(BackgroundJobServerStatus::getFirstHeartbeat))
                .map(BackgroundJobServerStatus::getId)
                .orElse(null);
    }

    @Override
    public int removeTimedOutBackgroundJobServers(Instant heartbeatOlderThan) {
        List<UUID> timedOut = serverStore.values().stream()
                .filter(s -> s.getLastHeartbeat().isBefore(heartbeatOlderThan))
                .map(BackgroundJobServerStatus::getId)
                .collect(Collectors.toList());
        timedOut.forEach(serverStore::remove);
        return timedOut.size();
    }

    // ─── Job CRUD ──────────────────────────────────────────────────────────────

    @Override
    public Job save(Job job) throws ConcurrentJobModificationException {
        String id    = job.getId().toString();
        String state = job.getJobState().getName().name();
        String json  = jobMapper.serializeJob(job);
        sql("UPDATE `jobrunr_jobs` SET id = :id, state = :state, jobAsJson = :json, updatedAt = sysdate() " +
            "UPSERT RETURN AFTER @rid WHERE id = :id",
            Map.of("id", id, "state", state, "json", json));
        notifyJobStatsOnChangeListeners();
        return job;
    }

    @Override
    public List<Job> save(List<Job> jobs) throws ConcurrentJobModificationException {
        for (Job job : jobs) save(job);
        return jobs;
    }

    @Override
    public Job getJobById(UUID id) throws JobNotFoundException {
        List<Map<String, Object>> rows = sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE id = :id",
            Map.of("id", id.toString()));
        if (rows == null || rows.isEmpty()) throw new JobNotFoundException(id);
        return jobMapper.deserializeJob((String) rows.get(0).get("jobAsJson"));
    }

    @Override
    public long countJobs(StateName state) {
        List<Map<String, Object>> rows = sql(
            "SELECT count(*) as cnt FROM `jobrunr_jobs` WHERE state = :state",
            Map.of("state", state.name()));
        if (rows == null || rows.isEmpty()) return 0L;
        Object cnt = rows.get(0).get("cnt");
        return cnt instanceof Number n ? n.longValue() : 0L;
    }

    @Override
    public List<Job> getJobList(StateName state, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = :state LIMIT :limit",
            Map.of("state", state.name(), "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public List<Job> getJobList(StateName state, Instant updatedBefore, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :cutoff LIMIT :limit",
            Map.of("state", state.name(), "cutoff", updatedBefore.toString(),
                   "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public List<Job> getScheduledJobs(Instant scheduledBefore, AmountRequest amountRequest) {
        List<Map<String, Object>> rows = sql(
            "SELECT jobAsJson FROM `jobrunr_jobs` WHERE state = 'SCHEDULED' AND scheduledAt < :cutoff LIMIT :limit",
            Map.of("cutoff", scheduledBefore.toString(), "limit", amountRequest.getLimit()));
        return deserializeJobs(rows);
    }

    @Override
    public int deletePermanently(UUID id) {
        List<Map<String, Object>> cnt = sql(
            "SELECT count(*) as cnt FROM `jobrunr_jobs` WHERE id = :id",
            Map.of("id", id.toString()));
        int count = (cnt != null && !cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number n)
                ? n.intValue() : 0;
        if (count > 0) {
            sql("DELETE FROM `jobrunr_jobs` WHERE id = :id", Map.of("id", id.toString()));
        }
        return count;
    }

    @Override
    public int deleteJobsPermanently(StateName state, Instant updatedBefore) {
        List<Map<String, Object>> cnt = sql(
            "SELECT count(*) as cnt FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :cutoff",
            Map.of("state", state.name(), "cutoff", updatedBefore.toString()));
        int count = (cnt != null && !cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number n)
                ? n.intValue() : 0;
        if (count > 0) {
            sql("DELETE FROM `jobrunr_jobs` WHERE state = :state AND updatedAt < :cutoff",
                Map.of("state", state.name(), "cutoff", updatedBefore.toString()));
        }
        return count;
    }

    @Override
    public Set<String> getDistinctJobSignatures(StateName... states) {
        return Set.of();
    }

    // ─── Recurring jobs — in-memory (HEIMDALL is sole scheduler node) ─────────

    @Override
    public RecurringJob saveRecurringJob(RecurringJob recurringJob) {
        recurringJobStore.put(recurringJob.getId(), recurringJob);
        LOG.debugf("[JR] saveRecurringJob id=%s", recurringJob.getId());
        return recurringJob;
    }

    @Override
    public RecurringJobsResult getRecurringJobs() {
        return new RecurringJobsResult(List.copyOf(recurringJobStore.values()));
    }

    @Override
    public boolean recurringJobsUpdated(Long updatedAt) {
        return true;
    }

    @Override
    public int deleteRecurringJob(String id) {
        return recurringJobStore.remove(id) != null ? 1 : 0;
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
        return new JobStats(Instant.now(), 0L, total, 0L, scheduled, enqueued,
                processing, failed, succeeded, 0L,
                recurringJobStore.size(), serverStore.size());
    }

    @Override
    public void publishTotalAmountOfSucceededJobs(int amount) {}

    // ─── Metadata ─────────────────────────────────────────────────────────────

    @Override
    public void saveMetadata(JobRunrMetadata metadata) {
        metadataStore.put(metadata.getName() + "/" + metadata.getOwner(), metadata);
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
    }

    @Override
    public void deleteMetadata(String name, String owner) {
        metadataStore.remove(name + "/" + owner);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<Job> deserializeJobs(List<Map<String, Object>> rows) {
        if (rows == null) return List.of();
        List<Job> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String json = (String) row.get("jobAsJson");
            if (json == null) continue;
            try {
                result.add(jobMapper.deserializeJob(json));
            } catch (Exception e) {
                LOG.warnf("[JR] skipping undeserializable job record: %s", e.getMessage());
            }
        }
        return result;
    }
}
