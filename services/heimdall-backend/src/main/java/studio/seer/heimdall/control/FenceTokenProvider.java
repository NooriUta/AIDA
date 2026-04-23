package studio.seer.heimdall.control;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.heimdall.scheduler.JobRunrFriggGateway;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MTN-50 / ADR-MT-004 — Fence token provider for HEIMDALL control-plane events.
 *
 * <p>Each {@code seer.control.*} event stamped by {@link ControlEventEmitter}
 * carries a monotonic {@code _fenceToken} so consumers can reject stale
 * fan-outs from a crashed/demoted leader during failover (see ADR-MT-004).
 *
 * <p>Seed semantics on startup:
 * <ol>
 *   <li>Read {@code MAX(id)} from {@code jobrunr_backgroundjobservers} (an
 *       existing JobRunr table). Its rows are monotonic by insertion, so the
 *       max is a safe lower bound that already embeds any previous-leader
 *       tokens we might have used.</li>
 *   <li>Add a gap of 1000 to guarantee strict increase even if leader promotion
 *       and counter seeding race (<em>better waste 1000 slots than double-
 *       issue one</em>).</li>
 *   <li>Cache the seeded value in an {@link AtomicLong}; {@link #next()}
 *       increments and returns.</li>
 * </ol>
 *
 * <p>On FRIGG failure during startup the provider falls back to
 * {@link System#currentTimeMillis()}. That's a weaker guarantee (clock skew
 * can regress) but keeps the service up; operator logs the warning and should
 * trigger a manual reconnect after recovery.
 */
@ApplicationScoped
public class FenceTokenProvider {

    private static final Logger LOG = Logger.getLogger(FenceTokenProvider.class);

    private static final long SEED_GAP = 1_000L;

    @Inject JobRunrFriggGateway frigg;

    private final AtomicLong counter = new AtomicLong(0L);

    /** Priority 15 — after JobRunrLifecycle (10) so the table exists. */
    void onStart(@Observes @Priority(15) StartupEvent ev) {
        long seed = safeSeed();
        counter.set(seed);
        LOG.infof("[MTN-50] FenceTokenProvider seeded at %d (gap=%d)", seed, SEED_GAP);
    }

    /** Return a fresh monotonic fence token. Thread-safe. */
    public long next() {
        long v = counter.incrementAndGet();
        if (v <= 0) {
            // Extremely unlikely — AtomicLong can overflow past 2^63. At 1M events/sec
            // that takes ~292 000 years. If it happens, log loudly; caller drops the event.
            LOG.errorf("[MTN-50] FenceTokenProvider overflow: counter=%d", v);
            throw new IllegalStateException("fence token counter overflowed");
        }
        return v;
    }

    /** Current counter without incrementing (for audit / diagnostic endpoints). */
    public long current() {
        return counter.get();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private long safeSeed() {
        try {
            List<Map<String, Object>> rows = frigg.sql(
                    "SELECT max(id) as maxId FROM jobrunr_backgroundjobservers",
                    Map.of());
            if (rows != null && !rows.isEmpty()) {
                Object raw = rows.get(0).get("maxId");
                if (raw instanceof Number n) {
                    return Math.abs(n.longValue()) + SEED_GAP;
                }
                if (raw instanceof String s && !s.isBlank()) {
                    try { return Math.abs(Long.parseLong(s)) + SEED_GAP; } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            LOG.warnf("[MTN-50] FRIGG max(id) seed failed: %s — falling back to wall clock", e.getMessage());
        }
        // Fallback: wall clock ms — high enough to never collide with a fresh
        // install's counter = 0. Acceptable for MVP; production should always
        // have a JobRunr row available because HEIMDALL registers itself first.
        return System.currentTimeMillis();
    }

    /** @internal test hook. */
    void setCounterForTests(long v) {
        counter.set(v);
    }
}
