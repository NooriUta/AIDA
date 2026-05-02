package com.mimir.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Resolves tenantAlias → ArcadeDB database name.
 *
 * <p>Q-MC1 closed: template-resolver pattern (не registry) — простое substitution
 * с заменой hyphen на underscore (`acme-corp` → `hound_acme_corp`).
 * Совпадает с DaliTenantConfig naming policy (Decision #53).
 *
 * <p>Если tenantAlias = null/blank → fallback на `hound_default`.
 *
 * <p>Override через config: {@code mimir.dbname.template=hound_%s}.
 */
@ApplicationScoped
public class DbNameResolver {

    @ConfigProperty(name = "mimir.dbname.template", defaultValue = "hound_%s")
    String template;

    @ConfigProperty(name = "mimir.dbname.default", defaultValue = "hound_default")
    String defaultDb;

    public String forTenant(String alias) {
        if (alias == null || alias.isBlank()) {
            return defaultDb;
        }
        String normalized = alias.toLowerCase().replace('-', '_');
        return String.format(template, normalized);
    }
}
