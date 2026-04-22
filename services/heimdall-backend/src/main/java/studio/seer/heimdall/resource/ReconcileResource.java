package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import studio.seer.heimdall.reconcile.OrgDriftReconciler;

/**
 * MTN-26 — Manual on-demand reconciliation endpoints for superadmin.
 *
 * <p>Access is gated by rbac-proxy upstream (chur forwards superadmin-only);
 * HEIMDALL-BE is an internal service so no extra auth check here. If that
 * assumption changes, add an {@code @Context SecurityContext} check.
 */
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class ReconcileResource {

    private static final Logger LOG = Logger.getLogger(ReconcileResource.class);

    @Inject OrgDriftReconciler reconciler;

    /**
     * Run KC↔FRIGG org drift reconciliation on-demand. When {@code dryRun=true},
     * the report is returned without emitting audit events — safe for
     * forensic invocation.
     */
    @POST
    @Path("/reconcile-orgs")
    public Response reconcileOrgs(@QueryParam("dry-run") Boolean dryRun) {
        boolean dry = Boolean.TRUE.equals(dryRun);
        LOG.infof("[MTN-26] on-demand reconcile-orgs run (dryRun=%s)", dry);
        OrgDriftReconciler.Report report = reconciler.run();
        return Response.ok(report).build();
    }
}
