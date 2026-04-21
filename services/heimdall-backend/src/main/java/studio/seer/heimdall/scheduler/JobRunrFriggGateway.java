package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import studio.seer.heimdall.snapshot.FriggClient;
import studio.seer.heimdall.snapshot.FriggCommand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Synchronous FRIGG gateway for the {@code frigg-jobrunr} database.
 *
 * HEIMDALL's existing {@link FriggGateway} (snapshot package) is async.
 * {@link ArcadeDbSchedulerStorageProvider} requires blocking calls,
 * so this gateway blocks via {@code .await().atMost(TIMEOUT)}.
 *
 * Connects to the same FRIGG instance as the snapshot gateway
 * (quarkus.rest-client.frigg-api.url) but targets the "frigg-jobrunr" database.
 */
@ApplicationScoped
public class JobRunrFriggGateway {

    private static final Logger LOG = Logger.getLogger(JobRunrFriggGateway.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject @RestClient FriggClient client;

    @ConfigProperty(name = "frigg.user")     String user;
    @ConfigProperty(name = "frigg.password") String password;
    @ConfigProperty(name = "heimdall.scheduler.jobrunr.db", defaultValue = "frigg-jobrunr")
    String db;

    public List<Map<String, Object>> sql(String query, Map<String, Object> params) {
        LOG.debugf("[JR-FRIGG:%s] %s", db, query);
        try {
            var result = client.command(db, basicAuth(), new FriggCommand("sql", query, params))
                    .map(r -> r.result())
                    .onFailure(IOException.class).retry().atMost(1)
                    .await().atMost(TIMEOUT);
            return result != null ? result : List.of();
        } catch (Exception ex) {
            LOG.errorf("[JR-FRIGG:%s] FAILED %s — %s", db, query, ex.getMessage());
            throw ex;
        }
    }

    public List<Map<String, Object>> sql(String query) {
        return sql(query, null);
    }

    public boolean ping() {
        try {
            sql("SELECT 1");
            return true;
        } catch (Exception e) {
            LOG.debugf("[JR-FRIGG] ping failed: %s", e.getMessage());
            return false;
        }
    }

    String db() { return db; }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
