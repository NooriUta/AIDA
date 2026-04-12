package studio.seer.shared;

import java.time.Instant;

/**
 * Represents a Dali parse session tracked by JobRunr.
 *
 * @param id        Unique session identifier (UUID)
 * @param status    Current lifecycle state
 * @param progress  Files processed so far
 * @param total     Total files in this session (0 = unknown)
 * @param dialect   SQL dialect used for parsing
 * @param startedAt When the session was enqueued
 * @param updatedAt Last status update timestamp
 */
public record Session(
        String        id,
        SessionStatus status,
        int           progress,
        int           total,
        String        dialect,
        Instant       startedAt,
        Instant       updatedAt
) {}
