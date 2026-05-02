package com.mimir.rest;

import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.persistence.MimirSession;
import com.mimir.persistence.MimirSessionRepository;
import com.mimir.model.MimirAnswer;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Operator decision endpoint for HiL-paused sessions (TIER2 MT-08).
 *
 * <p>POST {@code /api/sessions/{sessionId}/decision} with
 * {@code {"approve": bool, "comment": "...", "approvalId": "uuid"}}.
 *
 * <p>Auth: same Chur-fronted X-Seer-Role header as
 * {@link AdminLlmConfigResource}. Approver must hold {@code admin} or
 * {@code tenant-admin}; the {@code approvalId} must match what was issued
 * when the session was paused (replay protection).
 *
 * <p>This v0 only persists the decision — full re-execution of the paused
 * question lives behind {@code mimir.hil.resume-execution} (false by default,
 * planned for TIER2.5 once the paused question text is also stored).
 */
@Path("/api/sessions/{sessionId}/decision")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HilResumeResource {

    private static final Logger LOG = Logger.getLogger(HilResumeResource.class);

    @Inject MimirSessionRepository sessions;
    @Inject MimirEventEmitter      emitter;

    public record DecisionRequest(String approvalId, boolean approve, String comment) {}

    @POST
    public Response decide(
            @PathParam("sessionId") String sessionId,
            @HeaderParam("X-Seer-Role") String role,
            @HeaderParam("X-Seer-User-Id") String userId,
            DecisionRequest body) {

        if (body == null) {
            return Response.status(400).entity(Map.of("error", "missing body")).build();
        }
        if (!isApprover(role)) {
            return Response.status(403).entity(Map.of("error", "approver role required")).build();
        }
        Optional<MimirSession> opt = sessions.findById(sessionId);
        if (opt.isEmpty()) {
            return Response.status(404).entity(Map.of("error", "session not found")).build();
        }
        MimirSession s = opt.get();
        if (!"paused".equals(s.status())) {
            return Response.status(409).entity(Map.of(
                    "error", "session is not paused",
                    "status", s.status())).build();
        }
        if (s.pauseState() instanceof Map<?, ?> ps) {
            Object expectedId = ps.get("approvalId");
            if (expectedId == null || !expectedId.toString().equals(body.approvalId())) {
                return Response.status(409).entity(Map.of("error", "approvalId mismatch")).build();
            }
            Object expiresAt = ps.get("expiresAt");
            if (expiresAt != null) {
                try {
                    if (Instant.now().isAfter(Instant.parse(expiresAt.toString()))) {
                        return Response.status(410).entity(Map.of("error", "approval expired")).build();
                    }
                } catch (Exception ignore) { /* fall through — bad timestamp shouldn't block decision */ }
            }
        }

        String decidedBy = userId == null ? "anonymous" : userId;
        emitter.hilDecisionMade(sessionId, body.approve(), decidedBy, body.comment());
        LOG.infof("HiL decision: session=%s approve=%s by=%s", sessionId, body.approve(), decidedBy);

        if (body.approve()) {
            // v0: mark completed; the original question is not re-run automatically.
            sessions.save(MimirSession.completed(sessionId, s.tenantAlias(),
                    java.util.List.of(), java.util.List.of()));
            return Response.accepted(Map.of(
                    "sessionId", sessionId,
                    "status",    "approved",
                    "decidedBy", decidedBy)).build();
        } else {
            sessions.save(MimirSession.failed(sessionId, s.tenantAlias()));
            return Response.ok(MimirAnswer.rejected(body.comment())).build();
        }
    }

    private static boolean isApprover(String role) {
        if (role == null) return false;
        String r = role.toLowerCase();
        return r.equals("admin") || r.equals("superadmin") || r.equals("tenant-admin");
    }
}
