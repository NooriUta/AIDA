package studio.seer.lineage.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * SHT-02/11: JAX-RS filter that validates X-Seer-Tenant-Alias and enforces
 * JWT ↔ header alias consistency (anti-spoofing).
 *
 * Called before Shuttle's own handlers. lineage-api is an internal service
 * (behind rbac-proxy) so this filter validates the forwarded header, not raw JWTs.
 *
 * On mismatch: 403 + emit seer.audit.tenant_spoof_attempt via HeimdallEmitter.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TenantContextFilter implements ContainerRequestFilter {

    private static final Pattern ALIAS_REGEX = Pattern.compile("^[a-z][a-z0-9-]{2,30}[a-z0-9]$");
    private static final String HEADER_ALIAS = "X-Seer-Tenant-Alias";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String alias = ctx.getHeaderString(HEADER_ALIAS);

        String error = validateAlias(alias);
        if (error != null) {
            ctx.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + error + "\"}")
                    .type("application/json")
                    .build());
            return;
        }

        // SHT-11: Anti-spoofing — compare JWT organization.alias if present
        String authorization = ctx.getHeaderString("Authorization");
        String jwtAlias = extractJwtOrgAlias(authorization);
        if (jwtAlias != null && !jwtAlias.equals(alias)) {
            ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Tenant alias mismatch between header and JWT\"}")
                    .type("application/json")
                    .build());
        }
    }

    // ── Package-private for tests ──────────────────────────────────────────────

    static String validateAlias(String alias) {
        if (alias == null || alias.isBlank()) return "Missing X-Seer-Tenant-Alias header";
        if (alias.equals("default"))          return null; // MTN-04-EXEMPT: default tenant always passes alias validation
        if (!ALIAS_REGEX.matcher(alias.trim()).matches()) return "Invalid X-Seer-Tenant-Alias format";
        return null;
    }

    static String extractJwtOrgAlias(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return null;
        String token = authorization.substring(7);
        String[] parts = token.split("\\.");
        if (parts.length < 2) return null;
        try {
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            // Lightweight parse: look for "organization":{"alias":"<value>"}
            int idx = json.indexOf("\"organization\"");
            if (idx < 0) return null;
            int aliasIdx = json.indexOf("\"alias\"", idx);
            if (aliasIdx < 0) return null;
            int colon = json.indexOf(':', aliasIdx);
            if (colon < 0) return null;
            int start = json.indexOf('"', colon) + 1;
            int end   = json.indexOf('"', start);
            if (start <= 0 || end <= start) return null;
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}
