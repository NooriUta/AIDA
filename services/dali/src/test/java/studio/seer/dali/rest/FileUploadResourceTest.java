package studio.seer.dali.rest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import studio.seer.dali.storage.SessionRepository;
import studio.seer.shared.SessionStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@link FileUploadResource} — POST /api/sessions/upload.
 *
 * <p>Tests verify HTTP status codes and basic response shape. Actual parse execution
 * is async; these tests only assert that the session is accepted (202 QUEUED).
 */
@QuarkusTest
class FileUploadResourceTest {

    private static final String UPLOAD_URL = "/api/sessions/upload";

    @Inject
    SessionRepository repository;

    private static RequestSpecification withTenant() {
        return given().header("X-Seer-Tenant-Alias", "default");
    }

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    // ── Happy paths ────────────────────────────────────────────────────────────

    @Test
    void upload_singleSqlFile_returns202() throws IOException {
        Path sqlFile = createTempSql("SELECT 1 FROM dual;");
        try {
            withTenant()
                .multiPart("file", sqlFile.toFile(), "application/octet-stream")
                .multiPart("dialect", "plsql")
                .multiPart("preview", "true")
            .when()
                .post(UPLOAD_URL)
            .then()
                .statusCode(202)
                .body("id",     notNullValue())
                .body("status", equalTo(SessionStatus.QUEUED.name()));
        } finally {
            Files.deleteIfExists(sqlFile);
        }
    }

    @Test
    void upload_zipWithSqlFiles_returns202() throws IOException {
        byte[] zip = createZip("schema.sql", "CREATE TABLE t (id NUMBER);");
        withTenant()
            .multiPart("file", "archive.zip", zip, "application/zip")
            .multiPart("dialect", "plsql")
            .multiPart("preview", "true")
        .when()
            .post(UPLOAD_URL)
        .then()
            .statusCode(202)
            .body("id",     notNullValue())
            .body("status", equalTo(SessionStatus.QUEUED.name()));
    }

    // ── Validation errors ──────────────────────────────────────────────────────

    @Test
    void upload_missingFile_returns400() {
        withTenant()
            .multiPart("dialect", "plsql")
        .when()
            .post(UPLOAD_URL)
        .then()
            .statusCode(400);
    }

    @Test
    void upload_missingDialect_returns400() throws IOException {
        Path sqlFile = createTempSql("SELECT 1 FROM dual;");
        try {
            withTenant()
                .multiPart("file", sqlFile.toFile(), "application/octet-stream")
            .when()
                .post(UPLOAD_URL)
            .then()
                .statusCode(400)
                .body("error", containsString("dialect"));
        } finally {
            Files.deleteIfExists(sqlFile);
        }
    }

    @Test
    void upload_unsupportedExtension_returns400() throws IOException {
        Path exe = Files.createTempFile("malicious-", ".exe");
        Files.writeString(exe, "not-an-sql");
        try {
            withTenant()
                .multiPart("file", exe.toFile(), "application/octet-stream")
                .multiPart("dialect", "plsql")
            .when()
                .post(UPLOAD_URL)
            .then()
                .statusCode(400)
                .body("error", containsString("unsupported file type"));
        } finally {
            Files.deleteIfExists(exe);
        }
    }

    @Test
    void upload_zipWithPathTraversal_returns400() throws IOException {
        byte[] zip = createZip("../evil.sh", "rm -rf /");
        withTenant()
            .multiPart("file", "traversal.zip", zip, "application/zip")
            .multiPart("dialect", "plsql")
        .when()
            .post(UPLOAD_URL)
        .then()
            .statusCode(400)
            .body("error", containsString("illegal path"));
    }

    @Test
    void upload_zipWithNoSqlFiles_returns400() throws IOException {
        byte[] zip = createZip("readme.txt", "just text");
        withTenant()
            .multiPart("file", "nosql.zip", zip, "application/zip")
            .multiPart("dialect", "plsql")
        .when()
            .post(UPLOAD_URL)
        .then()
            .statusCode(400)
            .body("error", containsString("no SQL files"));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static Path createTempSql(String content) throws IOException {
        Path f = Files.createTempFile("dali-upload-test-", ".sql");
        Files.writeString(f, content);
        return f;
    }

    private static byte[] createZip(String entryName, String content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return bos.toByteArray();
    }
}
