package studio.seer.dali.skadi;

import com.hound.api.SqlSource;
import com.skadi.SkadiFetchedFile;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * comparison layer yet. Future implementation will compare SHA-256 hashes against
 * stored {@code sqlTextHash} in {@code DaliSourceFile} documents (YGG {@code hound_src_<tenantId>}).
 *
 * <p>Zero disk I/O: SQL text stays in memory from SKADI harvest through Hound parse.
 */
@ApplicationScoped
public class SourceArchiveService {

    private static final Logger log = LoggerFactory.getLogger(SourceArchiveService.class);

    /**
     * Converts fetched SKADI files to {@link SqlSource.FromText} instances.
     *
     * <p>Files with blank SQL text are silently skipped (should not occur in practice
     * unless the source DB object has an empty definition).
     *
     * <p>MVP: returns all non-blank files. Full implementation will filter by hash change.
     *
     * @param files list of DDL files returned by {@code SkadiFetcher.fetchScripts()}
     * @return ordered list of SQL sources to pass to {@code HoundParser.parseSources()}
     */
    public List<SqlSource> upsertAll(List<SkadiFetchedFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        List<SqlSource> sources = files.stream()
                .filter(f -> f.sqlText() != null && !f.sqlText().isBlank())
                .map(f -> (SqlSource) new SqlSource.FromText(f.sqlText(), f.suggestedFilename()))
                .toList();

        log.info("SourceArchiveService.upsertAll: {} files in → {} sources out (MVP: all forwarded)",
                files.size(), sources.size());
        return sources;
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
