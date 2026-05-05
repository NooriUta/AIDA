package studio.seer.dali.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

/**
 * HAL2-05: JobRequest wrapper for {@link RecomputeWorker}.
 *
 * <p>Uses the DMT-ASM-FIX pattern (bypasses bytecode analysis) to schedule
 * async cascade recomputation of atom statuses after PENDING_INJECT resolution.
 */
public record RecomputeJobRequest(
        @JsonProperty("sessionId")   String sessionId,
        @JsonProperty("tenantAlias") String tenantAlias
) implements JobRequest {

    @JsonCreator
    public RecomputeJobRequest(
            @JsonProperty("sessionId")   String sessionId,
            @JsonProperty("tenantAlias") String tenantAlias) {
        this.sessionId   = sessionId;
        this.tenantAlias = tenantAlias;
    }

    @Override
    public Class<? extends JobRequestHandler<?>> getJobRequestHandler() {
        return RecomputeWorker.class;
    }
}
