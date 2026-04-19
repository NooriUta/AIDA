package com.skadi.adapters;

import com.skadi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SKADI adapter for ClickHouse via HTTP API.
 *
 * Protocol: java.net.http.HttpClient (JDK built-in — no extra deps).
 * Config.jdbcUrl() is used as the base HTTP URL ("http://host:8123").
 * Hound dialect: "clickhouse"
 *
 * MVP: fetches SQLUserDefined functions only.
 * VIEW / TABLE DDL: TODO (skadi_pg sprint W11-W12).
 */
public class ClickHouseSkadiFetcher implements SkadiFetcher {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseSkadiFetcher.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public SkadiFetchResult fetchScripts(SkadiFetchConfig config) throws SkadiFetchException {
        long startMs = System.currentTimeMillis();
        List<SkadiFetchedFile> files = new ArrayList<>();
        int errors = 0;

        String database = config.extra().getOrDefault("database",
                config.schema() != null ? config.schema() : "default");

        try {
            // MVP: fetch SQL user-defined functions
            if (config.objectTypes().isEmpty()
                    || config.objectTypes().contains(SkadiFetchConfig.ObjectType.FUNCTION)) {
                String body = executeQuery(config, database,
                        "SELECT name, create_query FROM system.functions WHERE origin = 'SQLUserDefined'");
                parseFunctionRows(body, database, files);
            }

            // VIEW / TABLE / MATERIALIZED VIEW: TODO (skadi_pg sprint)
            // SHOW CREATE VIEW :database.:name
            // SHOW CREATE TABLE :database.:name

        } catch (SkadiFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new SkadiFetchException(e.getMessage(), adapterName(), null, e);
        }

        return new SkadiFetchResult(files,
                new SkadiFetchResult.FetchStats(
                        files.size(), errors,
                        System.currentTimeMillis() - startMs,
                        adapterName()));
    }

    @Override
    public boolean ping(SkadiFetchConfig config) {
        try {
            executeQuery(config, "system", "SELECT 1");
            return true;
        } catch (Exception e) {
            log.debug("ClickHouse ping failed for {}: {}", config.jdbcUrl(), e.getMessage());
            return false;
        }
    }

    @Override
    public String adapterName() { return "clickhouse"; }

    @Override
    public void close() {
        httpClient.close();
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private String executeQuery(SkadiFetchConfig config, String database, String sql)
            throws SkadiFetchException {

        String encoded = URLEncoder.encode(sql, StandardCharsets.UTF_8);
        URI uri = URI.create(config.jdbcUrl() + "/?database=" + database);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("X-ClickHouse-User", config.user())
                .header("X-ClickHouse-Key",  config.password())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString("query=" + encoded))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String body = response.body();
                if (response.statusCode() == 401 || response.statusCode() == 403)
                    throw new SkadiFetchPermissionException(
                            "HTTP " + response.statusCode() + ": " + body, adapterName(), null, null);
                throw new SkadiFetchException(
                        "HTTP " + response.statusCode() + ": " + body, adapterName(), null);
            }
            return response.body();
        } catch (IOException e) {
            throw new SkadiFetchConnectionException(e.getMessage(), adapterName(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SkadiFetchException("Interrupted", adapterName(), null, e);
        }
    }

    /** Parse TSV response from ClickHouse: name\tcreate_query\n */
    private void parseFunctionRows(String tsv, String database, List<SkadiFetchedFile> files) {
        for (String line : tsv.split("\n")) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", 2);
            if (parts.length < 2) continue;
            String name = parts[0].trim();
            String ddl  = parts[1].trim();
            files.add(new SkadiFetchedFile(name, database,
                    SkadiFetchConfig.ObjectType.FUNCTION, ddl,
                    null, Map.of("database", database)));
        }
    }
}
