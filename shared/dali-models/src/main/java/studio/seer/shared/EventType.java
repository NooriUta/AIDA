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
    MODEL_SELECTED,           // {model, reason: "request"|"default"|"byok"} — Decision #70
    TOOL_CALL_STARTED,
    TOOL_CALL_COMPLETED,
    LLM_REQUEST_STARTED,      // {model, prompt_tokens}
    LLM_RESPONSE_RECEIVED,    // {model, duration_ms, prompt_tokens, completion_tokens}
    LLM_RESPONSE_READY,
    RESPONSE_SYNTHESIZED,     // {total_duration_ms, total_tokens, affected_nodes}
    FALLBACK_ACTIVATED,       // {reason: "429"|"timeout"|"error", new_tier}
    TIMEOUT,                  // {elapsed_ms, threshold_ms}
    CACHE_HIT,
    LLM_CREDENTIAL_USED,      // {tenant_alias, provider, key_mask} — BYOK MT-06
    LLM_CONFIG_UPDATED,       // {tenant_alias, provider, action: "set"|"delete", admin}
    QUOTA_EXCEEDED,           // {tenant_alias, reason, current, limit, reset_at} — MT-07
    TOKEN_USAGE_RECORDED,     // {tenant_alias, provider, model, prompt_tokens, completion_tokens, cost_usd}
    HIL_PAUSE_REQUESTED,      // {tenant_alias, session_id, approval_id, reason} — MT-08
    HIL_DECISION_MADE,        // {session_id, approve, decided_by, comment}

    // ANVIL (I29)
    TRAVERSAL_STARTED,
    TRAVERSAL_PROGRESS,
    TRAVERSAL_COMPLETED,
    QUERY_EXECUTED,
    QUERY_BLOCKED,

    // SHUTTLE (I30)
    REQUEST_RECEIVED,
    REQUEST_COMPLETED,
    SUBSCRIPTION_OPENED,

    // YGG writes (Hound → YGG)
    YGG_WRITE_COMPLETED,    // {session_id, vertices_written, edges_written, duration_ms}
    YGG_WRITE_FAILED,       // {session_id, error_code, retries}
    YGG_CLEAR_COMPLETED,    // {session_id, deleted_vertices, deleted_edges, duration_ms}

    // SHUTTLE → YGG performance
    CYPHER_QUERY_SLOW,      // {query_type, duration_ms, threshold_ms}

    // DB health (YGG + FRIGG)
    DB_CONNECTION_ERROR,    // {db: "ygg"|"frigg", host, error}

    // Dali config audit
    SOURCE_CREATED,         // {source_id, dialect, schema}
    SOURCE_DELETED,         // {source_id, dialect}

    // Verdandi / LOOM frontend (через Chur POST /api/events)
    LOOM_NODE_SELECTED,     // {node_id, node_type, session_id}
    LOOM_VIEW_LOADED,       // {nodes_count, render_time_ms, level, session_id}
    LOOM_VIEW_SLOW,         // {nodes_count, render_time_ms, level, threshold_ms}

    // Chur BFF auth audit
    AUTH_LOGIN,             // {user_id, tenant_id, success: true|false}
    AUTH_LOGOUT,            // {user_id}
    RATE_LIMIT_EXCEEDED,    // {ip, endpoint, attempts}

    // UA-01: KNOT / LOOM UX analytics (Sprint 6)
    KNOT_SESSION_OPENED,    // {knot_id, knot_type, session_id}
    KNOT_TAB_VIEWED,        // {knot_id, tab_name, session_id}
    LOOM_SEARCH_USED,       // {query_length, results_count, session_id}
    LOOM_FILTER_APPLIED,    // {filter_type, filter_value, session_id}

    // HEIMDALL internal
    DEMO_RESET,
    SNAPSHOT_SAVED,
    REPLAY_STARTED
}
