package studio.seer.tenantrouting;

import jakarta.enterprise.context.RequestScoped;
import java.util.List;

/**
 * Mutable per-request holder populated by {@link TenantContextFilter} before CDI
 * producers run. Kept package-private; callers inject {@link TenantContext} directly.
 */
@RequestScoped
public class TenantContextHolder {

    private String tenantAlias;
    private String userId;
    private String userEmail;
    private List<String> scopes = List.of();
    private List<String> roles  = List.of();
    private String correlationId;

    void init(String tenantAlias,
              String userId,
              String userEmail,
              List<String> scopes,
              List<String> roles,
              String correlationId) {
        this.tenantAlias   = tenantAlias;
        this.userId        = userId;
        this.userEmail     = userEmail;
        this.scopes        = scopes != null ? scopes : List.of();
        this.roles         = roles  != null ? roles  : List.of();
        this.correlationId = correlationId;
    }

    String tenantAlias()   { return tenantAlias; }
    String userId()        { return userId; }
    String userEmail()     { return userEmail; }
    List<String> scopes()  { return scopes; }
    List<String> roles()   { return roles; }
    String correlationId() { return correlationId; }
}
