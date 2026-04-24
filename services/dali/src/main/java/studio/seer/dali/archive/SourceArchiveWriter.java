package studio.seer.dali.archive;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.rest.YggClient;
import studio.seer.dali.storage.FriggCommand;
import studio.seer.shared.FileResult;
import studio.seer.shared.ParseSessionInput;
import studio.seer.tenantrouting.TenantNotAvailableException;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Writes parse session, source file, and error records to hound_src_{tenant} in YGG.
 *
 * All writes are best-effort: any failure logs a warning and returns without
 * propagating the exception — the archive must never affect parse job outcomes.
 *
 * hound_src is a permanent archive and is NOT cleared on clearBeforeWrite=true.
 * That flag only truncates the lineage graph in hound_{tenant}.
 */
@Unremovable
@ApplicationScoped
public class SourceArchiveWriter {

    private static final Logger log = LoggerFactory.getLogger(SourceArchiveWriter.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject @RestClient YggClient yggClient;
    @Inject YggSourceArchiveRegistry sourceRegistry;
    @Inject SourceArchiveSchemaInitializer schemaInitializer;

    // ── Session lifecycle ──────────────────────────────────────────────────────

    /**
     * Opens a DaliParseSession record at the start of a parse job.
     * Returns the generated revision_name for logging.
     */
    public String openSession(String sessionId, ParseSessionInput input, boolean isBatch, long startMs) {
        String tenant = input.tenantAlias();
        String db = resolveDb(tenant);
        if (db == null) return null;
        schemaInitializer.ensureForAlias(tenant);

        String revisionName = "rev-" + LocalDate.now() + "-" + sessionId.substring(0, Math.min(8, sessionId.length()));
        try {
            sql(db,
                "INSERT INTO DaliParseSession SET " +
                "session_id = :sid, revision_name = :rev, tenant_id = :tid, dialect = :dialect, " +
                "source_path = :src, is_batch = :batch, is_preview = :preview, " +
                "clear_before_write = :cbw, datetime_start = :start",
                Map.of(
                    "sid",     sessionId,
                    "rev",     revisionName,
                    "tid",     tenant,
                    "dialect", orEmpty(input.dialect()),
                    "src",     orEmpty(input.source()),
                    "batch",   isBatch,
                    "preview", input.preview(),
                    "cbw",     input.clearBeforeWrite(),
                    "start",   startMs
                ));
            log.debug("[SourceArchive] session opened: sid={} rev={} db={}", sessionId, revisionName, db);
        } catch (Exception e) {
            log.warn("[SourceArchive] openSession failed for {}: {}", sessionId, e.getMessage());
        }
        return revisionName;
    }

    /** Closes a DaliParseSession after successful completion. */
    public void closeSession(String sessionId, ParseSessionInput input,
                             long stopMs, int totalFiles, int successCount, int errorCount,
                             long durationMs, int atomCount, double resolutionRate) {
        String db = resolveDb(input.tenantAlias());
        if (db == null) return;
        try {
            sql(db,
                "UPDATE DaliParseSession SET " +
                "datetime_stop = :stop, total_files = :tf, success_count = :sc, error_count = :ec, " +
                "duration_ms = :dur, atom_count = :atoms, resolution_rate = :rate " +
                "WHERE session_id = :sid",
                Map.of(
                    "stop",  stopMs,
                    "tf",    totalFiles,
                    "sc",    successCount,
                    "ec",    errorCount,
                    "dur",   durationMs,
                    "atoms", atomCount,
                    "rate",  resolutionRate,
                    "sid",   sessionId
                ));
        } catch (Exception e) {
            log.warn("[SourceArchive] closeSession failed for {}: {}", sessionId, e.getMessage());
        }
    }

    /** Marks session as failed (datetime_stop set, error_count=1). */
    public void failSession(String sessionId, ParseSessionInput input, long stopMs, long durationMs) {
        String db = resolveDb(input.tenantAlias());
        if (db == null) return;
        try {
            sql(db,
                "UPDATE DaliParseSession SET datetime_stop = :stop, duration_ms = :dur, error_count = 1 " +
                "WHERE session_id = :sid",
                Map.of("stop", stopMs, "dur", durationMs, "sid", sessionId));
        } catch (Exception e) {
            log.warn("[SourceArchive] failSession update failed for {}: {}", sessionId, e.getMessage());
        }
    }

    // ── File lifecycle ─────────────────────────────────────────────────────────

    /**
     * Writes a DaliSourceFile record for a parsed SQL file/object.
     * Returns the generated source_file_id (needed to link DaliParseError records).
     *
     * @param adapterName "file" for uploads; SKADI adapter name for JDBC ("oracle", "postgresql", …)
     * @param objectName  procedure/function/view name for SKADI; filename for file uploads
     * @param schemaName  DB schema/owner for SKADI; null for file uploads
     */
    public String writeFile(String sessionId, ParseSessionInput input,
                            String filePath, String sqlText,
                            String adapterName, String objectName, String schemaName,
                            long startMs, long stopMs, long durationMs, boolean success) {
        String db = resolveDb(input.tenantAlias());
        if (db == null) return null;
        String fileId   = UUID.randomUUID().toString();
        String hash     = studio.seer.dali.skadi.SourceArchiveService.sha256(sqlText);
        int    sizeBytes = sqlText != null ? sqlText.getBytes(StandardCharsets.UTF_8).length : 0;
        try {
            sql(db,
                "INSERT INTO DaliSourceFile SET " +
                "source_file_id = :fid, session_id = :sid, file_path = :fp, " +
                "object_name = :on, schema_name = :sn, adapter_name = :adapter, " +
                "sql_text = :text, sql_text_hash = :hash, size_bytes = :size, " +
                "datetime_start = :start, datetime_stop = :stop, duration_ms = :dur, success = :ok",
                Map.ofEntries(
                    Map.entry("fid",     fileId),
                    Map.entry("sid",     sessionId),
                    Map.entry("fp",      orEmpty(filePath)),
                    Map.entry("on",      orEmpty(objectName)),
                    Map.entry("sn",      orEmpty(schemaName)),
                    Map.entry("adapter", orEmpty(adapterName)),
                    Map.entry("text",    orEmpty(sqlText)),
                    Map.entry("hash",    hash),
                    Map.entry("size",    sizeBytes),
                    Map.entry("start",   startMs),
                    Map.entry("stop",    stopMs),
                    Map.entry("dur",     durationMs),
                    Map.entry("ok",      success)
                ));
            log.debug("[SourceArchive] file written: fid={} sid={} path={}", fileId, sessionId, filePath);
        } catch (Exception e) {
            log.warn("[SourceArchive] writeFile failed for session={} file={}: {}", sessionId, filePath, e.getMessage());
        }
        return fileId;
    }

    // ── Errors ─────────────────────────────────────────────────────────────────

    /**
     * Writes DaliParseError records for each error string in a FileResult.
     * errorType: "PARSE_ERROR" | "WRITE_ERROR" | "SESSION_ERROR"
     */
    public void writeErrors(String sessionId, ParseSessionInput input,
                            String sourceFileId, String filePath,
                            List<String> errors, String errorType) {
        if (errors == null || errors.isEmpty()) return;
        String db = resolveDb(input.tenantAlias());
        if (db == null) return;
        long now = System.currentTimeMillis();
        for (String errorText : errors) {
            try {
                sql(db,
                    "INSERT INTO DaliParseError SET " +
                    "error_id = :eid, session_id = :sid, source_file_id = :fid, " +
                    "file_path = :fp, error_text = :text, error_type = :type, occurred_at = :ts",
                    Map.of(
                        "eid",  UUID.randomUUID().toString(),
                        "sid",  sessionId,
                        "fid",  orEmpty(sourceFileId),
                        "fp",   orEmpty(filePath),
                        "text", orEmpty(errorText),
                        "type", orEmpty(errorType),
                        "ts",   now
                    ));
            } catch (Exception e) {
                log.warn("[SourceArchive] writeError failed for session={}: {}", sessionId, e.getMessage());
            }
        }
    }

    /** Convenience overload for FileResult errors. */
    public void writeFileErrors(String sessionId, ParseSessionInput input,
                                String sourceFileId, FileResult fr) {
        if (!fr.errors().isEmpty()) {
            writeErrors(sessionId, input, sourceFileId, fr.path(), fr.errors(), "PARSE_ERROR");
        }
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private String resolveDb(String tenantAlias) {
        try {
            return sourceRegistry.resourceFor(tenantAlias).databaseName();
        } catch (TenantNotAvailableException e) {
            log.debug("[SourceArchive] tenant '{}' not in FRIGG — skipping archive write", tenantAlias);
            return null;
        } catch (Exception e) {
            log.warn("[SourceArchive] cannot resolve source archive DB for tenant '{}': {}", tenantAlias, e.getMessage());
            return null;
        }
    }

    private void sql(String db, String query, Map<String, Object> params) {
        yggClient.command(db, schemaInitializer.auth(), new FriggCommand("sql", query, params))
                .await().atMost(TIMEOUT);
    }

    private static String orEmpty(String s) {
        return s != null ? s : "";
    }
}
