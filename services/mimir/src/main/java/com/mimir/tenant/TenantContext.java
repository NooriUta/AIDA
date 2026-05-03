package com.mimir.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Per-request tenant context — populated by {@link TenantContextFilter} from
 * the X-Seer-Tenant-Alias header, consumed by tools (AnvilTools, ShuttleTools, YggTools)
 * and by orchestration / routing layers.
 *
 * <p>Why @RequestScoped: @Tool methods на {@code @RegisterAiService} interface не получают
 * tenantAlias как параметр от LLM. Tools нужно injection точку для текущего tenant.
 * RequestScoped bean заполняется JAX-RS filter-ом на входе и доступен всем CDI beans
 * в течение request scope.
 *
 * <p>Sessionid также пробрасывается для correlation в HEIMDALL events.
 */
@RequestScoped
public class TenantContext {

    private String alias;
    private String sessionId;

    @Inject
    @ConfigProperty(name = "mimir.default-tenant-alias", defaultValue = "default")
    String defaultAlias;

    public String alias() {
        return (alias != null && !alias.isBlank()) ? alias : defaultAlias;
    }

    public String sessionId() {
        return sessionId;
    }

    public void set(String alias, String sessionId) {
        this.alias = alias;
        this.sessionId = sessionId;
    }
}
