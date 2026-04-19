package studio.seer.dali.job;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.config.DaliConfig;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.dali.service.SessionService;
import studio.seer.shared.ParseSessionInput;

import java.util.List;

/**
 * JobRunr parent job for the nightly JDBC harvest (DS-03).
 *
 * <p>Reads JDBC sources from {@link DaliConfig#sources()} and creates one
 * {@link ParseJob} subtask per source via {@link SessionService#enqueue}.
 * All subtasks share the same {@code sessionId} prefix for HEIMDALL correlation.
 *
 * <p>Enqueued by {@link studio.seer.dali.job.HarvestScheduler} (DS-06)
 * or via {@code POST /sessions} with {@code trigger=harvest}.
 */
@Unremovable
@ApplicationScoped
public class HarvestJob {

    private static final Logger log = LoggerFactory.getLogger(HarvestJob.class);

    @Inject SessionService   sessionService;
    @Inject HeimdallEmitter  emitter;
    @Inject DaliConfig       config;

    @Job(name = "Harvest %0", retries = 0)
    public void execute(String harvestId) {
        List<DaliConfig.Source> sources = config.sources();
        if (sources.isEmpty()) {
            log.warn("[{}] HarvestJob: no JDBC sources configured — skipping (add dali.sources[*] to application.properties)", harvestId);
            return;
        }

        log.info("[{}] HarvestJob: starting harvest for {} source(s)", harvestId, sources.size());
        emitter.sessionStarted(harvestId, "all", "multi", false, false, sources.size());

        int enqueued = 0;
        for (DaliConfig.Source src : sources) {
            try {
                ParseSessionInput input = new ParseSessionInput(
                        src.dialect(),
                        src.jdbcUrl(),
                        false,          // preview=false
                        enqueued == 0,  // clearBeforeWrite=true for first source only
                        false,
                        src.username(),
                        src.password(),
                        src.schema().orElse(null)
                );
                sessionService.enqueue(input);
                log.info("[{}] HarvestJob: enqueued ParseJob for source '{}' (dialect={})",
                        harvestId, src.name(), src.dialect());
                enqueued++;
            } catch (Exception e) {
                log.error("[{}] HarvestJob: failed to enqueue source '{}': {}", harvestId, src.name(), e.getMessage(), e);
                emitter.sessionFailed(harvestId, "Enqueue failed for " + src.name() + ": " + e.getMessage(), 0);
            }
        }

        log.info("[{}] HarvestJob: enqueued {}/{} source jobs", harvestId, enqueued, sources.size());
    }
}
