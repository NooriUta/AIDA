package com.mimir.tools;

import com.mimir.client.ArcadeDbClient;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.tenant.DbNameResolver;
import com.mimir.tenant.TenantContext;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tools for node search via direct ArcadeDB Cypher.
 *
 * <p>Q-MF4a closed (02.05.2026): SHUTTLE GraphQL не имеет {@code searchNodes} query →
 * используем прямой ArcadeDB Cypher. Tenant isolation через {@link DbNameResolver}.
 *
 * <p>Если позднее понадобится FTS / fuzzy / pagination — мигрировать в SHUTTLE GraphQL
 * с расширением schema (отдельный спринт).
 */
@ApplicationScoped
public class ShuttleTools {

    private static final Logger LOG = Logger.getLogger(ShuttleTools.class);

    @Inject @RestClient ArcadeDbClient arcade;
    @Inject TenantContext tenantContext;
    @Inject DbNameResolver dbNameResolver;
    @Inject MimirEventEmitter eventEmitter;

    @Tool("Search for nodes (tables, routines, columns) in the lineage graph by name pattern. " +
          "Supports wildcards (* for any chars). Returns matching node geoids and types.")
    public List<Map<String, String>> search_nodes(
            @P("Name pattern (case-insensitive substring or wildcard with *)") String namePattern,
            @P("Node type filter: TABLE, ROUTINE, COLUMN, or ALL") String nodeType,
            @P("Maximum results, default 20, max 50") int limit
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();
        int boundedLimit = Math.min(Math.max(limit, 1), 50);

        Map<String, Object> startArgs = new HashMap<>();
        startArgs.put("pattern",   namePattern == null ? "" : namePattern);
        startArgs.put("node_type", nodeType == null ? "ALL" : nodeType);
        startArgs.put("limit",     boundedLimit);
        eventEmitter.toolCallStarted(sessionId, "search_nodes", startArgs);

        try {
            String pattern = namePattern == null ? "" : namePattern.toLowerCase().replace('*', '%');
            String typeFilter = mapToVertexType(nodeType);

            // Cypher: match by qualifiedName OR name (case-insensitive contains).
            // Use SQL syntax which ArcadeDB supports for property filtering.
            String sql = """
                    SELECT geoid, qualifiedName, @class AS type, schema
                    FROM %s
                    WHERE qualifiedName.toLowerCase() LIKE :p
                       OR name.toLowerCase() LIKE :p
                    LIMIT :lim
                    """.formatted(typeFilter);
            String likePattern = pattern.contains("%") ? pattern : "%" + pattern + "%";

            Map<String, Object> params = new HashMap<>();
            params.put("p", likePattern);
            params.put("lim", boundedLimit);

            ArcadeDbClient.QueryResult r = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql", sql, params));

            List<Map<String, String>> result = r.result().stream()
                    .map(row -> Map.of(
                            "geoid",         String.valueOf(row.getOrDefault("geoid", "")),
                            "qualifiedName", String.valueOf(row.getOrDefault("qualifiedName", "")),
                            "type",          String.valueOf(row.getOrDefault("type", "")),
                            "schema",        String.valueOf(row.getOrDefault("schema", ""))))
                    .toList();

            eventEmitter.toolCallCompleted(sessionId, "search_nodes",
                    System.currentTimeMillis() - start, result.size());
            return result;
        } catch (Exception e) {
            LOG.warnf(e, "search_nodes failed for tenant=%s pattern=%s", alias, namePattern);
            eventEmitter.toolCallCompleted(sessionId, "search_nodes",
                    System.currentTimeMillis() - start, 0);
            return Collections.emptyList();
        }
    }

    /**
     * Maps user-facing nodeType to ArcadeDB vertex type.
     * ALL → DaliTable (default — most useful for impact queries).
     */
    private static String mapToVertexType(String nodeType) {
        if (nodeType == null) return "DaliTable";
        return switch (nodeType.toUpperCase()) {
            case "ROUTINE", "PROCEDURE", "FUNCTION" -> "DaliRoutine";
            case "COLUMN", "FIELD" -> "DaliColumn";
            case "ALL", "*"        -> "V"; // ArcadeDB super vertex type
            default                -> "DaliTable";
        };
    }
}
