package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import studio.seer.dali.service.SessionService;
import studio.seer.shared.ParseSessionInput;
import studio.seer.tenantrouting.TenantContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Handles multipart file uploads for parse sessions.
 *
 * <pre>
 * POST /api/sessions/upload   multipart/form-data
 *   file             — ZIP archive or a single .sql / .pck / … file  (required)
 *   dialect          — SQL dialect: plsql | postgresql | clickhouse   (required)
 *   preview          — boolean, default false
 *   clearBeforeWrite — boolean, default true
 * </pre>
 *
 * <p>The uploaded file is extracted into a JVM temp directory and passed to
 * {@link SessionService#enqueue} with {@code uploaded=true}.  {@link studio.seer.dali.job.ParseJob}
 * deletes the temp directory once the session completes or fails.
 */
@jakarta.ws.rs.Path("/api/sessions/upload")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class FileUploadResource {

    static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".sql", ".pck", ".prc", ".pkb", ".pks", ".fnc", ".trg", ".vw", ".zip", ".rar"
    );

    @Inject SessionService sessionService;
    @Inject TenantContext  tenantCtx;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(
            @RestForm("file")             FileUpload file,
            @RestForm("dialect")          String     dialect,
            @RestForm("preview")          @DefaultValue("false") boolean preview,
            @RestForm("clearBeforeWrite") @DefaultValue("true")  boolean clearBeforeWrite,
            @RestForm("dbName")           @DefaultValue("")       String  dbName,
            @RestForm("appName")          @DefaultValue("")       String  appName
    ) {
        if (file == null || file.fileName() == null || file.fileName().isBlank()) {
            return bad("file is required");
        }
        if (dialect == null || dialect.isBlank()) {
            return bad("dialect is required");
        }

        String originalName = file.fileName().toLowerCase();
        boolean isZip = originalName.endsWith(".zip");
        boolean isRar = originalName.endsWith(".rar");
        boolean isSql = ALLOWED_EXTENSIONS.stream()
                .filter(e -> !e.equals(".zip") && !e.equals(".rar"))
                .anyMatch(originalName::endsWith);

        if (!isZip && !isRar && !isSql) {
            return bad("unsupported file type: " + file.fileName()
                    + " — accepted: " + ALLOWED_EXTENSIONS);
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("dali-upload-");

            if (isZip) {
                UploadExtractor.extractZip(file.uploadedFile(), tempDir);
            } else if (isRar) {
                UploadExtractor.extractRar(file.uploadedFile(), tempDir);
            } else {
                String safeName = Path.of(file.fileName()).getFileName().toString();
                Files.copy(file.uploadedFile(), tempDir.resolve(safeName));
            }

            ParseSessionInput input = new ParseSessionInput(
                    dialect.strip(), tempDir.toString(), preview, clearBeforeWrite, true,
                    null, null, null,
                    dbName  != null && !dbName.isBlank()  ? dbName.strip()  : null,
                    appName != null && !appName.isBlank() ? appName.strip() : null,
                    tenantCtx.tenantAlias());

            try {
                return Response.accepted(sessionService.enqueue(input)).build();
            } catch (IllegalStateException e) {
                deleteTempDir(tempDir);
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\":\"" + e.getMessage() + "\"}")
                        .build();
            }

        } catch (WebApplicationException e) {
            deleteTempDir(tempDir);
            throw e;
        } catch (IOException e) {
            deleteTempDir(tempDir);
            return Response.serverError()
                    .entity("{\"error\":\"Failed to process upload: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static Response bad(String msg) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"" + msg + "\"}")
                .build();
    }

    private static void deleteTempDir(Path dir) {
        if (dir == null || !Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
    }
}
