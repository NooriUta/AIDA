package studio.seer.lineage.client.anvil;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import studio.seer.lineage.client.anvil.model.ImpactResult;

import java.util.List;

/**
 * Fallback for {@link AnvilClient} — activated when ANVIL is unreachable or
 * timeout/retry budget is exhausted.
 */
public class AnvilClientFallback implements FallbackHandler<Object> {

    @Override
    public Object handle(ExecutionContext ctx) {
        Log.warnf("[AnvilClient] Fallback activated for %s: %s",
                ctx.getMethod().getName(),
                ctx.getFailure() != null ? ctx.getFailure().getMessage() : "unknown error");

        return switch (ctx.getMethod().getName()) {
            case "findImpact" -> new ImpactResult(null, List.of(), List.of(), 0, false, false, 0L);
            default -> null;
        };
    }
}
