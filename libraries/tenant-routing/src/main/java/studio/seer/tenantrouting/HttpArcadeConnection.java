package studio.seer.tenantrouting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * HTTP-based implementation of {@link ArcadeConnection} using ArcadeDB REST API.
 * Thread-safe; the backing {@link HttpClient} is reused across requests.
 *
 * Endpoint: POST /api/v1/query/{database}
 * Body:     {"language":"cypher","command":"...","params":{...}}
 */
public class HttpArcadeConnection implements ArcadeConnection {

    private static final Logger log = LoggerFactory.getLogger(HttpArcadeConnection.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http;
    private final String baseUrl;
    private final String dbName;
    private final String authHeader;

    public HttpArcadeConnection(HttpClient http, String baseUrl, String dbName,
                                String username, String password) {
        this.http       = http;
        this.baseUrl    = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.dbName     = dbName;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String databaseName() { return dbName; }

    @Override
    public List<Map<String, Object>> sql(String query, Map<String, Object> params) {
        return execute("sql", query, params);
    }

    @Override
    public List<Map<String, Object>> cypher(String query, Map<String, Object> params) {
        return execute("cypher", query, params);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> execute(String language, String command,
                                               Map<String, Object> params) {
        try {
            var body = MAPPER.writeValueAsString(Map.of(
                    "language", language,
                    "command", command,
                    "params", params != null ? params : Map.of()
            ));
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/query/" + dbName))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("ArcadeDB query failed [" + response.statusCode() + "]: "
                        + response.body());
            }
            var parsed = MAPPER.readValue(response.body(), Map.class);
            var result = parsed.get("result");
            return result instanceof List ? (List<Map<String, Object>>) result : List.of();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("ArcadeDB {} query failed on db={}: {}", language, dbName, e.getMessage());
            throw new RuntimeException("ArcadeDB query error", e);
        }
    }
}
