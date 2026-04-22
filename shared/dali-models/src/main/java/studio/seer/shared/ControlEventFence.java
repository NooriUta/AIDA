package studio.seer.shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MTN-50 / ADR-MT-004 — Fence token convention for {@code seer.control.*} events.
 *
 * <p>A fence token is a monotonically-increasing {@code long} assigned by the
 * HEIMDALL scheduler leader at emit time and carried inside {@link HeimdallEvent}
 * {@code payload} under the reserved key {@value #FENCE_TOKEN_KEY}. Consumers
 * that apply control events (SHUTTLE / ANVIL / DALI / MIMIR) compare the
 * received token against their in-memory {@code lastSeenFenceToken[tenantAlias]}
 * and reject stale tokens — this defends against split-brain fan-out from a
 * crashed / promoted leader pair.
 *
 * <p>See also {@link SchemaVersion} ({@value SchemaVersion#SCHEMA_VERSION_KEY})
 * — the two reserved keys coexist in the same payload map, both prefixed with
 * underscore to avoid colliding with domain fields.
 */
public final class ControlEventFence {

    /** Reserved payload key carrying the monotonic fence token. */
    public static final String FENCE_TOKEN_KEY = "_fenceToken";

    private ControlEventFence() {
        throw new AssertionError("utility class");
    }

    /**
     * Return a new map derived from {@code payload} with
     * {@value #FENCE_TOKEN_KEY} set to {@code token}. Insertion order is
     * preserved for readability when the map is serialized as JSON.
     */
    public static Map<String, Object> stamp(Map<String, Object> payload, long token) {
        if (token <= 0) {
            throw new IllegalArgumentException(FENCE_TOKEN_KEY + " must be > 0, got " + token);
        }
        Map<String, Object> out = new LinkedHashMap<>(payload == null ? Map.of() : payload);
        out.put(FENCE_TOKEN_KEY, token);
        return out;
    }

    /**
     * Read the fence token from {@code payload}. Returns {@code 0L} when the
     * key is absent — consumers interpret 0 as "no fence, legacy emitter" and
     * skip the fence check (see {@link #isStale}).
     *
     * @throws IllegalArgumentException if the key is present but not a
     *         positive integer — shields consumers from emitter bugs.
     */
    public static long read(Map<String, Object> payload) {
        if (payload == null) return 0L;
        Object raw = payload.get(FENCE_TOKEN_KEY);
        if (raw == null) return 0L;
        if (raw instanceof Long l) {
            requirePositiveToken(l);
            return l;
        }
        if (raw instanceof Integer i) {
            requirePositiveToken(i);
            return i;
        }
        if (raw instanceof Number n) {
            double d = n.doubleValue();
            if (d != Math.floor(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException(FENCE_TOKEN_KEY + " must be an integer, got " + raw);
            }
            long v = n.longValue();
            requirePositiveToken(v);
            return v;
        }
        if (raw instanceof String s) {
            try {
                long v = Long.parseLong(s.trim());
                requirePositiveToken(v);
                return v;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(FENCE_TOKEN_KEY + " must be an integer: " + s);
            }
        }
        throw new IllegalArgumentException(
                FENCE_TOKEN_KEY + " must be an integer, got " + raw.getClass().getSimpleName());
    }

    /**
     * True iff {@code receivedToken} is strictly older than {@code lastSeenToken}.
     * Callers should drop the event + bump {@code stale_fence_token_rejected} metric.
     *
     * <p>An absent fence token ({@code receivedToken == 0}) is <strong>not</strong>
     * considered stale — it means the emitter pre-dates MTN-50. Apply these
     * events unconditionally (legacy fallback).
     */
    public static boolean isStale(long receivedToken, long lastSeenToken) {
        if (receivedToken == 0L) return false;        // legacy emitter, no fence
        return receivedToken < lastSeenToken;
    }

    /** Strip the fence key, returning a copy suitable for handlers that don't care. */
    public static Map<String, Object> without(Map<String, Object> payload) {
        if (payload == null || !payload.containsKey(FENCE_TOKEN_KEY)) {
            return payload == null ? Map.of() : Map.copyOf(payload);
        }
        Map<String, Object> out = new HashMap<>(payload);
        out.remove(FENCE_TOKEN_KEY);
        return out;
    }

    private static void requirePositiveToken(long v) {
        if (v <= 0) {
            throw new IllegalArgumentException(FENCE_TOKEN_KEY + " must be > 0, got " + v);
        }
    }
}
