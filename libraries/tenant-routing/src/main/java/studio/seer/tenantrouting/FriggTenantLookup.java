package studio.seer.tenantrouting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * MTN-01: Reads {@code DaliTenantConfig} vertex from FRIGG {@code frigg-tenants}
 * database. Used by {@link FriggYggLineageRegistry} to resolve tenant-specific
 * routing (yggLineageDbName, configVersion, status).
 *
 * <p>Stateless + thread-safe. The backing {@link HttpClient} is reused.
 *
 * <p>Separate from {@link HttpArcadeConnection} because the lookup target is a
 * different database ({@code frigg-tenants} on FRIGG :2481) from the tenant's
 * lineage DB ({@code hound_{alias}} on YGG :2480).
 */
public class FriggTenantLookup {

    private static final Logger log = LoggerFactory.getLogger(FriggTenantLookup.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TENANTS_DB = "frigg-tenants";

    /** Tenant alias must be a safe SQL literal — validated by {@link studio.seer.tenantrouting.TenantContextFilter}. */
    private static final java.util.regex.Pattern ALIAS_REGEX =
            java.util.regex.Pattern.compile("^[a-z][a-z0-9-]{2,30}[a-z0-9]$");

    public record TenantRouting(
            String tenantAlias,
            String status,                    // ACTIVE / SUSPENDED / ARCHIVED / PROVISIONING / PURGED
            int    configVersion,
            String yggLineageDbName,
            String yggSourceArchiveDbName,
            String friggDaliDbName,
            String yggInstanceUrl,
            String keycloakOrgId,
            /** MTN-48: per-tenant LRU slot cap override. null → inherit global. */
            Integer connectionCap
    ) {}

    private final HttpClient http;
    private final String baseUrl;
    private final String authHeader;

    public FriggTenantLookup(HttpClient http, String baseUrl, String username, String password) {
        this.http       = http;
        this.baseUrl    = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Lookup the tenant routing record. Returns {@link Optional#empty()} when the
     * alias is not present. Throws {@link TenantNotAvailableException} with
     * {@link TenantNotAvailableException.Reason#LOOKUP_FAILED} on FRIGG I/O error.
     */
    @SuppressWarnings("unchecked")
    public Optional<TenantRouting> lookup(String tenantAlias) {
        if (tenantAlias == null || !ALIAS_REGEX.matcher(tenantAlias).matches()) {
            throw new TenantNotAvailableException(
                    tenantAlias == null ? "(null)" : tenantAlias,
                    TenantNotAvailableException.Reason.NOT_FOUND);
        }

        try {
            var body = MAPPER.writeValueAsString(Map.of(
                    "language", "sql",
                    "command",  "SELECT tenantAlias, status, configVersion, " +
                                "yggLineageDbName, yggSourceArchiveDbName, friggDaliDbName, " +
                                "yggInstanceUrl, keycloakOrgId, connectionCap " +
                                "FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1",
                    "params",   Map.of("alias", tenantAlias)
            ));
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/query/" + TENANTS_DB))
                    .header("Content-Type",  "application/json")
                    .header("Authorization", authHeader)
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("FRIGG lookup for '{}' returned {}: {}",
                        tenantAlias, response.statusCode(), response.body());
                throw new TenantNotAvailableException(
                        tenantAlias, TenantNotAvailableException.Reason.LOOKUP_FAILED);
            }
            var parsed = MAPPER.readValue(response.body(), Map.class);
            var result = parsed.get("result");
            if (!(result instanceof java.util.List<?> rows) || rows.isEmpty()) {
                return Optional.empty();
            }
            var row = (Map<String, Object>) rows.get(0);
            Object rawCap = row.get("connectionCap");
            Integer connectionCap = null;
            if (rawCap instanceof Number n) connectionCap = n.intValue();
            return Optional.of(new TenantRouting(
                    (String) row.get("tenantAlias"),
                    (String) row.getOrDefault("status", "UNKNOWN"),
                    ((Number) row.getOrDefault("configVersion", 0)).intValue(),
                    (String) row.get("yggLineageDbName"),
                    (String) row.get("yggSourceArchiveDbName"),
                    (String) row.get("friggDaliDbName"),
                    (String) row.get("yggInstanceUrl"),
                    (String) row.get("keycloakOrgId"),
                    connectionCap
            ));
        } catch (TenantNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("FRIGG lookup failed for '{}': {}", tenantAlias, e.getMessage());
            throw new TenantNotAvailableException(
                    tenantAlias, TenantNotAvailableException.Reason.LOOKUP_FAILED, e);
        }
    }
}
