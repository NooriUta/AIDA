package studio.seer.shared;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Dali parse session tracked by JobRunr.
 *
 * @param id             Unique session identifier (UUID)
 * @param status         Current lifecycle state
 * @param progress       Files processed so far (0 until RUNNING)
 * @param total          Total files to parse (0 = single-file, set when batch starts)
 * @param batch          True when source is a directory (multiple files)
 * @param clearBeforeWrite When true, all existing graph data is wiped before writing
 * @param dialect        SQL dialect used for parsing
 * @param source         Source path submitted by the user (file or directory)
 * @param startedAt      When the session was enqueued
 * @param updatedAt      Last status update timestamp
 * @param atomCount        DaliAtom vertices written — null until COMPLETED
 * @param vertexCount      Total vertices written to YGG — null until COMPLETED
 * @param edgeCount        Edges written to YGG — null until COMPLETED
 * @param droppedEdgeCount Edges dropped (unresolvable endpoints) — null until COMPLETED
 * @param vertexStats      Per-type breakdown (inserted + duplicate) — empty until COMPLETED
 * @param resolutionRate   Column-level semantic resolution rate [0.0–1.0] — null until COMPLETED
 * @param durationMs     Wall-clock parse duration in ms — null until COMPLETED
 * @param warnings       Non-fatal warnings — empty until COMPLETED
 * @param errors         Fatal errors — empty until COMPLETED
 * @param fileResults    Per-file breakdown for batch sessions — empty for single-file
 * @param friggPersisted True when this session record has been successfully written to FRIGG.
 *                       False if FRIGG was unavailable or the save has not been attempted yet.
 */
public record Session(
        String         id,
        SessionStatus  status,
        int            progress,
        int            total,
        boolean        batch,
        boolean        clearBeforeWrite,
        String         dialect,
        String         source,
        Instant        startedAt,
        Instant        updatedAt,
        Integer              atomCount,
        Integer              vertexCount,
        Integer              edgeCount,
        Integer              droppedEdgeCount,
        List<VertexTypeStat> vertexStats,
        Double               resolutionRate,
        Long                 durationMs,
        List<String>         warnings,
        List<String>         errors,
        List<FileResult>     fileResults,
        boolean              friggPersisted   // true = record confirmed written to FRIGG
) {}
