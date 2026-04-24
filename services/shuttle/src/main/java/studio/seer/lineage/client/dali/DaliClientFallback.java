package studio.seer.lineage.client.dali;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import studio.seer.lineage.client.dali.model.*;

import java.util.List;

/**
 * Fallback for {@link DaliClient} — activated when Dali is unreachable or
 * a timeout/retry budget is exhausted.
 *
 * <p>Returns safe stub values so SHUTTLE mutations degrade gracefully
 * instead of propagating a 500 to the VERDANDI UI.
 */
public class DaliClientFallback implements FallbackHandler<Object> {

    @Override
    public Object handle(ExecutionContext ctx) {
        Log.warnf("[DaliClient] Fallback activated for %s: %s",
                ctx.getMethod().getName(),
                ctx.getFailure() != null ? ctx.getFailure().getMessage() : "unknown error");

        return switch (ctx.getMethod().getName()) {
            case "createSession"  -> SessionInfo.unavailable();
            case "getSession"     -> SessionInfo.unavailable();
            case "cancelSession"  -> new CancelResponse("UNAVAILABLE", "Dali service unreachable");
            case "replaySession"  -> SessionInfo.unavailable();
            case "listSessions"   -> List.of();
            case "getStats"       -> DaliStats.empty();
            case "getHealth"      -> DaliHealth.degraded("Dali unreachable");
            case "startHarvest"   -> java.util.Map.of("status", "unavailable");
            default               -> null;
        };
    }
}
