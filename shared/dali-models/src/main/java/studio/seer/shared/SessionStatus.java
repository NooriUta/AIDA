package studio.seer.shared;

/**
 * Lifecycle states for a Dali parse session managed by JobRunr.
 */
public enum SessionStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
