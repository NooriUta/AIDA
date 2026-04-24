package studio.seer.anvil.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.model.SchemaResponse;
import studio.seer.anvil.model.YggCommand;
import studio.seer.anvil.rest.YggQueryClient;
import studio.seer.anvil.util.YggUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SchemaService {

    private static final String VERTEX_TYPES_SQL = "SELECT name FROM schema:types WHERE type = 'vertex'";
    private static final String EDGE_TYPES_SQL   = "SELECT name FROM schema:types WHERE type = 'edge'";
    private static final String VERTEX_COUNT_SQL = "SELECT count(*) AS cnt FROM V";
    private static final String EDGE_COUNT_SQL   = "SELECT count(*) AS cnt FROM E";

    @Inject
    @RestClient
    YggQueryClient ygg;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    public SchemaResponse getSchema(String dbName) {
        String db   = dbName != null && !dbName.isBlank() ? dbName : defaultDb;
        String auth = YggUtil.basicAuth(yggUser, yggPassword);

        return new SchemaResponse(
                queryNames(db, auth, VERTEX_TYPES_SQL),
                queryNames(db, auth, EDGE_TYPES_SQL),
                db,
                Map.of("vertexCount", queryCount(db, auth, VERTEX_COUNT_SQL),
                       "edgeCount",   queryCount(db, auth, EDGE_COUNT_SQL))
        );
    }

    private List<String> queryNames(String db, String auth, String sql) {
        try {
            List<Map<String, Object>> rows = ygg.query(db, auth, new YggCommand("sql", sql, Map.of()))
                                                .await().indefinitely().result();
            if (rows == null) return List.of();
            List<String> names = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Object name = row.get("name");
                if (name != null) names.add(name.toString());
            }
            return names;
        } catch (Exception e) {
            return List.of();
        }
    }

    private long queryCount(String db, String auth, String sql) {
        try {
            List<Map<String, Object>> rows = ygg.query(db, auth, new YggCommand("sql", sql, Map.of()))
                                                .await().indefinitely().result();
            if (rows == null || rows.isEmpty()) return 0;
            Object cnt = rows.get(0).get("cnt");
            return cnt instanceof Number n ? n.longValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
