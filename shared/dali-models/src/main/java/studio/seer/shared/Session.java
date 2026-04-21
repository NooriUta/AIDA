package studio.seer.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @param instanceId     Dali instance tag (from {@code dali.instance.id} config).
 *                       Null for untagged sessions (pre-multi-instance, backward-compat).
 *                       Used to isolate sessions when multiple Dali instances share one FRIGG.
 * @param dbName         Database name supplied by the user (optional). When non-null, Hound
 *                       creates a DaliDatabase vertex and attaches CONTAINS_SCHEMA edges.
 *                       Null for ad-hoc sessions where the user did not specify a database name.
 * @param tenantAlias    Tenant alias resolved from X-Seer-Tenant-Alias header. Defaults to
 *                       "default" for legacy / single-tenant sessions. Determines which
 *                       dali_{alias} ArcadeDB database this session is stored in.
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
        boolean              friggPersisted,  // true = record confirmed written to FRIGG
        String               instanceId,      // Dali instance tag — null = untagged
        String               dbName,         // optional DB grouping label — null = ad-hoc
        String               tenantAlias     // tenant alias — "default" for single-tenant
) {
    @JsonCreator
    public static Session of(
            @JsonProperty("id")               String         id,
            @JsonProperty("status")           SessionStatus  status,
            @JsonProperty("progress")         int            progress,
            @JsonProperty("total")            int            total,
            @JsonProperty("batch")            boolean        batch,
            @JsonProperty("clearBeforeWrite") boolean        clearBeforeWrite,
            @JsonProperty("dialect")          String         dialect,
            @JsonProperty("source")           String         source,
            @JsonProperty("startedAt")        Instant        startedAt,
            @JsonProperty("updatedAt")        Instant        updatedAt,
            @JsonProperty("atomCount")        Integer        atomCount,
            @JsonProperty("vertexCount")      Integer        vertexCount,
            @JsonProperty("edgeCount")        Integer        edgeCount,
            @JsonProperty("droppedEdgeCount") Integer        droppedEdgeCount,
            @JsonProperty("vertexStats")      List<VertexTypeStat> vertexStats,
            @JsonProperty("resolutionRate")   Double         resolutionRate,
            @JsonProperty("durationMs")       Long           durationMs,
            @JsonProperty("warnings")         List<String>   warnings,
            @JsonProperty("errors")           List<String>   errors,
            @JsonProperty("fileResults")      List<FileResult> fileResults,
            @JsonProperty("friggPersisted")   boolean        friggPersisted,
            @JsonProperty("instanceId")       String         instanceId,
            @JsonProperty("dbName")           String         dbName,
            @JsonProperty("tenantAlias")      String         tenantAlias) {
        return new Session(id, status, progress, total, batch, clearBeforeWrite,
                dialect, source, startedAt, updatedAt,
                atomCount, vertexCount, edgeCount, droppedEdgeCount,
                vertexStats != null ? vertexStats : List.of(),
                resolutionRate, durationMs,
                warnings    != null ? warnings    : List.of(),
                errors      != null ? errors      : List.of(),
                fileResults != null ? fileResults : List.of(),
                friggPersisted, instanceId, dbName,
                tenantAlias != null ? tenantAlias : "default");
    }
}
