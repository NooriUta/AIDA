package studio.seer.heimdall.snapshot;

/**
 * Metadata record for a stored HEIMDALL snapshot (no event payload).
 */
public record SnapshotInfo(String id, String name, long timestamp, int eventCount) {}
