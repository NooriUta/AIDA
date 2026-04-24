package studio.seer.tenantrouting;

/**
 * Thrown by {@link YggLineageRegistry#resourceFor(String)} when the requested
 * tenant cannot be served: missing from {@code frigg-tenants}, or
 * {@code status != ACTIVE} (SUSPENDED / ARCHIVED / PURGED / PROVISIONING).
 *
 * <p>SHUTTLE / DALI / ANVIL resource-layer catches this and returns 403
 * {@code tenant_not_available} instead of 500 — signals an authorization
 * decision to the client, not a backend failure.
 *
 * <p>Deliberately a {@link RuntimeException} subclass so it propagates through
 * Mutiny pipelines without extra declared-throws boilerplate.
 */
public class TenantNotAvailableException extends RuntimeException {

    private final String tenantAlias;
    private final Reason reason;

    public enum Reason {
        /** Tenant not present in frigg-tenants. */
        NOT_FOUND,
        /** Status is SUSPENDED — admin manually blocked; can be unsuspended. */
        SUSPENDED,
        /** Status is ARCHIVED — data dropped; requires restore first. */
        ARCHIVED,
        /** Status is PURGED — terminal, no data. */
        PURGED,
        /** Status is PROVISIONING — partially provisioned; routing not ready. */
        PROVISIONING,
        /** Underlying FRIGG lookup failed (DB unavailable). */
        LOOKUP_FAILED,
    }

    public TenantNotAvailableException(String tenantAlias, Reason reason) {
        super("Tenant '" + tenantAlias + "' is not available: " + reason);
        this.tenantAlias = tenantAlias;
        this.reason = reason;
    }

    public TenantNotAvailableException(String tenantAlias, Reason reason, Throwable cause) {
        super("Tenant '" + tenantAlias + "' is not available: " + reason, cause);
        this.tenantAlias = tenantAlias;
        this.reason = reason;
    }

    public String tenantAlias() { return tenantAlias; }
    public Reason reason()      { return reason; }
}
