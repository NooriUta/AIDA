package com.mimir.byok;

import com.mimir.persistence.FriggClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * FRIGG-backed storage for {@link TenantLlmConfig} — used by
 * {@link LlmCredentialResolver} on cache miss.
 *
 * <p>Persists each config as a vertex in {@code DaliTenantLlmConfig}
 * (tenant-config DB). One row per tenant alias enforced by unique index.
 *
 * <p>Failures are logged at WARN and surfaced as empty Optional / no-op so
 * that BYOK setup outage cannot brick a tenant — the resolver falls through
 * to the static default model (DeepSeek shared key).
 */
@ApplicationScoped
public class TenantLlmConfigStore {

    private static final Logger LOG = Logger.getLogger(TenantLlmConfigStore.class);
    private static final String TYPE = "DaliTenantLlmConfig";

    @Inject @RestClient FriggClient frigg;
    @Inject CredentialEncryptor encryptor;

    @ConfigProperty(name = "mimir.frigg.tenants-db", defaultValue = "tenant-config")
    String tenantsDb;

    @ConfigProperty(name = "mimir.byok.store.skip-init", defaultValue = "false")
    boolean skipInit;

    void onStart(@Observes StartupEvent ev) {
        if (skipInit) {
            LOG.info("BYOK store init skipped (mimir.byok.store.skip-init=true)");
            return;
        }
        try {
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE VERTEX TYPE " + TYPE + " IF NOT EXISTS"));
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE PROPERTY " + TYPE + ".tenantAlias IF NOT EXISTS STRING"));
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE INDEX IF NOT EXISTS ON " + TYPE + " (tenantAlias) UNIQUE"));
            LOG.infof("BYOK store ready: %s.%s", tenantsDb, TYPE);
        } catch (Exception e) {
            LOG.warnf(e, "BYOK store init failed (db=%s) — BYOK lookups will return empty", tenantsDb);
        }
    }

    /** Looks up encrypted config for tenant. Empty when row missing or FRIGG unreachable. */
    public Optional<TenantLlmConfig> findByTenant(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return Optional.empty();
        try {
            FriggClient.QueryResult r = frigg.query(tenantsDb,
                    new FriggClient.FriggQuery("sql",
                            "SELECT FROM " + TYPE + " WHERE tenantAlias = :a LIMIT 1",
                            Map.of("a", tenantAlias)));
            if (r == null || r.result() == null || r.result().isEmpty()) return Optional.empty();
            Map<String, Object> row = r.result().get(0);
            return Optional.of(new TenantLlmConfig(
                    str(row, "tenantAlias"),
                    str(row, "provider"),
                    str(row, "encryptedApiKey"),
                    str(row, "baseUrl"),
                    str(row, "modelName"),
                    str(row, "updatedAt")
            ));
        } catch (Exception e) {
            LOG.warnf(e, "BYOK findByTenant failed for tenant=%s", tenantAlias);
            return Optional.empty();
        }
    }

    /** UPSERT: encrypts the plain key, writes the row. Throws on FRIGG error. */
    public void save(String tenantAlias, String provider, String plainApiKey,
                     String baseUrl, String modelName) {
        if (tenantAlias == null || tenantAlias.isBlank()) {
            throw new CredentialException("tenantAlias is required");
        }
        if (provider == null || provider.isBlank()) {
            throw new CredentialException("provider is required");
        }
        if (plainApiKey == null || plainApiKey.isBlank()) {
            throw new CredentialException("apiKey is required");
        }
        String encrypted = encryptor.encrypt(plainApiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("alias", tenantAlias);
        params.put("provider", provider.toLowerCase());
        params.put("encryptedApiKey", encrypted);
        params.put("baseUrl", baseUrl);
        params.put("modelName", modelName);
        params.put("updatedAt", Instant.now().toString());
        frigg.command(tenantsDb,
                new FriggClient.FriggCommand("sql",
                        "UPDATE " + TYPE + " SET tenantAlias=:alias, provider=:provider, " +
                        "encryptedApiKey=:encryptedApiKey, baseUrl=:baseUrl, modelName=:modelName, " +
                        "updatedAt=:updatedAt UPSERT WHERE tenantAlias=:alias",
                        params));
    }

    public boolean delete(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return false;
        try {
            frigg.command(tenantsDb,
                    new FriggClient.FriggCommand("sql",
                            "DELETE FROM " + TYPE + " WHERE tenantAlias = :a",
                            Map.of("a", tenantAlias)));
            return true;
        } catch (Exception e) {
            LOG.warnf(e, "BYOK delete failed for tenant=%s", tenantAlias);
            return false;
        }
    }

    private static String str(Map<String, Object> row, String k) {
        Object v = row.get(k);
        return v == null ? null : v.toString();
    }
}
