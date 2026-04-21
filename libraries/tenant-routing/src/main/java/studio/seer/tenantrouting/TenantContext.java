package studio.seer.tenantrouting;

import java.util.List;

/**
 * Immutable per-request tenant identity. Built by {@link TenantContextFilter} from
 * Chur-forwarded headers. Injected as @RequestScoped CDI bean via {@link TenantContextProducer}.
 *
 * Header sources (all set by Chur BFF, never by the end user):
 *   X-Seer-Tenant-Alias  → tenantAlias
 *   X-Seer-User-Id       → userId
 *   X-Seer-User-Email    → userEmail
 *   X-Seer-Scopes        → scopes (space-separated)
 *   X-Seer-Roles         → roles  (comma-separated)
 *   X-Correlation-ID     → correlationId
 */
public record TenantContext(
        String tenantAlias,
        String userId,
        String userEmail,
        List<String> scopes,
        List<String> roles,
        String correlationId
) {
    public boolean isAdmin()      { return scopes.contains("aida:admin"); }
    public boolean isSuperadmin() { return scopes.contains("aida:superadmin"); }
    public boolean canReadLore()  { return true; }
    public boolean canWriteLore() { return isSuperadmin(); }
}
