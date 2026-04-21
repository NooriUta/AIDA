package studio.seer.anvil.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.model.ImpactEdge;
import studio.seer.anvil.model.ImpactNode;
import studio.seer.anvil.model.LineageRequest;
import studio.seer.anvil.model.LineageResult;
import studio.seer.anvil.model.YggCommand;
import studio.seer.anvil.rest.YggQueryClient;
import studio.seer.anvil.util.YggUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LineageService {

    @Inject
    @RestClient
    YggQueryClient ygg;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    public LineageResult queryLineage(LineageRequest req) {
        long start = System.currentTimeMillis();
        String db = req.dbName() != null ? req.dbName() : defaultDb;
        int maxHops = req.maxHops() > 0 ? req.maxHops() : 10;
        String direction = req.direction() != null ? req.direction() : "downstream";
        String auth = YggUtil.basicAuth(yggUser, yggPassword);

        List<ImpactNode> nodes = new ArrayList<>();

        if ("both".equals(direction)) {
            nodes.addAll(traverse(req.nodeId(), "downstream", maxHops, db, auth));
            nodes.addAll(traverse(req.nodeId(), "upstream",   maxHops, db, auth));
        } else {
            nodes.addAll(traverse(req.nodeId(), direction, maxHops, db, auth));
        }

        return new LineageResult(nodes, List.of(), System.currentTimeMillis() - start);
    }

    private List<ImpactNode> traverse(String nodeId, String direction, int maxHops,
                                      String db, String auth) {
        String cypher = buildLineageCypher(nodeId, direction, maxHops, db);
        YggCommand cmd = new YggCommand("cypher", cypher, Map.of());

        List<Map<String, Object>> rows = ygg.query(db, auth, cmd)
                                            .await().indefinitely()
                                            .result();
        if (rows == null) return List.of();

        List<ImpactNode> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String id    = str(row, "id");
            String type  = str(row, "type");
            String label = str(row, "label");
            int    depth = num(row, "depth");
            if (id != null) result.add(new ImpactNode(id, type, label, depth));
        }
        return result;
    }

    private String buildLineageCypher(String nodeId, String direction, int maxHops, String db) {
        String relPattern = "upstream".equals(direction)
                ? "<-[:DATA_FLOW*1.." + maxHops + "]-"
                : "-[:DATA_FLOW*1.." + maxHops + "]->";

        return """
            MATCH (start)
            WHERE start.geoid = '%s' AND start.db_name = '%s'
            CALL {
              WITH start
              MATCH path = (start)%s(related)
              WHERE related.db_name = '%s'
              RETURN related, length(path) AS depth
            }
            RETURN related.geoid AS id, labels(related)[0] AS type,
                   related.qualifiedName AS label, depth
            ORDER BY depth ASC LIMIT 500
            """.formatted(YggUtil.escape(nodeId), YggUtil.escape(db), relPattern, YggUtil.escape(db));
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : null;
    }

    private static int num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }
}
