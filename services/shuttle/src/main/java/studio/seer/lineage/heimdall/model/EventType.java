package studio.seer.lineage.heimdall.model;

/**
 * Local copy of studio.seer.shared.EventType for SHUTTLE.
 * Temporary until Docker multi-module build for SHUTTLE is implemented.
 * Keep in sync with shared/dali-models/EventType.java.
 */
public enum EventType {
    // Hound
    FILE_PARSING_STARTED, FILE_PARSING_COMPLETED, FILE_PARSING_FAILED,
    ATOM_EXTRACTED, RESOLUTION_COMPLETED,

    // Dali
    SESSION_STARTED, SESSION_COMPLETED, SESSION_FAILED,
    WORKER_ASSIGNED, JOB_ENQUEUED, JOB_COMPLETED, COMPLEX_JOB_PROGRESS,

    // MIMIR
    QUERY_RECEIVED, TIER_SELECTED,
    TOOL_CALL_STARTED, TOOL_CALL_COMPLETED, LLM_RESPONSE_READY, CACHE_HIT,

    // ANVIL
    TRAVERSAL_STARTED, TRAVERSAL_PROGRESS, TRAVERSAL_COMPLETED,

    // SHUTTLE
    REQUEST_RECEIVED, REQUEST_COMPLETED, SUBSCRIPTION_OPENED,
    CYPHER_QUERY_SLOW,

    // YGG write observability (emitted by Dali/Hound, recognised by SHUTTLE for reference)
    YGG_WRITE_COMPLETED, YGG_WRITE_FAILED, YGG_CLEAR_COMPLETED,

    // DB health
    DB_CONNECTION_ERROR,

    // HEIMDALL internal
    DEMO_RESET, SNAPSHOT_SAVED, REPLAY_STARTED
}
