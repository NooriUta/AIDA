package com.mimir.persistence;

import java.time.Instant;
import java.util.List;

/**
 * MimirSession — per-question persistent record in FRIGG.
 *
 * <p>Stored in {@code frigg-mimir-sessions} database (separate from {@code frigg-sessions}
 * which holds Chur HTTP sessions). Per-tenant filtering via {@code tenantAlias} column.
 *
 * <p>{@code pauseState} is {@code Object} — typed as {@code HilPauseState} when MT-08 lands;
 * for now MIMIR_CORE leaves it null (HiL gate stub).
 *
 * <p>Schema (ArcadeDB SQL):
 * <pre>{@code
 * CREATE VERTEX TYPE MimirSession;
 * CREATE PROPERTY MimirSession.sessionId STRING;
 * CREATE PROPERTY MimirSession.tenantAlias STRING;
 * CREATE PROPERTY MimirSession.status STRING;          -- "active" | "completed" | "paused" | "failed"
 * CREATE PROPERTY MimirSession.toolCallsUsed JSON;     -- List<String>
 * CREATE PROPERTY MimirSession.highlightIds JSON;      -- List<String>
 * CREATE PROPERTY MimirSession.pauseState JSON;        -- nullable (TIER2 MT-08 HiL)
 * CREATE PROPERTY MimirSession.createdAt DATETIME;
 * CREATE PROPERTY MimirSession.updatedAt DATETIME;
 * CREATE INDEX MimirSession.sessionId ON MimirSession (sessionId) UNIQUE;
 * CREATE INDEX MimirSession.tenantAlias ON MimirSession (tenantAlias) NOTUNIQUE;
 * }</pre>
 */
public record MimirSession(
        String       sessionId,
        String       tenantAlias,
        String       status,
        List<String> toolCallsUsed,
        List<String> highlightIds,
        Object       pauseState,
        Instant      createdAt,
        Instant      updatedAt
) {

    public static MimirSession completed(String sessionId, String tenantAlias,
                                          List<String> toolCalls, List<String> highlights) {
        Instant now = Instant.now();
        return new MimirSession(sessionId, tenantAlias, "completed",
                toolCalls == null ? List.of() : toolCalls,
                highlights == null ? List.of() : highlights,
                null, now, now);
    }

    public static MimirSession failed(String sessionId, String tenantAlias) {
        Instant now = Instant.now();
        return new MimirSession(sessionId, tenantAlias, "failed",
                List.of(), List.of(), null, now, now);
    }
}
