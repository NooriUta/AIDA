package com.mimir.tenant;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS request filter — populates {@link TenantContext} on every incoming request.
 *
 * <p>Reads X-Seer-Tenant-Alias header and X-Seer-Session-Id header (optional).
 * Skips for /q/* (health, metrics) endpoints.
 */
@Provider
public class TenantContextFilter implements ContainerRequestFilter {

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getPath();
        if (path.startsWith("q/")) {
            return; // health/metrics — no tenant context
        }
        String alias = ctx.getHeaderString("X-Seer-Tenant-Alias");
        String session = ctx.getHeaderString("X-Seer-Session-Id");
        tenantContext.set(alias, session);
    }
}
