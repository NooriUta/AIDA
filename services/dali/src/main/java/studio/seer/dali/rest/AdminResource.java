package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.storage.FriggGateway;
import studio.seer.dali.storage.FriggSchemaInitializer;

import java.util.List;
import java.util.Map;

/**
 * Internal admin endpoints — not tenant-scoped.
 * Intended for use by Heimdall provisioning flows.
 */
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    @Inject FriggSchemaInitializer schemaInit;
    @Inject FriggGateway frigg;

    /**
     * Ensures dali_{alias} FRIGG schema exists for the given tenant alias.
     * Idempotent — safe to call multiple times or during tenant provisioning.
     */
    @POST
    @Path("/schema/ensure/{alias}")
    public Response ensureSchema(@PathParam("alias") String alias) {
        if (alias == null || alias.isBlank() || !alias.matches("[a-z][a-z0-9-]{2,30}[a-z0-9]")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid tenant alias\"}")
                    .build();
        }
        log.info("AdminResource: ensuring schema for tenant '{}' (on-demand)", alias);
        schemaInit.ensureSchema(alias);
        return Response.ok("{\"status\":\"ok\",\"tenantAlias\":\"" + alias + "\"}").build();
    }

    /**
     * Recovers orphaned JobRunr jobs stuck in SCHEDULED state with NULL scheduledAt.
     *
     * <p>Background: prior to fix(dali) — persist scheduledAt in JobRunr UPSERT,
     * the {@code save()} method dropped the {@code scheduledAt} column. SCHEDULED
     * jobs without scheduledAt are invisible to the JobRunr poll
     * ({@code WHERE state='SCHEDULED' AND scheduledAt &lt; :cutoff}) and stay
     * stuck forever. Their JSON also has the wrong terminal state, so a manual
     * UPDATE state=ENQUEUED leads to {@code IllegalJobStateChangeException} on
     * pickup. The only clean recovery is to DELETE the poisoned rows; user
     * re-triggers parse sessions and JobRunr creates fresh jobs.
     *
     * <p>Returns: {@code {"deleted": N, "tenantSessionsCleaned": [{db, count}, ...]}}.
     */
    @POST
    @Path("/jobrunr/recover")
    public Response recoverOrphanedJobs(
            @HeaderParam("X-Seer-Role") String role,
            @QueryParam("alsoCleanSessions") @DefaultValue("true") boolean alsoCleanSessions) {
        if (!"admin".equalsIgnoreCase(role) && !"superadmin".equalsIgnoreCase(role)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"admin role required\"}").build();
        }

        // 1. Count + delete poisoned jobs (jobrunr_jobs in shared `dali` DB).
        long jobsDeleted = 0;
        try {
            List<Map<String, Object>> cnt = frigg.sql(
                    "SELECT count(*) as cnt FROM jobrunr_jobs WHERE state = 'SCHEDULED' AND scheduledAt IS NULL");
            if (!cnt.isEmpty() && cnt.get(0).get("cnt") instanceof Number n) {
                jobsDeleted = n.longValue();
            }
            if (jobsDeleted > 0) {
                frigg.sql("DELETE FROM jobrunr_jobs WHERE state = 'SCHEDULED' AND scheduledAt IS NULL");
                log.warn("AdminResource: jobrunr/recover deleted {} orphaned SCHEDULED jobs (NULL scheduledAt)", jobsDeleted);
            }
        } catch (Exception e) {
            log.error("AdminResource: jobrunr/recover failed during DELETE jobrunr_jobs: {}", e.getMessage(), e);
            return Response.serverError()
                    .entity("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}").build();
        }

        // 2. Optional: also clean up orphaned QUEUED dali_sessions across all tenant DBs
        //    (these references jobs that no longer exist).
        StringBuilder sessionReport = new StringBuilder("[");
        long totalSessions = 0;
        if (alsoCleanSessions) {
            try {
                List<Map<String, Object>> tenants = frigg.sqlIn("frigg-tenants",
                        "SELECT tenantAlias FROM DaliTenantConfig WHERE status IN ['ACTIVE', 'PROVISIONING', 'SUSPENDED']");
                boolean first = true;
                for (Map<String, Object> t : tenants) {
                    String alias = String.valueOf(t.get("tenantAlias"));
                    String tenantDb = frigg.tenantDb(alias);
                    try {
                        List<Map<String, Object>> sCnt = frigg.sqlIn(tenantDb,
                                "SELECT count(*) as cnt FROM dali_sessions WHERE status = 'QUEUED'");
                        long count = (sCnt.isEmpty() || !(sCnt.get(0).get("cnt") instanceof Number)) ? 0
                                : ((Number) sCnt.get(0).get("cnt")).longValue();
                        if (count > 0) {
                            frigg.sqlIn(tenantDb, "DELETE FROM dali_sessions WHERE status = 'QUEUED'");
                            totalSessions += count;
                        }
                        if (!first) sessionReport.append(",");
                        sessionReport.append("{\"db\":\"").append(tenantDb).append("\",\"count\":").append(count).append("}");
                        first = false;
                    } catch (Exception inner) {
                        log.warn("AdminResource: jobrunr/recover skipped {} — {}", tenantDb, inner.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("AdminResource: jobrunr/recover could not enumerate tenants: {}", e.getMessage());
            }
        }
        sessionReport.append("]");

        log.info("AdminResource: jobrunr/recover OK — jobsDeleted={} sessionsDeleted={}", jobsDeleted, totalSessions);
        return Response.ok("{\"jobsDeleted\":" + jobsDeleted
                + ",\"sessionsDeleted\":" + totalSessions
                + ",\"perTenantSessions\":" + sessionReport.toString() + "}").build();
    }
}
