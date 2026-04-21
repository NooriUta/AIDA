package studio.seer.heimdall.tenant;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import java.util.Arrays;
import java.util.List;

/**
 * JAX-RS pre-matching filter that populates {@link TenantContext} from
 * Chur-forwarded trusted headers on every request.
 *
 * <p>Must run before any resource method so that injected {@code TenantContext}
 * beans are ready when the first resource method executes.  The {@code @PreMatching}
 * annotation guarantees this.
 *
 * <p>All HEIMDALL endpoints are on the internal Docker network; Chur is the only
 * ingress point.  We therefore trust these headers unconditionally.
 */
@Provider
@PreMatching
public class TenantContextFilter implements ContainerRequestFilter {

    @Inject
    TenantContext ctx;

    @Override
    public void filter(ContainerRequestContext req) {
        ctx.setUserId(req.getHeaderString("X-Seer-User-Id"));
        ctx.setTenantId(req.getHeaderString("X-Seer-Tenant"));

        // Accept both X-Seer-Tenant-Role (new) and X-Seer-Role (backward-compat)
        String role = req.getHeaderString("X-Seer-Tenant-Role");
        if (role == null || role.isBlank()) {
            role = req.getHeaderString("X-Seer-Role");
        }
        ctx.setTenantRole(role);

        String scopesHeader = req.getHeaderString("X-Seer-Scopes");
        List<String> scopes = (scopesHeader != null && !scopesHeader.isBlank())
            ? Arrays.asList(scopesHeader.split("\\s+"))
            : List.of();
        ctx.setScopes(scopes);
    }
}
