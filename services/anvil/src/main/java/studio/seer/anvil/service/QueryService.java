package studio.seer.anvil.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.model.*;
import studio.seer.anvil.rest.YggQueryClient;
import studio.seer.anvil.util.YggUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class QueryService {

    @Inject
    @RestClient
    YggQueryClient ygg;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    public QueryResult execute(String language, String query, String dbName) {
        String db   = dbName != null && !dbName.isBlank() ? dbName : defaultDb;
        String auth = YggUtil.basicAuth(yggUser, yggPassword);
        long   start = System.currentTimeMillis();

        List<Map<String, Object>> rows = ygg.query(db, auth, new YggCommand(language, query, Map.of()))
                                            .await().indefinitely()
                                            .result();
        if (rows == null) rows = List.of();

        boolean hasMore = rows.size() >= 500;
        List<Map<String, Object>> trimmed = rows.size() > 500 ? rows.subList(0, 500) : rows;
        long elapsed = System.currentTimeMillis() - start;

        return new QueryResult(language, List.of(), List.of(), trimmed,
                trimmed.size(), hasMore, elapsed, UUID.randomUUID().toString());
    }
}
