package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("Aggregated statistics for a single tenant")
public record TenantStats(
        String tenantAlias,
        String status,
        long   sessionCount,
        long   routineCount,
        long   tableCount
) {}
