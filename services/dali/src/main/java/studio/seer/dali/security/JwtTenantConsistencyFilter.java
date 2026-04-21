package studio.seer.dali.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.tenantrouting.TenantContext;

import java.io.IOException;
import java.util.Base64;

/**
 * DMT-02 — JWT ↔ header consistency check.
 *
 * <p>Reads the Bearer JWT and extracts {@code organization.alias} claim.
 * If both the header alias (from {@link TenantContext}) and the JWT claim are present
 * and they do NOT match → 403 Forbidden + emit spoofing event.
 *
 * <p>Runs at {@link Priorities#AUTHORIZATION} (500) — after TenantContextFilter (AUTHENTICATION=1000)
 * has already validated and parsed the alias.
 *
 * <p>Missing JWT or missing claim → pass-through (dev/local without KC).
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class JwtTenantConsistencyFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTenantConsistencyFilter.class);

    @Inject TenantContext tenantCtx;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String authorization = ctx.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return; // no JWT — local dev / test, pass through
        }
        String jwtAlias = extractOrgAlias(authorization.substring(7));
        if (jwtAlias == null || jwtAlias.isBlank()) {
            return; // claim absent — pass through
        }
        String headerAlias = tenantCtx.tenantAlias();
        if (!jwtAlias.equals(headerAlias)) {
            log.warn("[DMT-02] Tenant spoofing attempt: JWT org.alias='{}' != X-Seer-Tenant-Alias='{}'",
                    jwtAlias, headerAlias);
            ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"JWT organization.alias does not match X-Seer-Tenant-Alias\"}")
                    .type("application/json")
                    .build());
        }
    }

    /**
     * Extracts {@code organization.alias} from a JWT payload (Base64url-encoded).
     * Returns null if the claim is absent or the JWT is malformed.
     */
    static String extractOrgAlias(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            String payload = new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8);
            // Simple JSON field extraction — no full Jackson parse to keep it lightweight
            return extractJsonString(payload, "organization");
        } catch (Exception e) {
            log.debug("[DMT-02] Could not parse JWT payload: {}", e.getMessage());
            return null;
        }
    }

    private static String extractJsonString(String json, String outerKey) {
        // Find "organization":{"alias":"<value>"} or "organization.alias":"<value>"
        int outerIdx = json.indexOf("\"" + outerKey + "\"");
        if (outerIdx < 0) return null;
        int aliasIdx = json.indexOf("\"alias\"", outerIdx);
        if (aliasIdx < 0) return null;
        int colonIdx = json.indexOf(':', aliasIdx + 7);
        if (colonIdx < 0) return null;
        int startQuote = json.indexOf('"', colonIdx + 1);
        if (startQuote < 0) return null;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    private static String padBase64(String base64) {
        return switch (base64.length() % 4) {
            case 2  -> base64 + "==";
            case 3  -> base64 + "=";
            default -> base64;
        };
    }
}
