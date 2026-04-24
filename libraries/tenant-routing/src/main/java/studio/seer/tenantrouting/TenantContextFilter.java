package studio.seer.tenantrouting;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JAX-RS filter that reads Chur-forwarded headers and populates {@link TenantContextHolder}.
 * Registered automatically via @Provider scanning. Runs at AUTHENTICATION priority so it
 * fires before any application-level filters.
 *
 * Missing X-Seer-Tenant-Alias → 400.
 * Invalid alias format        → 400.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TenantContextFilter implements ContainerRequestFilter {

    static final Pattern ALIAS_REGEX = Pattern.compile("^[a-z][a-z0-9-]{2,30}[a-z0-9]$");

    @Inject
    TenantContextHolder holder;

    /** Returns an error message if alias is invalid, null if valid. Extracted for unit testability. */
    static String validateAlias(String alias) {
        if (alias == null || alias.isBlank()) return "Missing X-Seer-Tenant-Alias header";
        if (!ALIAS_REGEX.matcher(alias).matches()) return "Invalid X-Seer-Tenant-Alias format";
        return null;
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String alias = ctx.getHeaderString("X-Seer-Tenant-Alias");
        String err = validateAlias(alias);
        if (err != null) {
            ctx.abortWith(Response.status(400)
                    .entity("{\"error\":\"" + err + "\"}")
                    .type("application/json")
                    .build());
            return;
        }

        String userId        = ctx.getHeaderString("X-Seer-User-Id");
        String userEmail     = ctx.getHeaderString("X-Seer-User-Email");
        String scopesRaw     = ctx.getHeaderString("X-Seer-Scopes");
        String rolesRaw      = ctx.getHeaderString("X-Seer-Roles");
        String correlationId = ctx.getHeaderString("X-Correlation-ID");

        List<String> scopes = scopesRaw != null && !scopesRaw.isBlank()
                ? Arrays.asList(scopesRaw.trim().split("\\s+"))
                : List.of();
        List<String> roles = rolesRaw != null && !rolesRaw.isBlank()
                ? Arrays.asList(rolesRaw.trim().split(","))
                : List.of();

        holder.init(alias, userId, userEmail, scopes, roles, correlationId);
    }
}
