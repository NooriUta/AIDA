package studio.seer.heimdall.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
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
import java.util.Map;

/**
 * MTN-64 — Nightly purge of {@code UserSessionEvents} older than
 * 180 days (Q-UA-3: NIS2 + ISO 27001 A.12.4 retention baseline).
 *
 * <p>Runs via Quarkus Scheduler at 03:30 UTC (after the 03:00 daily
 * {@code OrgDriftReconcileJob} to not dogpile FRIGG). Emits
 * {@code seer.audit.user_session_events_purged} with the delete count so
 * operators see the purge happened even when no events were aged out.
 */
@ApplicationScoped
public class UserSessionEventsPurgeJob {

    private static final Logger LOG = Logger.getLogger(UserSessionEventsPurgeJob.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long RETENTION_MS = 180L * 24 * 60 * 60 * 1000;  // 180 days

    @ConfigProperty(name = "frigg.url",   defaultValue = "http://localhost:2481")
    String friggUrl;
    @ConfigProperty(name = "frigg.user",  defaultValue = "root")
    String friggUser;
    @ConfigProperty(name = "frigg.password", defaultValue = "playwithdata")
    String friggPassword;
    @ConfigProperty(name = "heimdall.mtn64.users-db", defaultValue = "frigg-users")
    String friggUsersDb;

    @Scheduled(cron = "0 30 3 * * ?", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void runDaily() {
        long threshold = System.currentTimeMillis() - RETENTION_MS;
        long deleted = purgeBefore(threshold);
        LOG.infof("[MTN-64] UserSessionEvents purge: threshold=%d deleted=%d", threshold, deleted);
        // Emit audit through system log (MTN-21 SIEM exporter picks up WARN/INFO already)
        LOG.infof("[seer.audit.user_session_events_purged] deleted=%d retentionMs=%d", deleted, RETENTION_MS);
    }

    long purgeBefore(long thresholdMs) {
        try {
            String body = MAPPER.writeValueAsString(Map.of(
                    "language", "sql",
                    "command",  "DELETE FROM UserSessionEvents WHERE ts < :threshold",
                    "params",   Map.of("threshold", thresholdMs)));
            String basic = Base64.getEncoder().encodeToString(
                    (friggUser + ":" + friggPassword).getBytes(StandardCharsets.UTF_8));
            HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(friggUrl + "/api/v1/command/" + friggUsersDb))
                    .header("Content-Type",  "application/json")
                    .header("Authorization", "Basic " + basic)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                LOG.warnf("[MTN-64] purge HTTP %d: %s", res.statusCode(), res.body());
                return -1;
            }
            // ArcadeDB returns {result: [{count: N}]} for DELETE
            var tree = MAPPER.readTree(res.body());
            var result = tree.path("result");
            if (result.isArray() && result.size() > 0) {
                return result.get(0).path("count").asLong(0);
            }
            return 0;
        } catch (Exception e) {
            LOG.warnf("[MTN-64] purge failed: %s", e.getMessage());
            return -1;
        }
    }
}
