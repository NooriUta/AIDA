package studio.seer.tenantrouting;

/**
 * Registry for tenant-scoped connections to YGG source archive databases
 * ({@code hound_src_{alias}}).
 * Consumers: Dali (upload flow, Skadi fetch).
 */
public interface YggSourceArchiveRegistry extends TenantResourceRegistry<ArcadeConnection> {
}
