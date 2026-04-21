package studio.seer.lineage.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.lineage.client.anvil.AnvilClient;
import studio.seer.lineage.client.anvil.model.*;
import studio.seer.lineage.client.dali.DaliClient;
import studio.seer.lineage.client.dali.model.CancelResponse;
import studio.seer.lineage.client.dali.model.DaliParseSessionInput;
import studio.seer.lineage.client.dali.model.SessionInfo;
import studio.seer.lineage.client.mimir.MimirClient;
import studio.seer.lineage.client.mimir.model.AskInput;
import studio.seer.lineage.client.mimir.model.MimirAnswer;
import studio.seer.lineage.heimdall.HeimdallControlClient;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.heimdall.model.ParseSessionInput;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.lineage.storage.ViewRepository;

import java.util.List;
import java.util.Map;

/**
 * GraphQL Mutations for SHUTTLE.
 *
 * C.2.2 scope (real implementations):
 *  - resetDemoState    — HEIMDALL /control/reset                        ✓ M1
 *  - startParseSession — DaliClient.createSession() → Dali REST         ✓ C.2.4
 *  - cancelSession     — DaliClient.cancelSession() → Dali REST         ✓ C.2.4
 *  - askMimir          — MimirClient.ask() → MIMIR REST                 ✓ SC-02
 *  - findImpact        — AnvilClient.findImpact() → ANVIL REST          ✓ SC-03
 *  - executeQuery      — AnvilClient.executeQuery() → ANVIL REST        ✓ SC-03
 *  - saveView          — YGG ArcadeDB ShuttleView vertex (C.4.1)        ✓
 *  - deleteView        — YGG ArcadeDB ShuttleView vertex (C.4.1)        ✓
 */
@GraphQLApi
public class MutationResource {

    @Inject @RestClient HeimdallControlClient controlClient;
    @Inject @RestClient DaliClient            daliClient;
    @Inject @RestClient MimirClient           mimirClient;
    @Inject @RestClient AnvilClient           anvilClient;
    @Inject             HeimdallEmitter       heimdall;
    @Inject             ViewRepository        viewRepository;
    @Inject             SeerIdentity          identity;
    @Inject             ObjectMapper          objectMapper;

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

    // ── askMimir (SC-02) ──────────────────────────────────────────────────────

    @Mutation("askMimir")
    @Description("Query MIMIR AI assistant for lineage questions. Role: editor+")
    public Uni<MimirAnswer> askMimir(
            @Name("question") String question,
            @Name("sessionId") String sessionId,
            @Name("maxToolCalls") @DefaultValue("5") int maxToolCalls) {

        return Uni.createFrom().item(() ->
            mimirClient.ask(
                    identity.tenantAlias(),
                    new AskInput(question, sessionId, "hound_default", maxToolCalls))
        ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
         .onFailure().recoverWithItem(ex -> {
             Log.warnf("[SHUTTLE] MIMIR unavailable (askMimir): %s", ex.getMessage());
             return new MimirAnswer(
                     "MIMIR unavailable — please try again later.",
                     List.of(), List.of(), "unavailable", 0L);
         });
    }

    // ── findImpact (SC-03) ────────────────────────────────────────────────────

    @Mutation("findImpact")
    @Description("Find downstream/upstream impact of a data node via ANVIL. Role: editor+")
    public Uni<ImpactResult> findImpact(
            @Name("nodeId") String nodeId,
            @Name("direction") @DefaultValue("downstream") String direction,
            @Name("maxHops") @DefaultValue("5") int maxHops) {

        return Uni.createFrom().item(() ->
            anvilClient.findImpact(
                    identity.tenantAlias(),
                    new ImpactRequest(nodeId, direction, "hound_default", maxHops, List.of()))
        ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
         .onFailure().recoverWithItem(ex -> {
             Log.warnf("[SHUTTLE] ANVIL unavailable (findImpact): %s", ex.getMessage());
             return new ImpactResult(null, List.of(), List.of(), 0, false, false, 0L);
         });
    }

    // ── executeQuery (SC-03) ──────────────────────────────────────────────────

    @Mutation("executeQuery")
    @Description("Execute Cypher or SQL query against YGG lineage graph via ANVIL. Role: editor+")
    public Uni<QueryResult> executeQuery(
            @Name("query") String query,
            @Name("language") @DefaultValue("cypher") String language,
            @Name("dbName") @DefaultValue("hound_default") String dbName) {

        return Uni.createFrom().item(() -> {
            AnvilQueryResponse r = anvilClient.executeQuery(
                    identity.tenantAlias(),
                    new QueryRequest(language, query, dbName));
            return new QueryResult(r.language(), toJson(r.rows()), r.totalRows(),
                    r.hasMore(), r.executionMs(), r.queryId());
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
         .onFailure().recoverWithItem(ex -> {
             Log.warnf("[SHUTTLE] ANVIL unavailable (executeQuery): %s", ex.getMessage());
             return new QueryResult(language, "[]", 0, false, 0L, null);
         });
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

    // ── helpers ───────────────────────────────────────────────────────────────

    private String toJson(List<?> list) {
        if (list == null) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            Log.debugf("toJson serialization error: %s", e.getMessage());
            return "[]";
        }
    }
}
