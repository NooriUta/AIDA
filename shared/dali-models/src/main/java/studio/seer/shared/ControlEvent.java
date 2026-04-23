package studio.seer.shared;

import java.util.Map;
import java.util.UUID;

/**
 * MTN-51 — Canonical control-plane event shape.
 *
 * <p>Persisted append-only to {@code heimdall.ControlEvent} vertex. Producers
 * use {@link #newEvent} to stamp an idempotency {@code id} ({@link UUID});
 * consumers store the highest-applied {@code id} per tenant as their offset
 * so they can replay from there on restart (MTN-51 pull-based delivery).
 *
 * <p>Fields mirror {@link HeimdallEvent} where overlap makes sense, but
 * ControlEvent is a narrower contract: no severity / session / user context,
 * only tenant-scoped orchestration signals (invalidate / suspend / archive /
 * purge / reconnect). The rich event shape stays on {@link HeimdallEvent}.
 *
 * @param id             idempotency key (UUIDv4)
 * @param tenantAlias    the tenant the event acts on
 * @param eventType      {@code tenant_invalidated} / {@code tenant_suspended} / ...
 * @param fenceToken     monotonic from HEIMDALL leader (ADR-MT-004)
 * @param schemaVersion  payload schema version (ADR-MT-005)
 * @param createdAt      unix ms at emit time
 * @param payload        event-specific data (already validated against schemaVersion)
 */
public record ControlEvent(
        String  id,
        String  tenantAlias,
        String  eventType,
        long    fenceToken,
        int     schemaVersion,
        long    createdAt,
        Map<String, Object> payload
) {

    public ControlEvent {
        if (id == null || id.isBlank())            throw new IllegalArgumentException("id required");
        if (tenantAlias == null || tenantAlias.isBlank()) throw new IllegalArgumentException("tenantAlias required");
        if (eventType == null || eventType.isBlank())     throw new IllegalArgumentException("eventType required");
        if (fenceToken <= 0)                       throw new IllegalArgumentException("fenceToken must be > 0");
        if (schemaVersion < 1)                     throw new IllegalArgumentException("schemaVersion must be >= 1");
        if (createdAt <= 0)                        throw new IllegalArgumentException("createdAt must be > 0");
        if (payload == null)                       throw new IllegalArgumentException("payload required (empty Map ok)");
    }

    /**
     * Factory — allocates a new UUIDv4 id + stamps createdAt. Caller supplies
     * fenceToken from HEIMDALL's FenceTokenProvider.
     */
    public static ControlEvent newEvent(
            String tenantAlias,
            String eventType,
            long   fenceToken,
            int    schemaVersion,
            Map<String, Object> payload
    ) {
        return new ControlEvent(
                UUID.randomUUID().toString(),
                tenantAlias,
                eventType,
                fenceToken,
                schemaVersion,
                System.currentTimeMillis(),
                payload
        );
    }
}
