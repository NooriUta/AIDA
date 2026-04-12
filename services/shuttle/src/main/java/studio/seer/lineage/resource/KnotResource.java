package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.model.*;
import studio.seer.lineage.service.KnotService;

import java.util.List;
import java.util.Map;

/**
 * GraphQL API for KNOT — Hound session analytics report.
 *
 * knotSessions  → sidebar list of all parsed sessions
 * knotReport    → full report for one session (summary + tables + statements)
 */
@GraphQLApi
public class KnotResource {

    @Inject KnotService     knotService;
    @Inject HeimdallEmitter heimdall;

    @Query("knotSessions")
    @Description("KNOT — list of all parsed Hound sessions (for sidebar). Role: viewer+")
    public Uni<List<KnotSession>> knotSessions() {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0, Map.of("op", "knotSessions"));
        return knotService.knotSessions()
                .invoke(sessions -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotSessions", "count", sessions != null ? sessions.size() : 0)));
    }

    @Query("knotReport")
    @Description("KNOT — full report for one session: summary + tables + statements. Role: viewer+")
    public Uni<KnotReport> knotReport(
        @Name("sessionId")
        @Description("session_id property of the DaliSession vertex")
        String sessionId
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0, Map.of("op", "knotReport", "sessionId", sessionId != null ? sessionId : ""));
        return knotService.knotReport(sessionId)
                .invoke(__ -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start, Map.of("op", "knotReport")));
    }
}
