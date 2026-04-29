package studio.seer.dali.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import studio.seer.shared.ParseSessionInput;

/**
 * DMT-ASM-FIX: JobRequest wrapper for {@link ParseJob}.
 *
 * <p>Replaces the IoC lambda {@code j -> j.execute(sessionId, input)} that was
 * previously passed to {@link org.jobrunr.scheduling.JobScheduler#enqueue}.
 * JobRunr's ASM bytecode analyser fails on that lambda inside
 * {@link studio.seer.dali.service.SessionService} because Quarkus's CDI
 * transformation shifts local variable slots and the captured variable ends
 * up at a slot the virtual-stack tracker does not expect ("Can not find
 * variable 4 in stack").
 *
 * <p>{@link org.jobrunr.scheduling.JobRequestScheduler#enqueue(JobRequest)}
 * bypasses the ASM analyser entirely: JobRunr serialises the {@code JobRequest}
 * object itself as the single parameter and calls
 * {@link ParseJob#run(ParseJobRequest)} on the worker side.
 */
public record ParseJobRequest(
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("input")     ParseSessionInput input
) implements JobRequest {

    @JsonCreator
    public ParseJobRequest(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("input")     ParseSessionInput input) {
        this.sessionId = sessionId;
        this.input     = input;
    }

    @Override
    public Class<? extends JobRequestHandler<?>> getJobRequestHandler() {
        return ParseJob.class;
    }
}
