package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.lineage.client.dali.DaliClient;
import studio.seer.lineage.client.dali.model.CancelResponse;
import studio.seer.lineage.client.dali.model.DaliParseSessionInput;
import studio.seer.lineage.client.dali.model.SessionInfo;
import studio.seer.lineage.heimdall.HeimdallControlClient;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.heimdall.model.ParseSessionInput;

import java.util.Map;

/**
 * GraphQL Mutations for SHUTTLE.
 *
 * C.2.2 scope (real implementations):
 *  - resetDemoState    — HEIMDALL /control/reset                        ✓ M1
 *  - startParseSession — DaliClient.createSession() → Dali REST         ✓ C.2.4
 *  - cancelSession     — DaliClient.cancelSession() → Dali REST         ✓ C.2.4
 *
 * Stubs pending further specs:
 *  - askMimir          — MimirClient (C.2.3 pending)
 *  - saveView          — FRIGG ArcadeDB view storage (pending)
 *  - deleteView        — FRIGG ArcadeDB view storage (pending)
 */
@GraphQLApi
public class MutationResource {

    @Inject @RestClient HeimdallControlClient controlClient;
    @Inject @RestClient DaliClient            daliClient;
    @Inject             HeimdallEmitter        heimdall;

    // ── resetDemoState ────────────────────────────────────────────────────────

    @Mutation("resetDemoState")
    @Description("Trigger HEIMDALL demo reset: clears ring buffer and metric counters. Role: admin")
    public Uni<Boolean> resetDemoState() {
        return controlClient.reset("admin")
                .map(r -> r.getStatus() < 300)
                .onFailure().recoverWithItem(ex -> false);
    }

    // ── startParseSession ─────────────────────────────────────────────────────

    @Mutation("startParseSession")
    @Description("Start a new Dali parse session. Role: admin")
    public Uni<SessionInfo> startParseSession(
            @Name("input") @DefaultValue("{}") ParseSessionInput input) {

        heimdall.emit(
                EventType.SESSION_STARTED,
                EventLevel.INFO,
                null, null, 0,
                Map.of(
                        "source",  input.source()  != null ? input.source()  : "",
                        "dialect", input.dialect() != null ? input.dialect() : "unknown"
                )
        );

        return Uni.createFrom().item(() -> {
            DaliParseSessionInput daliInput = new DaliParseSessionInput(
                    input.dialect(),
                    input.source(),
                    input.preview()          != null ? input.preview()          : false,
                    input.clearBeforeWrite(),   // null → Dali defaults to true
                    input.filePattern(),        // null → Dali defaults to "*.sql"
                    input.maxFiles()            // null → no limit
            );
            return daliClient.createSession(daliInput);
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    // ── cancelSession ─────────────────────────────────────────────────────────

    @Mutation("cancelSession")
    @Description("Cancel a running or queued parse session. Role: admin")
    public Uni<Boolean> cancelSession(@Name("sessionId") String sessionId) {
        return Uni.createFrom().item(() -> {
            CancelResponse resp = daliClient.cancelSession(sessionId);
            return !"UNAVAILABLE".equals(resp.status());
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    // ── askMimir (stub) ───────────────────────────────────────────────────────

    @Mutation("askMimir")
    @Description("Query Mimir AI assistant (stub — MimirClient C.2.3 pending). Role: editor+")
    public Uni<String> askMimir(@Name("question") String question) {
        heimdall.emit(EventType.SESSION_STARTED, EventLevel.WARN, null, null, 0,
                Map.of("mutation", "askMimir", "stub", true, "question_len", question != null ? question.length() : 0));
        return Uni.createFrom().failure(
                new GraphQLException("askMimir: MimirClient not yet implemented (C.2.3 pending)"));
    }

    // ── saveView (stub) ───────────────────────────────────────────────────────

    @Mutation("saveView")
    @Description("Persist a LOOM view configuration to FRIGG (stub — pending). Role: editor+")
    public Uni<Boolean> saveView(
            @Name("viewId")  String viewId,
            @Name("payload") String payload) {
        heimdall.emit(EventType.SESSION_STARTED, EventLevel.WARN, null, null, 0,
                Map.of("mutation", "saveView", "stub", true, "viewId", viewId != null ? viewId : ""));
        return Uni.createFrom().failure(
                new GraphQLException("saveView: FRIGG view storage not yet implemented"));
    }

    // ── deleteView (stub) ─────────────────────────────────────────────────────

    @Mutation("deleteView")
    @Description("Delete a saved LOOM view from FRIGG (stub — pending). Role: editor+")
    public Uni<Boolean> deleteView(@Name("viewId") String viewId) {
        heimdall.emit(EventType.SESSION_STARTED, EventLevel.WARN, null, null, 0,
                Map.of("mutation", "deleteView", "stub", true, "viewId", viewId != null ? viewId : ""));
        return Uni.createFrom().failure(
                new GraphQLException("deleteView: FRIGG view storage not yet implemented"));
    }
}
