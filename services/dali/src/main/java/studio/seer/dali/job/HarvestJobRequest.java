package studio.seer.dali.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

/**
 * DMT-ASM-FIX: JobRequest wrapper for {@link HarvestJob}.
 *
 * <p>Replaces the IoC lambda {@code j -> j.execute(harvestId, tenantAlias)} that was
 * previously passed to {@link org.jobrunr.scheduling.JobScheduler#enqueue}.
 * See {@link ParseJobRequest} for the root-cause explanation.
 *
 * <p>Enqueued via {@link org.jobrunr.scheduling.JobRequestScheduler} from
 * {@link studio.seer.dali.rest.SessionResource#harvest}.
 */
public record HarvestJobRequest(
        @JsonProperty("harvestId")   String harvestId,
        @JsonProperty("tenantAlias") String tenantAlias
) implements JobRequest {

    @JsonCreator
    public HarvestJobRequest(
            @JsonProperty("harvestId")   String harvestId,
            @JsonProperty("tenantAlias") String tenantAlias) {
        this.harvestId   = harvestId;
        this.tenantAlias = tenantAlias;
    }

    @Override
    public Class<? extends JobRequestHandler<?>> getJobRequestHandler() {
        return HarvestJob.class;
    }
}
