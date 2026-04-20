package studio.seer.lineage.resource;

import io.quarkus.logging.Log;
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
import studio.seer.lineage.storage.ViewRepository;

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
 *  - saveView          — YGG ArcadeDB ShuttleView vertex (C.4.1)    ✓
 *  - deleteView        — YGG ArcadeDB ShuttleView vertex (C.4.1)    ✓
 */
@GraphQLApi
public class MutationResource {

    @Inject @RestClient HeimdallControlClient controlClient;
    @Inject @RestClient DaliClient            daliClient;
    @Inject             HeimdallEmitter        heimdall;
    @Inject             ViewRepository         viewRepository;

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
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
          .onFailure().recoverWithItem(ex -> {
              Log.warnf("[SHUTTLE] Dali unavailable (startParseSession): %s", ex.getMessage());
              return SessionInfo.unavailable();
          });
    }

    // ── cancelSession ─────────────────────────────────────────────────────────

    @Mutation("cancelSession")
    @Description("Cancel a running or queued parse session. Role: admin")
    public Uni<Boolean> cancelSession(@Name("sessionId") String sessionId) {
        return Uni.createFrom().item(() -> {
            CancelResponse resp = daliClient.cancelSession(sessionId);
            return !"UNAVAILABLE".equals(resp.status());
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
          .onFailure().recoverWithItem(ex -> {
              Log.warnf("[SHUTTLE] Dali unavailable (cancelSession): %s", ex.getMessage());
              return false;
          });
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

    // ── saveView (C.4.1) ──────────────────────────────────────────────────────

    @Mutation("saveView")
    @Description("Persist a LOOM view configuration to YGG (ShuttleView vertex). Role: editor+")
    public Uni<Boolean> saveView(
            @Name("viewId")  String viewId,
            @Name("payload") String payload) {
        if (viewId == null || viewId.isBlank()) {
            return Uni.createFrom().failure(new GraphQLException("saveView: viewId is required"));
        }
        heimdall.emit(EventType.SESSION_STARTED, EventLevel.INFO, null, null, 0,
                Map.of("mutation", "saveView", "viewId", viewId));
        return viewRepository.save(viewId, payload != null ? payload : "{}", "system")
                .map(ignored -> Boolean.TRUE)
                .onFailure().recoverWithUni(ex -> {
                    Log.errorf(ex, "saveView failed viewId=%s", viewId);
                    return Uni.createFrom().failure(
                            new GraphQLException("saveView failed: " + ex.getMessage()));
                });
    }

    // ── deleteView (C.4.1) ────────────────────────────────────────────────────

    @Mutation("deleteView")
    @Description("Delete a saved LOOM view from YGG. Role: editor+")
    public Uni<Boolean> deleteView(@Name("viewId") String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return Uni.createFrom().failure(new GraphQLException("deleteView: viewId is required"));
        }
        heimdall.emit(EventType.SESSION_STARTED, EventLevel.INFO, null, null, 0,
                Map.of("mutation", "deleteView", "viewId", viewId));
        return viewRepository.delete(viewId)
                .map(ignored -> Boolean.TRUE)
                .onFailure().recoverWithUni(ex -> {
                    Log.errorf(ex, "deleteView failed viewId=%s", viewId);
                    return Uni.createFrom().failure(
                            new GraphQLException("deleteView failed: " + ex.getMessage()));
                });
    }
}
