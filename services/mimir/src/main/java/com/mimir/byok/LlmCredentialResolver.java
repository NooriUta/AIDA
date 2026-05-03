package com.mimir.byok;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mimir.security.Secret;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Optional;

/**
 * Resolves a tenant's BYOK key (decrypted, ready for LangChain4j) with a
 * Caffeine cache to avoid hammering FRIGG on every request.
 *
 * <p>Cache miss path: {@link TenantLlmConfigStore#findByTenant} → decrypt via
 * {@link CredentialEncryptor} → wrap raw key in {@link Secret} → store
 * {@link ResolvedKey} for {@code mimir.byok.cache.ttl-minutes}.
 *
 * <p>Returns empty when no config exists for the tenant — caller must fall
 * back to the shared default model. Returns empty also on decryption failures
 * (logged WARN with masked key envelope), never throwing through to callers.
 */
@ApplicationScoped
public class LlmCredentialResolver {

    private static final Logger LOG = Logger.getLogger(LlmCredentialResolver.class);

    @Inject TenantLlmConfigStore store;
    @Inject CredentialEncryptor encryptor;

    @ConfigProperty(name = "mimir.byok.cache.ttl-minutes", defaultValue = "5")
    int ttlMinutes;

    @ConfigProperty(name = "mimir.byok.cache.max-size", defaultValue = "1000")
    int maxSize;

    private Cache<String, Optional<ResolvedKey>> cache;

    @PostConstruct
    void init() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .maximumSize(maxSize)
                .build();
        LOG.infof("BYOK resolver cache initialised (TTL=%dm, max=%d)", ttlMinutes, maxSize);
    }

    public Optional<ResolvedKey> resolveForTenant(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return Optional.empty();
        return cache.get(tenantAlias, this::loadFromStore);
    }

    public void invalidate(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return;
        cache.invalidate(tenantAlias);
        LOG.debugf("BYOK cache invalidated for tenant=%s", tenantAlias);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    private Optional<ResolvedKey> loadFromStore(String tenantAlias) {
        Optional<TenantLlmConfig> cfg = store.findByTenant(tenantAlias);
        if (cfg.isEmpty()) return Optional.empty();
        TenantLlmConfig c = cfg.get();
        try {
            String plain = encryptor.decrypt(c.encryptedApiKey());
            return Optional.of(new ResolvedKey(
                    c.tenantAlias(),
                    c.provider(),
                    Secret.of(plain),
                    c.baseUrl(),
                    c.modelName()
            ));
        } catch (CredentialException e) {
            LOG.warnf("BYOK decrypt failed for tenant=%s — falling back to default model", tenantAlias);
            return Optional.empty();
        }
    }
}
