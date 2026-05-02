package com.mimir.rest;

import com.mimir.memory.MimirMemoryConfiguration;
import com.mimir.model.AskRequest;
import com.mimir.model.MimirAnswer;
import com.mimir.orchestration.MimirOrchestrator;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AskResource {

    private static final Logger LOG = Logger.getLogger(AskResource.class);

    @Inject MimirOrchestrator    orchestrator;
    @Inject MimirMemoryConfiguration memoryConfiguration;

    // ── POST /api/ask ─────────────────────────────────────────────────────────
    // ADR-MIMIR-001: routes via MimirOrchestrator → ModelRouter → model service

    @POST
    @Path("/ask")
    public Response ask(
        @HeaderParam("X-Seer-Tenant-Alias") String tenantAlias,
        @Valid AskRequest request
    ) {
        if (tenantAlias == null || tenantAlias.isBlank()) {
            return Response.status(400)
                .entity(Map.of("error", "X-Seer-Tenant-Alias header required"))
                .build();
        }
        long start = System.currentTimeMillis();
        try {
            MimirAnswer answer = orchestrator.ask(request, request.sessionId(), tenantAlias);
            long durationMs = System.currentTimeMillis() - start;
            return Response.ok(new MimirAnswer(
                answer.answer(),
                answer.toolCallsUsed(),
                answer.highlightNodeIds(),
                answer.confidence(),
                durationMs
            )).build();
        } catch (Exception e) {
            LOG.errorf(e, "mimir ask failed for session %s", request.sessionId());
            return Response.ok(MimirAnswer.unavailable()).build();
        }
    }

    // ── GET /api/sessions/{id} ────────────────────────────────────────────────

    @GET
    @Path("/sessions/{id}")
    public Response getSession(@PathParam("id") String sessionId) {
        // Placeholder — persistence via FRIGG in MIMIR Foundation sprint
        return Response.ok(Map.of(
            "sessionId", sessionId,
            "status",    "active",
            "note",      "FRIGG persistence not yet wired (MIMIR Foundation)"
        )).build();
    }

    // ── DELETE /api/sessions/{id} — clears in-memory chat history ────────────

    @DELETE
    @Path("/sessions/{id}")
    public Response deleteSession(@PathParam("id") String sessionId) {
        memoryConfiguration.evict(sessionId);
        return Response.noContent().build();
    }

    // ── GET /api/health ───────────────────────────────────────────────────────

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        return Response.ok(Map.of(
            "status",  "UP",
            "service", "mimir",
            "models",  java.util.List.of("deepseek", "anthropic", "ollama")
        )).build();
    }
}
