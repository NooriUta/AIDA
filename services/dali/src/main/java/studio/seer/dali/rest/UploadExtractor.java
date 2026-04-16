package studio.seer.dali.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility for extracting uploaded ZIP archives into a target directory.
 *
 * <p>Safeguards:
 * <ul>
 *   <li>Path traversal — rejects entries with {@code ..} or absolute paths</li>
 *   <li>ZIP bomb — rejects archives exceeding {@link #MAX_FILES} entries or
 *       {@link #MAX_UNCOMPRESSED_BYTES} total uncompressed size</li>
 *   <li>Extension whitelist — silently skips non-SQL files inside the archive</li>
 * </ul>
 */
class UploadExtractor {

    static final int  MAX_FILES             = 2000;
    static final long MAX_UNCOMPRESSED_BYTES = 500L * 1024 * 1024; // 500 MB

    static final Set<String> SQL_EXTENSIONS = Set.of(
            ".sql", ".pck", ".prc", ".pkb", ".pks", ".fnc", ".trg", ".vw"
    );

    private UploadExtractor() {}

    /**
     * Extracts all SQL files from {@code zipFile} into {@code targetDir}.
     *
     * @param zipFile   path to the uploaded ZIP (Quarkus temp file)
     * @param targetDir pre-created destination directory
     * @throws WebApplicationException 400 for path traversal / too many files / no SQL files found
     * @throws WebApplicationException 413 for uncompressed size exceeding the limit
     * @throws IOException             on I/O failure during extraction
     */
    static void extractZip(Path zipFile, Path targetDir) throws IOException {
        int  fileCount  = 0;
        long totalBytes = 0;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                String name = entry.getName();
                guardPathTraversal(name);

                // Only extract SQL-family files
                String lower = name.toLowerCase();
                boolean sqlFile = SQL_EXTENSIONS.stream().anyMatch(lower::endsWith);
                if (!sqlFile) {
                    zis.closeEntry();
                    continue;
                }

                if (++fileCount > MAX_FILES) {
                    throw bad("ZIP exceeds maximum of " + MAX_FILES + " SQL files");
                }

                // Use only the filename (no subdirectory structure in target)
                String safeName = Path.of(name).getFileName().toString();
                Path target = uniquePath(targetDir, safeName, fileCount);

                byte[] buf = new byte[8192];
                int read;
                try (OutputStream out = Files.newOutputStream(target)) {
                    while ((read = zis.read(buf)) != -1) {
                        totalBytes += read;
                        if (totalBytes > MAX_UNCOMPRESSED_BYTES) {
                            throw entity(413, "ZIP uncompressed size exceeds 500 MB limit");
                        }
                        out.write(buf, 0, read);
                    }
                }
                zis.closeEntry();
            }
        }

        if (fileCount == 0) {
            throw bad("ZIP contains no SQL files (accepted extensions: " + SQL_EXTENSIONS + ")");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static void guardPathTraversal(String name) {
        if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
            throw bad("ZIP entry contains illegal path: " + name);
        }
    }

    /** Returns {@code dir/name}, appending {@code _N} before the extension if the path already exists. */
    private static Path uniquePath(Path dir, String name, int counter) {
        Path candidate = dir.resolve(name);
        if (!Files.exists(candidate)) return candidate;
        int dot = name.lastIndexOf('.');
        String base = dot >= 0 ? name.substring(0, dot) : name;
        String ext  = dot >= 0 ? name.substring(dot)    : "";
        return dir.resolve(base + "_" + counter + ext);
    }

    private static WebApplicationException bad(String msg) {
        return entity(400, msg);
    }

    private static WebApplicationException entity(int status, String msg) {
        return new WebApplicationException(
                Response.status(status)
                        .entity("{\"error\":\"" + msg + "\"}")
                        .type("application/json")
                        .build());
    }
}
