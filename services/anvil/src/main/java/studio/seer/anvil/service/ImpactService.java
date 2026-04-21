package studio.seer.anvil.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.heimdall.AnvilEventEmitter;
import studio.seer.anvil.model.*;
import studio.seer.anvil.rest.YggQueryClient;

import java.util.*;

@ApplicationScoped
public class ImpactService {

    static final List<String> EXCLUDED_TYPES =
            List.of("DaliAtom", "DaliPrimaryKey", "DaliForeignKey");

    static final List<String> DEFAULT_INCLUDE_TYPES =
            List.of("DaliRoutine", "DaliStatement", "DaliTable", "DaliColumn");

    @Inject
    @RestClient
    YggQueryClient ygg;

    @Inject
    AnvilEventEmitter events;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    public ImpactResult findImpact(ImpactRequest req) {
        long start = System.currentTimeMillis();
        String db = req.dbName() != null ? req.dbName() : defaultDb;
        List<String> types = (req.includeTypes() != null && !req.includeTypes().isEmpty())
                ? req.includeTypes() : DEFAULT_INCLUDE_TYPES;
        int maxHops = req.maxHops() > 0 ? req.maxHops() : 5;
        String direction = req.direction() != null ? req.direction() : "downstream";

        events.traversalStarted(req.nodeId(), direction, maxHops, db);

        String cypher = buildTraversalCypher(req.nodeId(), direction, maxHops, db, types);
        YggCommand cmd = new YggCommand("cypher", cypher, Map.of());
        String auth = basicAuth(yggUser, yggPassword);

        List<Map<String, Object>> rows = ygg.query(db, auth, cmd)
                                            .await().indefinitely()
                                            .result();

        if (rows == null) rows = List.of();

        List<ImpactNode> nodes = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String id    = str(row, "id");
            String type  = str(row, "type");
            String label = str(row, "label");
            int    depth = num(row, "depth");
            if (id != null && !EXCLUDED_TYPES.contains(type)) {
                nodes.add(new ImpactNode(id, type, label, depth));
            }
        }

        boolean hasMore = nodes.size() >= 500;
        long elapsed = System.currentTimeMillis() - start;

        // Build root node from request
        ImpactNode root = new ImpactNode(req.nodeId(), "unknown", req.nodeId(), 0);

        // Edges not available from this Cypher — ANVIL-2 will enrich
        List<ImpactEdge> edges = List.of();

        events.traversalCompleted(elapsed, nodes.size(), hasMore, false);

        return new ImpactResult(root, nodes, edges, nodes.size(), hasMore, false, elapsed);
    }

    // ── Cypher builders ───────────────────────────────────────────────────────

    private String buildTraversalCypher(String nodeId, String direction, int maxHops,
                                        String db, List<String> includeTypes) {
        String relPattern = direction.equals("upstream")
                ? "<-[:ATOM_REF_COLUMN|DATA_FLOW|FILTER_FLOW|JOIN_FLOW*1.." + maxHops + "]-"
                : "-[:ATOM_REF_COLUMN|DATA_FLOW|FILTER_FLOW|JOIN_FLOW*1.." + maxHops + "]->";
        String typeList = toArcadeList(includeTypes);

        return """
            MATCH (start)
            WHERE start.geoid = '%s' AND start.db_name = '%s'
            CALL {
              WITH start
              MATCH path = (start)%s(affected)
              WHERE affected.db_name = '%s'
                AND labels(affected)[0] IN %s
              RETURN affected, length(path) AS depth
            }
            RETURN affected.geoid AS id, labels(affected)[0] AS type,
                   affected.qualifiedName AS label, depth
            ORDER BY depth ASC LIMIT 500
            """.formatted(escape(nodeId), escape(db), relPattern, escape(db), typeList);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String basicAuth(String user, String pass) {
        String creds = user + ":" + pass;
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes());
    }

    private static String toArcadeList(List<String> items) {
        return "['" + String.join("','", items) + "']";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "\\'");
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

    String basicAuthForTest(String user, String pass) {
        return basicAuth(user, pass);
    }
}
