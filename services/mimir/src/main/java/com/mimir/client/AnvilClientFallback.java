package com.mimir.client;

import com.mimir.model.anvil.ImpactResult;
import com.mimir.model.anvil.LineageResult;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Fallback handlers for {@link AnvilClient}.
 *
 * <p>SmallRye Fault Tolerance requires fallback handler's generic type to match the
 * method's return type exactly — wildcard {@code <Object>} is invalid. Hence two
 * separate inner classes: one per ANVIL endpoint return type.
 *
 * <p>Both fallbacks return empty result with {@code executionMs = -1} as failure marker.
 * The LLM tool wrapper in {@link com.mimir.tools.AnvilTools} surfaces this as graceful
 * "no data" rather than throwing — keeping MIMIR responsive when ANVIL is down.
 */
public final class AnvilClientFallback {

    private static final Logger LOG = Logger.getLogger(AnvilClientFallback.class);

    private AnvilClientFallback() {}

    /** Fallback for {@code AnvilClient.impact()}. */
    @ApplicationScoped
    public static class Impact implements FallbackHandler<ImpactResult> {
        @Override
        public ImpactResult handle(ExecutionContext context) {
            LOG.warnf("AnvilClient.impact fallback triggered (timeout/retry exhausted) — returning empty");
            return new ImpactResult(null, List.of(), List.of(), 0, false, false, -1L);
        }
    }

    /** Fallback for {@code AnvilClient.lineage()}. */
    @ApplicationScoped
    public static class Lineage implements FallbackHandler<LineageResult> {
        @Override
        public LineageResult handle(ExecutionContext context) {
            LOG.warnf("AnvilClient.lineage fallback triggered — returning empty");
            return new LineageResult(List.of(), List.of(), -1L);
        }
    }
}
