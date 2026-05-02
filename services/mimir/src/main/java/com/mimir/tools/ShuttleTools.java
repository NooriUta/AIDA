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
            String pattern = namePattern == null ? "" : namePattern.toUpperCase().replace('*', '%');
            String type = nodeType == null ? "ALL" : nodeType.toUpperCase();
            String likePattern = pattern.contains("%") ? pattern : "%" + pattern + "%";

            // Real Hound ArcadeDB schema (verified 02.05.2026):
            //   DaliTable:   table_geoid, table_name, schema_geoid, table_type
            //   DaliRoutine: routine_geoid, routine_name, schema_geoid, routine_type, source_text
            //   DaliColumn:  column_geoid, column_name, table_geoid, data_type
            //
            // Use UNION ALL to search across all 3 vertex types unless filtered.
            String sql;
            switch (type) {
                case "ROUTINE":
                case "PROCEDURE":
                case "FUNCTION":
                    sql = """
                          SELECT routine_geoid AS geoid, routine_name AS name, @class AS type,
                                 schema_geoid AS schema
                          FROM DaliRoutine
                          WHERE routine_geoid.toUpperCase() LIKE :p
                             OR routine_name.toUpperCase() LIKE :p
                          LIMIT :lim
                          """;
                    break;
                case "COLUMN":
                case "FIELD":
                    sql = """
                          SELECT column_geoid AS geoid, column_name AS name, @class AS type,
                                 table_geoid AS schema
                          FROM DaliColumn
                          WHERE column_geoid.toUpperCase() LIKE :p
                             OR column_name.toUpperCase() LIKE :p
                          LIMIT :lim
                          """;
                    break;
                case "TABLE":
                    sql = """
                          SELECT table_geoid AS geoid, table_name AS name, @class AS type,
                                 schema_geoid AS schema
                          FROM DaliTable
                          WHERE table_geoid.toUpperCase() LIKE :p
                             OR table_name.toUpperCase() LIKE :p
                          LIMIT :lim
                          """;
                    break;
                default:  // ALL
                    sql = """
                          SELECT table_geoid AS geoid, table_name AS name, @class AS type,
                                 schema_geoid AS schema
                          FROM DaliTable
                          WHERE table_geoid.toUpperCase() LIKE :p OR table_name.toUpperCase() LIKE :p
                          LIMIT :lim
                          """;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("p", likePattern);
            params.put("lim", boundedLimit);

            ArcadeDbClient.QueryResult r = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql", sql, params));

            List<Map<String, String>> result = r.result().stream()
                    .map(row -> Map.of(
                            "geoid",  String.valueOf(row.getOrDefault("geoid", "")),
                            "name",   String.valueOf(row.getOrDefault("name", "")),
                            "type",   String.valueOf(row.getOrDefault("type", "")),
                            "schema", String.valueOf(row.getOrDefault("schema", ""))))
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

}
