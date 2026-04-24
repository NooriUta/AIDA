package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import studio.seer.heimdall.scheduler.HarvestCronRegistry;
import studio.seer.heimdall.scheduler.SchedulerConfig;

/**
 * Internal-only REST endpoint for Chur to manage per-tenant harvest cron scheduling.
 *
 * All methods require the X-Internal-Secret header matching heimdall.scheduler.internal-secret.
 * This endpoint is NOT proxied through SHUTTLE — it's called service-to-service from Chur.
 *
 * Called by Chur at:
 *   - Provisioning step 6 (register-harvest)
 *   - Tenant suspend/archive (unregister-harvest)
 *   - Tenant restore (re-register via register-harvest)
 *   - Schedule archive job (schedule-archive, 30d delay after suspend)
 */
@Path("/api/internal/scheduler")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SchedulerResource {

    private static final Logger LOG = Logger.getLogger(SchedulerResource.class);

    @Inject HarvestCronRegistry cronRegistry;
    @Inject SchedulerConfig     config;

    // ─── Register harvest cron ────────────────────────────────────────────────

    @POST
    @Path("/register-harvest")
    public Response registerHarvest(
            @HeaderParam("X-Internal-Secret") String secret,
            @Valid RegisterHarvestRequest body) {
        checkSecret(secret);
        cronRegistry.registerHarvestJob(body.tenantAlias(), body.cronExpr());
        LOG.infof("[SchedulerResource] registered harvest alias=%s", body.tenantAlias());
        return Response.accepted().build();
    }

    // ─── Unregister harvest cron ──────────────────────────────────────────────

    @POST
    @Path("/unregister-harvest")
    public Response unregisterHarvest(
            @HeaderParam("X-Internal-Secret") String secret,
            @Valid UnregisterHarvestRequest body) {
        checkSecret(secret);
        cronRegistry.unregisterHarvestJob(body.tenantAlias());
        LOG.infof("[SchedulerResource] unregistered harvest alias=%s", body.tenantAlias());
        return Response.accepted().build();
    }

    // ─── Harvest status ───────────────────────────────────────────────────────

    @GET
    @Path("/harvest-status")
    public Response harvestStatus(
            @HeaderParam("X-Internal-Secret") String secret,
            @QueryParam("tenantAlias") String tenantAlias) {
        checkSecret(secret);
        String cron = cronRegistry.registeredCron(tenantAlias);
        boolean active = cron != null;
        return Response.ok(new HarvestStatusResponse(tenantAlias, active, cron)).build();
    }

    // ─── Schedule archive job (one-shot, after delayDays) ─────────────────────

    @POST
    @Path("/schedule-archive")
    public Response scheduleArchive(
            @HeaderParam("X-Internal-Secret") String secret,
            @Valid ScheduleArchiveRequest body) {
        checkSecret(secret);
        // Unregister harvest first so no new harvest jobs fire during archive grace period
        cronRegistry.unregisterHarvestJob(body.tenantAlias());
        // Archive job implementation added in HTA-11 (Track B)
        LOG.infof("[SchedulerResource] schedule-archive alias=%s delayDays=%d (archive job pending HTA-11)",
                body.tenantAlias(), body.delayDays());
        return Response.accepted(new ScheduleArchiveResponse(body.tenantAlias(), body.delayDays())).build();
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    private void checkSecret(String provided) {
        if (!config.internalSecret().equals(provided)) {
            throw new WebApplicationException(Response.status(401).entity(
                new ErrorResponse("unauthorized", "Invalid X-Internal-Secret")).build());
        }
    }

    // ─── Request / response records ───────────────────────────────────────────

    public record RegisterHarvestRequest(
            @NotBlank @Pattern(regexp = "^[a-z][a-z0-9-]{2,30}[a-z0-9]$") String tenantAlias,
            @NotBlank String cronExpr) {}

    public record UnregisterHarvestRequest(
            @NotBlank @Pattern(regexp = "^[a-z][a-z0-9-]{2,30}[a-z0-9]$") String tenantAlias) {}

    public record ScheduleArchiveRequest(
            @NotBlank @Pattern(regexp = "^[a-z][a-z0-9-]{2,30}[a-z0-9]$") String tenantAlias,
            int delayDays) {}

    public record HarvestStatusResponse(String tenantAlias, boolean active, String cronExpr) {}

    public record ScheduleArchiveResponse(String tenantAlias, int delayDays) {}

    public record ErrorResponse(String error, String message) {}
}
