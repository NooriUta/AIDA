package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jobrunr.jobs.states.StateName;
import studio.seer.dali.storage.ArcadeDbStorageProvider;
import studio.seer.dali.storage.FriggGateway;

import java.util.Map;

/**
 * JobRunr job queue stats endpoint — consumed by Heimdall DALI Sessions page.
 *
 * <pre>
 * GET  /api/jobs/stats        — enqueued/processing/failed/succeeded/scheduled counts
 * POST /api/jobs/reset-stuck  — reset all PROCESSING jobs to FAILED (superadmin action)
 * </pre>
 */
@Path("/api/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class JobRunrStatsResource {

    @Inject ArcadeDbStorageProvider storageProvider;
    @Inject FriggGateway            frigg;

    /**
     * Returns live JobRunr queue counters by reading the {@code jobrunr_jobs} table
     * in FRIGG directly via {@link ArcadeDbStorageProvider#countJobs}.
     */
    @GET
    @Path("/stats")
    public Response stats() {
        long enqueued   = storageProvider.countJobs(StateName.ENQUEUED);
        long processing = storageProvider.countJobs(StateName.PROCESSING);
        long failed     = storageProvider.countJobs(StateName.FAILED);
        long succeeded  = storageProvider.countJobs(StateName.SUCCEEDED);
        long scheduled  = storageProvider.countJobs(StateName.SCHEDULED);
        return Response.ok(Map.of(
                "enqueued",   enqueued,
                "processing", processing,
                "failed",     failed,
                "succeeded",  succeeded,
                "scheduled",  scheduled
        )).build();
    }

    /**
     * Resets all stuck PROCESSING jobs to FAILED.
     *
     * <p>When Dali restarts, {@code FriggSchemaInitializer.clearStaleServers()} already
     * handles this at boot time (BUG-SS-STUCK fix). This endpoint is for the rare case
     * where a worker crashes mid-session <em>without</em> a Dali restart — e.g. OOM kill
     * inside a running container — and the operator wants to unblock the queue without
     * restarting the service.
     *
     * <p>Not tenant-scoped — intentionally a superadmin / operator action.
     */
    @POST
    @Path("/reset-stuck")
    @Consumes(MediaType.WILDCARD)
    public Response resetStuck() {
        try {
            frigg.sql("UPDATE `jobrunr_jobs` SET state = 'FAILED' WHERE state = 'PROCESSING'");
            long stillProcessing = storageProvider.countJobs(StateName.PROCESSING);
            long nowFailed       = storageProvider.countJobs(StateName.FAILED);
            return Response.ok(Map.of(
                    "reset",      "ok",
                    "processing", stillProcessing,
                    "failed",     nowFailed
            )).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
