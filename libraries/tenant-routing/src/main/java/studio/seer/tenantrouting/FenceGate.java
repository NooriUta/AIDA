package studio.seer.tenantrouting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MTN-50 / MTN-51 — Consumer-side fence token gate.
 *
 * <p>Tracks the highest fence token seen per tenant, compares each incoming
 * {@code seer.control.*} event against it, and reports the applicable action:
 *
 * <ul>
 *   <li>{@link Decision#APPLY}  — token is strictly newer than last seen;
 *       the gate advances lastSeen to {@code received}.</li>
 *   <li>{@link Decision#REPLAY} — token equals lastSeen; caller SHOULD skip
 *       but can log at debug. Happens on retry fan-out.</li>
 *   <li>{@link Decision#STALE}  — token is older than lastSeen. Classic
 *       split-brain signal from a crashed/demoted leader. Caller MUST drop +
 *       bump the {@code stale_fence_token_rejected} metric.</li>
 *   <li>{@link Decision#LEGACY} — token is 0, meaning the emitter pre-dates
 *       MTN-50. Caller MUST apply unconditionally.</li>
 * </ul>
 *
 * <p>State is process-local: after a JVM restart, the first event per tenant
 * re-seeds lastSeen. MTN-51 reconcile (pull-based replay from
 * {@code heimdall.ControlEvent} vertex by createdAt order) is the defence
 * against the restart-amnesia window.
 *
 * <p>This class is not a CDI bean — consumers inject it per-scope to avoid
 * accidental cross-service state sharing.
 */
public final class FenceGate {

    private static final Logger log = LoggerFactory.getLogger(FenceGate.class);

    public enum Decision { APPLY, REPLAY, STALE, LEGACY }

    private final ConcurrentHashMap<String, AtomicLong> lastSeen = new ConcurrentHashMap<>();

    /**
     * Evaluate an incoming control event. Mutates {@code lastSeen[tenantAlias]}
     * atomically only on {@link Decision#APPLY}.
     */
    public Decision admit(String tenantAlias, long receivedToken) {
        if (tenantAlias == null || tenantAlias.isBlank()) {
            throw new IllegalArgumentException("tenantAlias required");
        }
        if (receivedToken == 0L) {
            return Decision.LEGACY;
        }
        if (receivedToken < 0L) {
            throw new IllegalArgumentException("fenceToken must be >= 0, got " + receivedToken);
        }

        AtomicLong current = lastSeen.computeIfAbsent(tenantAlias, k -> new AtomicLong(0L));
        while (true) {
            long prev = current.get();
            if (receivedToken < prev) {
                log.warn("stale fence token for tenant='{}' (received={}, lastSeen={})",
                        tenantAlias, receivedToken, prev);
                return Decision.STALE;
            }
            if (receivedToken == prev) {
                return Decision.REPLAY;
            }
            if (current.compareAndSet(prev, receivedToken)) {
                return Decision.APPLY;
            }
            // lost CAS race; re-read and compare again
        }
    }

    /**
     * Convenience overload that reads the fence token from {@code payload}.
     * See {@link studio.seer.shared.ControlEventFence#read}.
     *
     * <p>Kept in the {@code tenant-routing} module as a light wrapper to avoid
     * a compile-time dependency on {@code shared/dali-models} for every
     * consumer — callers who want structural access can bring the util in
     * themselves.
     */
    public Decision admit(String tenantAlias, Map<String, Object> payload) {
        long token = extractFenceToken(payload);
        return admit(tenantAlias, token);
    }

    /** @return last-seen token for {@code tenantAlias}, or 0 if never seen. */
    public long lastSeen(String tenantAlias) {
        AtomicLong a = lastSeen.get(tenantAlias);
        return a == null ? 0L : a.get();
    }

    /** Reset state for {@code tenantAlias}. Used by tests + reconnect flow. */
    public void reset(String tenantAlias) {
        lastSeen.remove(tenantAlias);
    }

    /** Reset all state — used only by tests. */
    public void resetAll() {
        lastSeen.clear();
    }

    private static long extractFenceToken(Map<String, Object> payload) {
        if (payload == null) return 0L;
        Object raw = payload.get("_fenceToken");
        if (raw == null)              return 0L;
        if (raw instanceof Long l)    return l;
        if (raw instanceof Integer i) return i;
        if (raw instanceof Number n)  return n.longValue();
        if (raw instanceof String s)  {
            try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return 0L; }
        }
        return 0L;
    }
}
