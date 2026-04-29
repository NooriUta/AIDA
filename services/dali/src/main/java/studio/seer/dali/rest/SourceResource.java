package studio.seer.dali.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.dali.rest.SourceDTO.SchemaFilter;
import studio.seer.dali.security.JdbcUrlValidator;
import studio.seer.dali.storage.SourceRepository;
import studio.seer.shared.EventType;
import studio.seer.tenantrouting.TenantContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

/**
 * CRUD REST API for JDBC harvest sources.
 *
 * <pre>
 * GET    /api/sources               → list all sources (password omitted)
 * POST   /api/sources               → create source
 * PUT    /api/sources/{id}          → update source (password optional)
 * DELETE /api/sources/{id}          → delete source
 * POST   /api/sources/test          → test JDBC connection
 * </pre>
 */
@RequestScoped
@Path("/api/sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SourceResource {

    private static final Logger log = LoggerFactory.getLogger(SourceResource.class);

    @Inject SourceRepository  repository;
    @Inject JdbcUrlValidator  jdbcUrlValidator;
    @Inject TenantContext     tenantCtx;
    @Inject HeimdallEmitter   emitter;

    @GET
    public Response list() {
        return Response.ok(repository.findAll(tenantCtx.tenantAlias())).build();
    }

    @POST
    public Response create(SourceRequest body) {
        if (body == null || blank(body.name()) || blank(body.dialect()) || blank(body.jdbcUrl())) {
            return bad("name, dialect, and jdbcUrl are required");
        }
        var ssrf = jdbcUrlValidator.validate(body.jdbcUrl());
        if (!ssrf.allowed()) return bad("source_url_rejected: " + ssrf.reason());
        SourceDTO created = repository.create(
            tenantCtx.tenantAlias(), body.name(), body.dialect(), body.jdbcUrl(),
            body.username(), body.password(), body.schemaFilter());
        // EV-06: source created
        emitter.info(EventType.SOURCE_CREATED, null, Map.of(
                "source_id", created.id(),
                "dialect",   created.dialect() != null ? created.dialect() : ""));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, SourceRequest body) {
        if (body == null || blank(body.name()) || blank(body.dialect()) || blank(body.jdbcUrl())) {
            return bad("name, dialect, and jdbcUrl are required");
        }
        var ssrf = jdbcUrlValidator.validate(body.jdbcUrl());
        if (!ssrf.allowed()) return bad("source_url_rejected: " + ssrf.reason());
        var updated = repository.update(tenantCtx.tenantAlias(), id, body.name(), body.dialect(), body.jdbcUrl(),
                body.username(), body.password(), body.schemaFilter());
        if (updated.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"source not found\"}").build();
        }
        return Response.ok(updated.get()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        String alias = tenantCtx.tenantAlias();
        var found = repository.findById(alias, id);
        if (found.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"source not found\"}").build();
        }
        String dialect = found.get().dialect() != null ? found.get().dialect() : "";
        repository.delete(alias, id);
        // EV-06: source deleted
        emitter.info(EventType.SOURCE_DELETED, null, Map.of(
                "source_id", id,
                "dialect",   dialect));
        return Response.noContent().build();
    }

    @POST
    @Path("/test")
    public Response test(TestConnectionRequest req) {
        if (req == null || blank(req.jdbcUrl())) return bad("jdbcUrl is required");
        var ssrf = jdbcUrlValidator.validate(req.jdbcUrl());
        if (!ssrf.allowed()) return bad("source_url_rejected: " + ssrf.reason());
        long start = System.currentTimeMillis();
        try {
            try (Connection conn = DriverManager.getConnection(
                    req.jdbcUrl(), req.username(), req.password())) {
                conn.createStatement().execute("SELECT 1");
            }
            long latency = System.currentTimeMillis() - start;
            return Response.ok(Map.of("ok", true, "latencyMs", latency)).build();
        } catch (Exception e) {
            log.debug("Connection test failed for {}: {}", req.jdbcUrl(), e.getMessage());
            return Response.ok(Map.of("ok", false, "error", e.getMessage())).build();
        }
    }

    // ── Request records ──────────────────────────────────────────────────────────

    /** Body for POST /api/sources and PUT /api/sources/{id}. */
    public record SourceRequest(
            String name,
            String dialect,
            String jdbcUrl,
            String username,
            String password,
            SchemaFilter schemaFilter
    ) {}

    /** Body for POST /api/sources/test. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TestConnectionRequest(String jdbcUrl, String username, String password) {}

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    private static Response bad(String msg) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"" + msg + "\"}").build();
    }
}
