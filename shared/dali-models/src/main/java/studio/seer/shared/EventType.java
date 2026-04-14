package studio.seer.shared;

/**
 * Канонический реестр типов событий HEIMDALL.
 * Schema фиксируется в Sprint 1 и не меняется при переходе к production observability
 * (меняются collectors и UI, но не schema — решение Q6).
 */
public enum EventType {
    // Hound (I26)
    FILE_PARSING_STARTED,
    FILE_PARSING_COMPLETED,
    FILE_PARSING_FAILED,
    PARSE_ERROR,
    PARSE_WARNING,
    ATOM_EXTRACTED,
    RESOLUTION_COMPLETED,

    // Dali (I27)
    SESSION_STARTED,
    SESSION_COMPLETED,
    SESSION_FAILED,
    WORKER_ASSIGNED,
    JOB_ENQUEUED,
    JOB_COMPLETED,
    COMPLEX_JOB_PROGRESS,

    // MIMIR (I28)
    QUERY_RECEIVED,
    TIER_SELECTED,
    TOOL_CALL_STARTED,
    TOOL_CALL_COMPLETED,
    LLM_RESPONSE_READY,
    CACHE_HIT,

    // ANVIL (I29)
    TRAVERSAL_STARTED,
    TRAVERSAL_PROGRESS,
    TRAVERSAL_COMPLETED,

    // SHUTTLE (I30)
    REQUEST_RECEIVED,
    REQUEST_COMPLETED,
    SUBSCRIPTION_OPENED,

    // HEIMDALL internal
    DEMO_RESET,
    SNAPSHOT_SAVED,
    REPLAY_STARTED
}
