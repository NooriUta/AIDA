package studio.seer.anvil.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.heimdall.AnvilEventEmitter;
import studio.seer.anvil.model.*;
import studio.seer.anvil.rest.YggQueryClient;
import studio.seer.anvil.util.YggUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Inject
    AnvilCache cache;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    public ImpactResult findImpact(ImpactRequest req, String tenantAlias) {
        long start = System.currentTimeMillis();
        String db        = req.dbName()    != null ? req.dbName()    : defaultDb;
        int    maxHops   = req.maxHops()   >  0    ? req.maxHops()   : 5;
        String direction = req.direction() != null ? req.direction() : "downstream";
        List<String> types = (req.includeTypes() != null && !req.includeTypes().isEmpty())
                ? req.includeTypes() : DEFAULT_INCLUDE_TYPES;

        if (tenantAlias == null || tenantAlias.isBlank()) {
            throw new IllegalArgumentException("tenantAlias must be non-blank (MTN-30)");
        }

        // AV-03 + MTN-30: cache lookup — key now scoped by tenantAlias so cross-tenant
        // requests can never read or evict each other's entries.
        String cacheKey = AnvilCache.key(tenantAlias, req.nodeId(), direction, maxHops, db);
        ImpactResult cached = cache.get(cacheKey);
        if (cached != null) {
            events.cacheHit(req.nodeId(), direction, db);
            return new ImpactResult(cached.rootNode(), cached.nodes(), cached.edges(),
                    cached.totalAffected(), cached.hasMore(), true,
                    System.currentTimeMillis() - start);
        }

        events.traversalStarted(req.nodeId(), direction, maxHops, db);

        String auth   = YggUtil.basicAuth(yggUser, yggPassword);
        String cypher = buildTraversalCypher(req.nodeId(), direction, maxHops, db, types);
        List<Map<String, Object>> rows = ygg.query(db, auth, new YggCommand("cypher", cypher, Map.of()))
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
        long    elapsed = System.currentTimeMillis() - start;
        ImpactNode root = new ImpactNode(req.nodeId(), "unknown", req.nodeId(), 0);
        ImpactResult result = new ImpactResult(root, nodes, List.of(), nodes.size(), hasMore, false, elapsed);

        cache.put(cacheKey, result);
        events.traversalCompleted(elapsed, nodes.size(), hasMore, false);
        return result;
    }

    // ── Cypher builders ───────────────────────────────────────────────────────

    private String buildTraversalCypher(String nodeId, String direction, int maxHops,
                                        String db, List<String> includeTypes) {
        String relPattern = "upstream".equals(direction)
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
            """.formatted(YggUtil.escape(nodeId), YggUtil.escape(db), relPattern, YggUtil.escape(db), typeList);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String toArcadeList(List<String> items) {
        return "['" + String.join("','", items) + "']";
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
