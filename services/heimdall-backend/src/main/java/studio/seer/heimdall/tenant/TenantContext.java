package studio.seer.heimdall.tenant;

import jakarta.enterprise.context.RequestScoped;
import java.util.List;

/**
 * Request-scoped bean carrying the authenticated user's identity as extracted
 * from Chur trusted headers (X-Seer-*).
 *
 * Chur validates the Keycloak JWT, checks scopes via requireScope(), and then
 * forwards trusted headers to upstream services.  HEIMDALL reads these headers
 * in {@link TenantContextFilter} and injects them into this bean for use by
 * resource classes.
 *
 * Header protocol:
 *   X-Seer-User-Id     — Keycloak sub (UUID)
 *   X-Seer-Scopes      — space-separated scope list (e.g. "seer:read aida:admin")
 *   X-Seer-Tenant      — tenantId (Phase 1: always "default")
 *   X-Seer-Tenant-Role — effective role string (e.g. "admin")
 *   X-Seer-Role        — same as Tenant-Role (backward-compat with ControlResource)
 */
@RequestScoped
public class TenantContext {

    private String       userId;
    private String       tenantId;
    private String       tenantRole;
    private List<String> scopes = List.of();

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String       getUserId()    { return userId;     }
    public String       getTenantId()  { return tenantId;   }
    public String       getTenantRole(){ return tenantRole;  }
    public List<String> getScopes()    { return scopes;     }

    public void setUserId(String userId)          { this.userId     = userId;     }
    public void setTenantId(String tenantId)      { this.tenantId   = tenantId;   }
    public void setTenantRole(String tenantRole)  { this.tenantRole = tenantRole; }
    public void setScopes(List<String> scopes)    { this.scopes     = scopes != null ? scopes : List.of(); }

    // ── Convenience scope checks ──────────────────────────────────────────────

    public boolean hasScope(String scope)  { return scopes.contains(scope); }
    public boolean isSuperAdmin()          { return hasScope("aida:superadmin"); }
    public boolean isAdmin()               { return hasScope("aida:admin"); }
    public boolean isTenantOwner()         { return hasScope("aida:tenant:owner"); }
    public boolean isLocalAdmin()          { return hasScope("aida:tenant:admin"); }

    /** Convenience alias matching ControlResource's existing isAdmin(role) pattern. */
    public boolean isAdminRole() {
        return "admin".equalsIgnoreCase(tenantRole)
            || "super-admin".equalsIgnoreCase(tenantRole);
    }
}
