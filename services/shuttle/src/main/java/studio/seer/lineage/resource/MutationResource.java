package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.lineage.heimdall.HeimdallControlClient;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.heimdall.model.ParseSessionInput;

import java.util.Map;
import java.util.UUID;

/**
 * GraphQL Mutations for SHUTTLE.
 *
 * M1 scope (stub implementations):
 *  - resetDemoState    — triggers HEIMDALL ring buffer + metrics reset (admin)
 *  - startParseSession — starts a parse session stub, emits SESSION_STARTED (admin)
 *  - cancelSession     — cancels a running session stub, emits SESSION_FAILED (admin)
 *
 * Real Dali/Hound integration deferred to M2.
 */
@GraphQLApi
public class MutationResource {

    @Inject @RestClient HeimdallControlClient controlClient;
    @Inject             HeimdallEmitter        heimdall;

    // ── resetDemoState ────────────────────────────────────────────────────────

    @Mutation("resetDemoState")
    @Description("Trigger HEIMDALL demo reset: clears ring buffer and metric counters. Role: admin")
    public Uni<Boolean> resetDemoState() {
        return controlClient.reset("admin")
                .map(r -> r.getStatus() < 300)
                .onFailure().recoverWithItem(ex -> {
                    // HEIMDALL unavailable is not a fatal mutation error — return false
                    return false;
                });
    }

    // ── startParseSession ─────────────────────────────────────────────────────

    @Mutation("startParseSession")
    @Description("Start a new parse session (M1 stub — Dali integration in M2). Role: admin")
    public Uni<String> startParseSession(
            @Name("input") @DefaultValue("{}") ParseSessionInput input) {

        String sessionId = UUID.randomUUID().toString();
        heimdall.emit(
                EventType.SESSION_STARTED,
                EventLevel.INFO,
                sessionId,
                null,
                0,
                Map.of(
                        "path",   input.path()   != null ? input.path()   : "",
                        "dbType", input.dbType() != null ? input.dbType() : "unknown",
                        "stub",   true
                )
        );
        return Uni.createFrom().item(sessionId);
    }

    // ── cancelSession ─────────────────────────────────────────────────────────

    @Mutation("cancelSession")
    @Description("Cancel a running session (M1 stub). Role: admin")
    public Uni<Boolean> cancelSession(@Name("sessionId") String sessionId) {
        heimdall.emit(
                EventType.SESSION_FAILED,
                EventLevel.WARN,
                sessionId,
                null,
                0,
                Map.of("reason", "cancelled", "stub", true)
        );
        return Uni.createFrom().item(true);
    }
}
