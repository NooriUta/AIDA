package studio.seer.tenantrouting;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness probe: verifies that the default lineage registry can reach its
 * ArcadeDB database ({@code hound_default}). Included in /q/health/ready.
 */
@Readiness
@ApplicationScoped
public class TenantRoutingHealthCheck implements HealthCheck {

    @Inject
    YggLineageRegistry lineageRegistry;

    @Override
    public HealthCheckResponse call() {
        try {
            var conn = lineageRegistry.resourceFor("default");
            conn.sql("SELECT 1", java.util.Map.of());
            return HealthCheckResponse.up("tenant-routing-ygg-lineage");
        } catch (Exception e) {
            return HealthCheckResponse.named("tenant-routing-ygg-lineage")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
