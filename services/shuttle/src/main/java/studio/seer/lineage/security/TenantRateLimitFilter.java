package studio.seer.lineage.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * MTN-10 / SHT-10 — Wire {@link TenantRateLimiter} into the JAX-RS request
 * pipeline. Runs after {@link TenantContextFilter} (priority AUTHENTICATION)
 * so that by the time we read {@code X-Seer-Tenant-Alias} its authenticity has
 * already been verified against the JWT. A request that fails rate-limit is
 * aborted with HTTP 429 + {@code Retry-After: 60}.
 *
 * <p>Super-admin role (header {@code X-Seer-Role: super-admin}) bypasses the
 * bucket — see {@link TenantRateLimiter#allow}. Local per-replica state is the
 * Q-RL-1 decision; coordinated distributed limiting lands with MTN-56 priority
 * tiers once load-shedding policy is in place.
 */
@Provider
@Priority(Priorities.AUTHORIZATION + 1)
public class TenantRateLimitFilter implements ContainerRequestFilter {

    @Inject TenantRateLimiter limiter;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        limiter.checkAndAbort(ctx);
    }
}
