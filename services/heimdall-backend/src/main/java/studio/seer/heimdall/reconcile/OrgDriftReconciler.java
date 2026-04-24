package studio.seer.heimdall.reconcile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import studio.seer.heimdall.snapshot.FriggGateway;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MTN-26 — Daily Keycloak↔FRIGG organization drift reconciler.
 *
 * <p>Compares {@code DaliTenantConfig.keycloakOrgId} in FRIGG against
 * Keycloak's {@code /admin/realms/{realm}/organizations} listing. Emits
 * audit events on any mismatch so the on-call gets a signal before the
 * divergence becomes user-visible (login fails because org was deleted in
 * KC, or an orphan org exists with no backing tenant).
 *
 * <p>Three drift classes are reported:
 * <ul>
 *   <li>{@code FRIGG_ORPHAN} — FRIGG row points at {@code keycloakOrgId}
 *       that no longer exists in KC. Tenant users can't log in.</li>
 *   <li>{@code KC_ORPHAN} — KC organization has no matching FRIGG row.
 *       Leftover from failed provisioning or manual KC edit.</li>
 *   <li>{@code ALIAS_MISMATCH} — the KC {@code alias} and FRIGG
 *       {@code tenantAlias} diverged (manual rename only on one side).</li>
 * </ul>
 *
 * <p>Cron: daily at 03:00 UTC via JobRunr. Also runnable on-demand from a
 * CLI / admin endpoint for forensic investigation.
 *
 * <p>Triggers {@link #run()} are safe to call concurrently — the reconcile
 * is read-only and idempotent (audit events carry a run-id for dedupe).
 */
@ApplicationScoped
public class OrgDriftReconciler {

    private static final Logger LOG = Logger.getLogger(OrgDriftReconciler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject FriggGateway frigg;

    @ConfigProperty(name = "heimdall.reconcile.kc.url",      defaultValue = "http://localhost:18180/kc")
    String kcUrl;
    @ConfigProperty(name = "heimdall.reconcile.kc.realm",    defaultValue = "seer")
    String kcRealm;
    @ConfigProperty(name = "heimdall.reconcile.kc.user",     defaultValue = "admin")
    String kcAdminUser;
    @ConfigProperty(name = "heimdall.reconcile.kc.password", defaultValue = "admin")
    String kcAdminPass;

    /** Visible for direct scheduling by OrgDriftReconcileJob (JobRunr @Job entry point). */
    public Report run() {
        String runId = java.util.UUID.randomUUID().toString();
        LOG.infof("[OrgDriftReconciler] starting runId=%s", runId);

        List<FriggRow> friggRows;
        List<KcOrg>    kcOrgs;
        try {
            friggRows = loadFriggRows();
            kcOrgs    = loadKcOrgs();
        } catch (Exception e) {
            LOG.errorf(e, "[OrgDriftReconciler] data load failed runId=%s", runId);
            return new Report(runId, List.of(), List.of(), List.of(),
                    "data_load_failed: " + e.getMessage());
        }

        Map<String, FriggRow> byOrgId  = new HashMap<>();
        for (var r : friggRows) if (r.keycloakOrgId != null) byOrgId.put(r.keycloakOrgId, r);
        Map<String, KcOrg> kcById = new HashMap<>();
        for (var o : kcOrgs) kcById.put(o.id, o);

        List<String> friggOrphan = new ArrayList<>();
        List<String> kcOrphan    = new ArrayList<>();
        List<String> mismatch    = new ArrayList<>();

        for (var row : friggRows) {
            if (row.keycloakOrgId == null) continue;          // not yet provisioned, skip
            KcOrg kc = kcById.get(row.keycloakOrgId);
            if (kc == null) {
                friggOrphan.add(row.tenantAlias);
                continue;
            }
            if (!row.tenantAlias.equals(kc.alias)) {
                mismatch.add(row.tenantAlias + "<>KC:" + kc.alias);
            }
        }
        for (var org : kcOrgs) {
            if (!byOrgId.containsKey(org.id)) {
                kcOrphan.add(org.alias + "(" + org.id + ")");
            }
        }

        Report report = new Report(runId, friggOrphan, kcOrphan, mismatch, null);
        LOG.infof("[OrgDriftReconciler] runId=%s frigg_orphan=%d kc_orphan=%d alias_mismatch=%d",
                runId, friggOrphan.size(), kcOrphan.size(), mismatch.size());
        if (!report.clean()) {
            LOG.warnf("[seer.audit.tenant_kc_drift_detected] runId=%s friggOrphan=%s kcOrphan=%s aliasMismatch=%s",
                    runId, friggOrphan, kcOrphan, mismatch);
        }
        return report;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    List<FriggRow> loadFriggRows() {
        Uni<List<Map<String, Object>>> q = frigg.sqlTenants(
                "SELECT tenantAlias, keycloakOrgId, status FROM DaliTenantConfig",
                Map.of());
        List<Map<String, Object>> rows = q.await().atMost(Duration.ofSeconds(10));
        List<FriggRow> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            out.add(new FriggRow(
                    String.valueOf(r.getOrDefault("tenantAlias", "")),
                    r.get("keycloakOrgId") == null ? null : String.valueOf(r.get("keycloakOrgId")),
                    String.valueOf(r.getOrDefault("status", "UNKNOWN"))));
        }
        return out;
    }

    List<KcOrg> loadKcOrgs() throws Exception {
        String token = fetchKcAdminToken();
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(kcUrl + "/admin/realms/" + kcRealm + "/organizations?max=1000"))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("KC organizations list: HTTP " + res.statusCode() + " " + res.body());
        }
        JsonNode arr = MAPPER.readTree(res.body());
        List<KcOrg> out = new ArrayList<>();
        for (JsonNode n : arr) {
            out.add(new KcOrg(n.path("id").asText(""), n.path("alias").asText("")));
        }
        return out;
    }

    private String fetchKcAdminToken() throws Exception {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        String form = "grant_type=password"
                + "&client_id=admin-cli"
                + "&username=" + java.net.URLEncoder.encode(kcAdminUser, StandardCharsets.UTF_8)
                + "&password=" + java.net.URLEncoder.encode(kcAdminPass, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(kcUrl + "/realms/master/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("KC admin token: HTTP " + res.statusCode() + " " + res.body());
        }
        return MAPPER.readTree(res.body()).path("access_token").asText();
    }

    // ── Value types ───────────────────────────────────────────────────────────

    record FriggRow(String tenantAlias, String keycloakOrgId, String status) {}
    record KcOrg(String id, String alias) {}

    public record Report(
            String runId,
            List<String> friggOrphan,
            List<String> kcOrphan,
            List<String> aliasMismatch,
            String error
    ) {
        public boolean clean() {
            return error == null && friggOrphan.isEmpty() && kcOrphan.isEmpty() && aliasMismatch.isEmpty();
        }
    }
}
