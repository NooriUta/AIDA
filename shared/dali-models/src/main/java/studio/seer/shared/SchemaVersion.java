package studio.seer.shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MTN-52 — Schema-versioning convention for {@link HeimdallEvent} payloads.
 *
 * <p>Design decision (ADR-MT-005): keep the existing {@code HeimdallEvent}
 * record shape unchanged; carry the schema version inside {@code payload}
 * under the reserved key {@value #SCHEMA_VERSION_KEY}. No new wire type, no
 * producer/consumer refactor storm — only emitters of evolving payloads opt
 * in.
 *
 * <p>Consumer contract:
 * <ul>
 *   <li>Missing key → implicit version 1 (legacy / pre-MTN-52 emitters);</li>
 *   <li>{@code payload.get(_schemaVersion)} present → read as integer,
 *       dispatch to handler supporting that version;</li>
 *   <li>Consumers MUST support the current version {@link #CURRENT} and
 *       {@link #PREVIOUS} (N and N−1) for the deprecation window
 *       {@value #DEPRECATION_WINDOW_DESCRIPTION}.</li>
 * </ul>
 *
 * <p>Producers that want explicit versioning wrap their payload via
 * {@link #withSchemaVersion(Map, int)}. The key is intentionally prefixed
 * with an underscore to avoid colliding with domain fields.
 */
public final class SchemaVersion {

    /** Reserved payload key carrying the event schema version. */
    public static final String SCHEMA_VERSION_KEY = "_schemaVersion";

    /** Current version emitted by new code paths. */
    public static final int CURRENT = 1;

    /** Previous version consumers MUST keep supporting during the deprecation window. */
    public static final int PREVIOUS = 1;

    /** Human-readable deprecation window used in ADR / runbooks. */
    public static final String DEPRECATION_WINDOW_DESCRIPTION = "3 months";

    private SchemaVersion() {
        throw new AssertionError("utility class");
    }

    /**
     * Return a new map derived from {@code payload} with
     * {@value #SCHEMA_VERSION_KEY} set to {@code version}. Insertion order is
     * preserved for readability when the map is serialized as JSON.
     */
    public static Map<String, Object> withSchemaVersion(Map<String, Object> payload, int version) {
        requirePositiveVersion(version);
        Map<String, Object> out = new LinkedHashMap<>(payload == null ? Map.of() : payload);
        out.put(SCHEMA_VERSION_KEY, version);
        return out;
    }

    /** Shorthand for {@link #withSchemaVersion} at the current version. */
    public static Map<String, Object> withCurrentSchemaVersion(Map<String, Object> payload) {
        return withSchemaVersion(payload, CURRENT);
    }

    /**
     * Read the schema version from {@code payload}, returning
     * {@code 1} (implicit legacy) when no explicit key is present.
     *
     * @throws IllegalArgumentException if the key is present but not an
     *         integer ≥ 1 — shields consumers from emitter bugs.
     */
    public static int read(Map<String, Object> payload) {
        if (payload == null) return 1;
        Object raw = payload.get(SCHEMA_VERSION_KEY);
        if (raw == null) return 1;
        if (raw instanceof Integer i) {
            requirePositiveVersion(i);
            return i;
        }
        if (raw instanceof Long || raw instanceof Short || raw instanceof Byte) {
            int v = ((Number) raw).intValue();
            requirePositiveVersion(v);
            return v;
        }
        if (raw instanceof Number n) {
            double d = n.doubleValue();
            if (d != Math.floor(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException(SCHEMA_VERSION_KEY + " must be an integer, got " + raw);
            }
            int v = n.intValue();
            requirePositiveVersion(v);
            return v;
        }
        if (raw instanceof String s) {
            try {
                int v = Integer.parseInt(s.trim());
                requirePositiveVersion(v);
                return v;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(SCHEMA_VERSION_KEY + " must be an integer: " + s);
            }
        }
        throw new IllegalArgumentException(
                SCHEMA_VERSION_KEY + " must be an integer, got " + raw.getClass().getSimpleName());
    }

    /**
     * True when the consumer, currently supporting {@link #CURRENT} and
     * {@link #PREVIOUS}, can safely handle the version read from {@code payload}.
     */
    public static boolean isSupported(Map<String, Object> payload) {
        int v = read(payload);
        return v == CURRENT || v == PREVIOUS;
    }

    /**
     * True when the version read from {@code payload} is older than
     * {@link #PREVIOUS} — i.e. outside the deprecation window. Producers still
     * emitting this version should surface a warning / bump the
     * {@code deprecated_schema_emit} metric in the consumer.
     */
    public static boolean isDeprecated(Map<String, Object> payload) {
        return read(payload) < PREVIOUS;
    }

    /** Strip the schema version key, returning a copy suitable for passing to v1 handlers. */
    public static Map<String, Object> withoutSchemaVersion(Map<String, Object> payload) {
        if (payload == null || !payload.containsKey(SCHEMA_VERSION_KEY)) {
            return payload == null ? Map.of() : Map.copyOf(payload);
        }
        Map<String, Object> out = new HashMap<>(payload);
        out.remove(SCHEMA_VERSION_KEY);
        return out;
    }

    private static void requirePositiveVersion(int v) {
        if (v < 1) {
            throw new IllegalArgumentException(SCHEMA_VERSION_KEY + " must be >= 1, got " + v);
        }
    }
}
