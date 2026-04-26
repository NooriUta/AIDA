package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * HTA-11/12: Low-level FRIGG/ArcadeDB database admin used by the archive/restore jobs.
 *
 * Minimal HTTP client that calls ArcadeDB's {@code /api/v1/server} admin endpoints
 * to drop and create databases. Isolated from domain code so jobs stay focused.
 */
@ApplicationScoped
public class TenantDatabaseAdmin {

    private static final Logger LOG = Logger.getLogger(TenantDatabaseAdmin.class);

    @ConfigProperty(name = "frigg.url",  defaultValue = "http://localhost:2481") String friggUrl;
    @ConfigProperty(name = "frigg.user", defaultValue = "root")                   String friggUser;
    @ConfigProperty(name = "frigg.pass", defaultValue = "playwithdata")           String friggPass;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public void dropDatabase(String dbName) {
        callServerCommand("drop database " + dbName);
    }

    public void createDatabase(String dbName) {
        callServerCommand("create database " + dbName);
    }

    private void callServerCommand(String command) {
        String body = "{\"command\":\"" + command.replace("\"", "\\\"") + "\"}";
        HttpRequest req = HttpRequest.newBuilder(URI.create(friggUrl.replaceAll("/$", "") + "/api/v1/server"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((friggUser + ":" + friggPass).getBytes(StandardCharsets.UTF_8)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 300) {
                throw new RuntimeException("FRIGG " + res.statusCode() + " for \"" + command + "\": " + res.body());
            }
            LOG.infof("[TenantDbAdmin] %s → %d", command, res.statusCode());
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("FRIGG admin call failed: " + command, e);
        }
    }
}
