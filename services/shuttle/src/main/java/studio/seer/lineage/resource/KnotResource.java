package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import studio.seer.lineage.model.*;
import studio.seer.lineage.service.KnotService;

import java.util.List;

/**
 * GraphQL API for KNOT — Hound session analytics report.
 *
 * knotSessions  → sidebar list of all parsed sessions
 * knotReport    → full report for one session (summary + tables + statements)
 */
@GraphQLApi
public class KnotResource {

    @Inject
    KnotService knotService;

    @Query("knotSessions")
    @Description("KNOT — list of all parsed Hound sessions (for sidebar). Role: viewer+")
    public Uni<List<KnotSession>> knotSessions() {
        return knotService.knotSessions();
    }

    @Query("knotReport")
    @Description("KNOT — full report for one session: summary + tables + statements. Role: viewer+")
    public Uni<KnotReport> knotReport(
        @Name("sessionId")
        @Description("session_id property of the DaliSession vertex")
        String sessionId
    ) {
        return knotService.knotReport(sessionId);
    }
}
