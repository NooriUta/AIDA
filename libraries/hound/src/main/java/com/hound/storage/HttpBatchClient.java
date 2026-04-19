// File: src/main/java/com/hound/storage/HttpBatchClient.java
package com.hound.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

/**
 * Отправляет NDJSON payload к ArcadeDB HTTP Batch endpoint.
 *
 * <p>Один инстанс на весь {@link ArcadeDBSemanticWriter} (thread-safe — HttpClient thread-safe).
 */
public class HttpBatchClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpBatchClient.class);
    private static final int MAX_RETRIES = 3;

    private final HttpClient http;
    private final String baseUrl;
    private final String authHeader;
    private final boolean gzip;

    public HttpBatchClient(String host, int port, String dbName, String user, String password) {
        this(host, port, dbName, user, password, false);
    }

    public HttpBatchClient(String host, int port, String dbName, String user, String password, boolean gzip) {
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.baseUrl = "http://" + host + ":" + port + "/api/v1/batch/" + dbName
                + "?lightEdges=true&wal=false&parallelFlush=true&batchSize=100000";
        String creds = user + ":" + password;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        this.gzip = gzip;
    }

    /**
     * Отправляет payload. При ошибке — retry с exponential backoff (1s, 2s, 4s).
     *
     * @param payload NDJSON строка
     * @param sid     session_id для логирования
     * @throws RuntimeException если все 3 попытки неуспешны
     */
    public void send(String payload, String sid) {
        byte[] body = gzip ? compress(payload) : payload.getBytes(StandardCharsets.UTF_8);

        Exception lastEx = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest.Builder rb = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl))
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/x-ndjson");
                if (gzip) rb.header("Content-Encoding", "gzip");
                HttpRequest req = rb.POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();

                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    logger.debug("Batch OK sid={} payload={}b gzip={}b status={}",
                            sid, payload.length(), body.length, resp.statusCode());
                    return;
                }

                if (resp.statusCode() < 500) {
                    // 4xx — not retryable
                    logger.warn("Batch HTTP {} (non-retryable) attempt {}/{} sid={}: {}",
                            resp.statusCode(), attempt, MAX_RETRIES, sid, resp.body());
                    break;
                }

                // 5xx — retryable; detect MVCC specifically for observability
                boolean isMvcc = resp.body() != null &&
                        resp.body().contains("Concurrent modification");
                if (isMvcc) {
                    logger.warn("Batch MVCC conflict attempt {}/{} sid={} — ArcadeDB page locked, will retry",
                            attempt, MAX_RETRIES, sid);
                } else {
                    logger.warn("Batch HTTP {} attempt {}/{} sid={}: {}",
                            resp.statusCode(), attempt, MAX_RETRIES, sid, resp.body());
                }
                lastEx = new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());

            } catch (Exception e) {
                lastEx = e;
                logger.warn("Batch attempt {}/{} sid={} error: {}", attempt, MAX_RETRIES, sid, e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                try { Thread.sleep(1000L << (attempt - 1)); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new RuntimeException("Batch failed after " + MAX_RETRIES + " attempts sid=" + sid, lastEx);
    }

    private static byte[] compress(String s) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(s.length() / 2);
            try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
                gz.write(s.getBytes(StandardCharsets.UTF_8));
            }
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("gzip failed", e);
        }
    }
}
