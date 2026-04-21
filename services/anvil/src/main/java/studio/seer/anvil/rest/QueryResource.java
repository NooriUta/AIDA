package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.heimdall.AnvilEventEmitter;
import studio.seer.anvil.model.QueryRequest;
import studio.seer.anvil.model.QueryResult;
import studio.seer.anvil.security.QuerySecurityException;
import studio.seer.anvil.security.QuerySanitizer;
import studio.seer.anvil.service.QueryService;

/**
 * AV-06 — YGG Light IDE query endpoint (ANVIL_SPEC §5).
 *
 * <pre>
 * POST /api/query   — execute Cypher or SQL against YGG; GraphQL → 501
 * </pre>
 */
@Path("/api/query")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryResource {

    @Inject
    QueryService queryService;

    @Inject
    QuerySanitizer sanitizer;

    @Inject
    AnvilEventEmitter events;

    @POST
    public Response executeQuery(QueryRequest req) {
        if (req == null || req.query() == null || req.query().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"query is required\"}")
                    .build();
        }
        String language = req.language() != null ? req.language().toLowerCase() : "cypher";

        if ("graphql".equals(language)) {
            return Response.status(501)
                    .entity("{\"error\":\"GraphQL not implemented — use SHUTTLE /graphql\"}")
                    .build();
        }
        if (!"cypher".equals(language) && !"sql".equals(language)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"language must be cypher | sql | graphql\"}")
                    .build();
        }

        try {
            sanitizer.validate(req.query(), language);
        } catch (QuerySecurityException e) {
            events.queryBlocked(e.getMessage(), language);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }

        try {
            QueryResult result = queryService.execute(language, req.query(), req.dbName());
            events.queryExecuted(language, result.executionMs(), result.totalRows(),
                    req.dbName() != null ? req.dbName() : "hound_default");
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity("{\"error\":\"YGG query failed: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
