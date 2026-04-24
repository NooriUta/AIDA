package studio.seer.lineage.security;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

/**
 * Extracts the authenticated identity from trusted headers set by rbac-proxy.
 *
 * rbac-proxy verifies the JWT from the httpOnly cookie and forwards:
 *   X-Seer-Role: viewer | editor | admin
 *   X-Seer-User: <username>
 *
 * lineage-api is an internal service — never exposed directly to the browser.
 * In production, enforce this at the network level (firewall / compose network).
 */
@RequestScoped
public class SeerIdentity {

    @Inject
    RoutingContext rc;

    public String role() {
        String role = rc.request().getHeader("X-Seer-Role");
        return (role != null && !role.isBlank()) ? role : "viewer";
    }

    public String username() {
        String user = rc.request().getHeader("X-Seer-User");
        return (user != null && !user.isBlank()) ? user : "anonymous";
    }

    public String tenantAlias() {
        String alias = rc.request().getHeader("X-Seer-Tenant-Alias");
        return (alias != null && !alias.isBlank()) ? alias : "default";
    }

    public boolean isAdmin()   { return "admin".equals(role()); }
    public boolean isEditor()  { return "editor".equals(role()) || isAdmin(); }
    public boolean canWrite()  { return isEditor(); }
}
