package studio.seer.tenantrouting;

/**
 * Registry for tenant-scoped connections to YGG lineage databases ({@code hound_{alias}}).
 * Consumers: SHUTTLE, MIMIR, ANVIL, Dali (parse flow).
 *
 * Usage:
 * <pre>
 *   {@literal @}Inject YggLineageRegistry lineageRegistry;
 *   {@literal @}Inject TenantContext ctx;
 *
 *   var conn = lineageRegistry.resourceFor(ctx.tenantAlias());
 *   var rows = conn.cypher("MATCH (t:DaliTable) RETURN t", Map.of());
 * </pre>
 */
public interface YggLineageRegistry extends TenantResourceRegistry<ArcadeConnection> {
}
