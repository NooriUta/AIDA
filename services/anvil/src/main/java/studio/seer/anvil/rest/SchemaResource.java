package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.service.SchemaService;

/**
 * AV-07 — YGG schema introspection (ANVIL_SPEC §5).
 *
 * <pre>
 * GET /api/schema?dbName=hound_default
 * </pre>
 */
@Path("/api/schema")
@Produces(MediaType.APPLICATION_JSON)
public class SchemaResource {

    @Inject
    SchemaService schemaService;

    @GET
    public Response schema(@QueryParam("dbName") String dbName) {
        return Response.ok(schemaService.getSchema(dbName)).build();
    }
}
