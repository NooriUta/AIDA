package studio.seer.shared;

import java.time.Instant;
import java.util.List;

/**
 * Represents a Dali parse session tracked by JobRunr.
 *
 * @param id             Unique session identifier (UUID)
 * @param status         Current lifecycle state
 * @param progress       Files processed so far (0 until RUNNING)
 * @param total          Total files to parse (0 = single-file, set when batch starts)
 * @param batch          True when source is a directory (multiple files)
 * @param dialect        SQL dialect used for parsing
 * @param source         Source path submitted by the user (file or directory)
 * @param startedAt      When the session was enqueued
 * @param updatedAt      Last status update timestamp
 * @param atomCount      Atoms extracted — null until COMPLETED
 * @param vertexCount    Vertices written to YGG — null until COMPLETED
 * @param edgeCount      Edges written to YGG — null until COMPLETED
 * @param resolutionRate Column-level resolution rate [0.0–1.0] — null until COMPLETED
 * @param durationMs     Wall-clock parse duration in ms — null until COMPLETED
 * @param warnings       Non-fatal warnings — empty until COMPLETED
 * @param errors         Fatal errors — empty until COMPLETED
 * @param fileResults    Per-file breakdown for batch sessions — empty for single-file
 */
public record Session(
        String         id,
        SessionStatus  status,
        int            progress,
        int            total,
        boolean        batch,
        String         dialect,
        String         source,
        Instant        startedAt,
        Instant        updatedAt,
        Integer        atomCount,
        Integer        vertexCount,
        Integer        edgeCount,
        Double         resolutionRate,
        Long           durationMs,
        List<String>   warnings,
        List<String>   errors,
        List<FileResult> fileResults
) {}
