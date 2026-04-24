package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.storage.FriggSchemaInitializer;

/**
 * Internal admin endpoints — not tenant-scoped.
 * Intended for use by Heimdall provisioning flows.
 */
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    @Inject FriggSchemaInitializer schemaInit;

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
}
