package studio.seer.lineage.client.mimir;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import studio.seer.lineage.client.mimir.model.MimirAnswer;

import java.util.List;

/**
 * Fallback for {@link MimirClient} — activated when MIMIR is unreachable or
 * timeout/retry budget is exhausted.
 *
 * <p>Returns a degraded but non-throwing answer so SHUTTLE mutations degrade
 * gracefully instead of propagating a 500 to VERDANDI.
 */
public class MimirClientFallback implements FallbackHandler<Object> {

    @Override
    public Object handle(ExecutionContext ctx) {
        Log.warnf("[MimirClient] Fallback activated for %s: %s",
                ctx.getMethod().getName(),
                ctx.getFailure() != null ? ctx.getFailure().getMessage() : "unknown error");

        return switch (ctx.getMethod().getName()) {
            case "ask" -> new MimirAnswer(
                    "MIMIR unavailable — please try again later.",
                    List.of(),
                    List.of(),
                    "unavailable",
                    0L
            );
            default -> null;
        };
    }
}
