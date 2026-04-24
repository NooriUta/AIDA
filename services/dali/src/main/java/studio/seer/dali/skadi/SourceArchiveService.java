package studio.seer.dali.skadi;

import com.hound.api.SqlSource;
import com.skadi.SkadiFetchedFile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Archives SKADI-fetched DDL objects and returns only the changed files
 * as {@link SqlSource.FromText} instances ready for HoundParser.
 *
 * <p><b>MVP behaviour</b>: all fetched files are considered "changed" — no persistent
 * comparison layer yet. Phase 2 will compare SHA-256 hashes against stored records in
 * {@code hound_src_{alias}} via {@link YggSourceArchiveRegistry}.
 *
 * <p>Zero disk I/O: SQL text stays in memory from SKADI harvest through Hound parse.
 */
@ApplicationScoped
public class SourceArchiveService {

    private static final Logger log = LoggerFactory.getLogger(SourceArchiveService.class);

    @Inject YggSourceArchiveRegistry sourceArchiveRegistry;

    /**
     * DMT-05/06: Converts fetched SKADI files to {@link SqlSource.FromText} instances
     * for the given tenant. The source archive DB ({@code hound_src_{alias}}) is resolved
     * via {@link YggSourceArchiveRegistry} — currently returns the same single-pool connection
     * in Phase 1 (single-tenant demo mode).
     *
     * @param tenantAlias tenant alias — used to route to the correct source archive DB
     * @param files       list of DDL files returned by SKADI
     * @return ordered list of SQL sources to pass to HoundParser
     */
    public List<SqlSource> upsertAll(String tenantAlias, List<SkadiFetchedFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        // Phase 1: all files forwarded; Phase 2: filter by hash against hound_src_{alias}
        String archiveDb = sourceArchiveRegistry.resourceFor(tenantAlias).databaseName();
        List<SqlSource> sources = files.stream()
                .filter(f -> f.sqlText() != null && !f.sqlText().isBlank())
                .map(f -> (SqlSource) new SqlSource.FromText(f.sqlText(), f.suggestedFilename()))
                .toList();

        log.info("SourceArchiveService.upsertAll: tenant={} db={} {} files in → {} sources out (MVP: all forwarded)",
                tenantAlias, archiveDb, files.size(), sources.size());
        return sources;
    }

    /** Backward-compat overload — uses "default" tenant. */
    public List<SqlSource> upsertAll(List<SkadiFetchedFile> files) {
        return upsertAll("default", files);
    }

    /**
     * Computes SHA-256 hex digest of a SQL text string.
     *
     * <p>Used for deduplication: compare against stored {@code sqlTextHash} in DaliSourceFile.
     * If hashes match, the object was not modified since the last harvest and can be skipped.
     *
     * @param sqlText the DDL text (may be null)
     * @return 64-character lowercase hex string, or empty string for null input
     */
    public static String sha256(String sqlText) {
        if (sqlText == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sqlText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
