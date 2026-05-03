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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tools for direct YGG queries via ArcadeDB REST.
 *
 * <p>Decision #65: Tool 5 = {@code count_dependencies} (validate_columns deferred to v2).
 *
 * <p>Tenant isolation: dbName = {@code hound_{alias}} via {@link DbNameResolver}.
 * tenantAlias берётся из {@link TenantContext} (заполняется JAX-RS filter из X-Seer-Tenant-Alias).
 */
@ApplicationScoped
public class YggTools {

    private static final Logger LOG = Logger.getLogger(YggTools.class);

    @Inject @RestClient ArcadeDbClient arcade;
    @Inject TenantContext tenantContext;
    @Inject DbNameResolver dbNameResolver;
    @Inject MimirEventEmitter eventEmitter;

    @Tool("Get the source code (DDL/PL-SQL) of a stored routine (procedure, function or package) " +
          "by its qualified name. Returns source text, language, and metadata.")
    public Map<String, Object> get_procedure_source(
            @P("Qualified routine name (e.g. SCHEMA.PROC_NAME or SCHEMA.PKG.PROC)") String qualifiedName
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();

        Map<String, Object> startArgs = new HashMap<>();
        startArgs.put("qualifiedName", qualifiedName == null ? "" : qualifiedName);
        eventEmitter.toolCallStarted(sessionId, "get_procedure_source", startArgs);

        try {
            String sql = """
                    SELECT qualifiedName, source_text AS sourceText, language, schema, name, @class AS type
                    FROM DaliRoutine
                    WHERE qualifiedName = :name
                    LIMIT 1
                    """;
            ArcadeDbClient.QueryResult r = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql", sql, Map.of("name", qualifiedName)));

            if (r.result().isEmpty()) {
                eventEmitter.toolCallCompleted(sessionId, "get_procedure_source",
                        System.currentTimeMillis() - start, 0);
                return Map.of(
                        "error",         "not_found",
                        "qualifiedName", qualifiedName,
                        "message",       "No DaliRoutine with qualifiedName=" + qualifiedName + " in " + dbName);
            }

            Map<String, Object> row = r.result().get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("qualifiedName", String.valueOf(row.getOrDefault("qualifiedName", "")));
            result.put("sourceText",    String.valueOf(row.getOrDefault("sourceText", "")));
            result.put("language",      String.valueOf(row.getOrDefault("language", "unknown")));
            result.put("schema",        String.valueOf(row.getOrDefault("schema", "")));
            result.put("name",          String.valueOf(row.getOrDefault("name", "")));
            result.put("type",          String.valueOf(row.getOrDefault("type", "DaliRoutine")));

            eventEmitter.toolCallCompleted(sessionId, "get_procedure_source",
                    System.currentTimeMillis() - start, 1);
            return result;
        } catch (Exception e) {
            LOG.warnf(e, "get_procedure_source failed for tenant=%s qualifiedName=%s", alias, qualifiedName);
            eventEmitter.toolCallCompleted(sessionId, "get_procedure_source",
                    System.currentTimeMillis() - start, 0);
            return Map.of("error", "query_failed", "message", e.getMessage());
        }
    }

    @Tool("List columns of a table with their data types, primary key flags and ordinal positions. " +
          "Use when user asks 'what columns does X have?', 'how many fields in Y?', 'структура таблицы Z'.")
    public Map<String, Object> describe_table_columns(
            @P("Table geoid (e.g. CRM.COUNTRIES, HR.EMPLOYEES)") String tableGeoid
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();

        Map<String, Object> args = new HashMap<>();
        args.put("tableGeoid", tableGeoid == null ? "" : tableGeoid);
        eventEmitter.toolCallStarted(sessionId, "describe_table_columns", args);

        try {
            String sql = """
                    SELECT column_name, data_type, is_pk, is_fk, is_required, ordinal_position
                    FROM DaliColumn
                    WHERE table_geoid = :tg
                    ORDER BY ordinal_position
                    """;
            ArcadeDbClient.QueryResult r = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql", sql, Map.of("tg", tableGeoid)));

            eventEmitter.toolCallCompleted(sessionId, "describe_table_columns",
                    System.currentTimeMillis() - start, r.result().size());

            return Map.of(
                    "tableGeoid",  tableGeoid,
                    "columnCount", r.result().size(),
                    "columns",     r.result());
        } catch (Exception e) {
            LOG.warnf(e, "describe_table_columns failed for tenant=%s tableGeoid=%s", alias, tableGeoid);
            eventEmitter.toolCallCompleted(sessionId, "describe_table_columns",
                    System.currentTimeMillis() - start, 0);
            return Map.of(
                    "tableGeoid",  tableGeoid == null ? "" : tableGeoid,
                    "columnCount", 0,
                    "columns",     List.of(),
                    "error",       "query_failed");
        }
    }

    @Tool("Count tables that belong to a given DB schema. Use this to answer " +
          "'how many tables in schema X', 'сколько таблиц в схеме X'. Traverses " +
          "DaliSchema --CONTAINS_TABLE--> DaliTable, scoped to the user's tenant DB. " +
          "Also returns example table names for context.")
    public Map<String, Object> count_tables_in_schema(
            @P("Schema name as it appears in the source DB, e.g. 'DWH', 'HR', 'CRM'") String schemaName
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();

        Map<String, Object> args = new HashMap<>();
        args.put("schemaName", schemaName == null ? "" : schemaName);
        eventEmitter.toolCallStarted(sessionId, "count_tables_in_schema", args);

        if (schemaName == null || schemaName.isBlank()) {
            eventEmitter.toolCallCompleted(sessionId, "count_tables_in_schema",
                    System.currentTimeMillis() - start, 0);
            return Map.of(
                    "schemaName",  "",
                    "tableCount",  0,
                    "tables",      List.of(),
                    "error",       "schema_name_required");
        }

        try {
            // Count via edge traversal (no schema_geoid scan — uses CONTAINS_TABLE index implicitly)
            String countSql = """
                    SELECT count(*) AS n
                    FROM (SELECT expand(out("CONTAINS_TABLE")) FROM DaliSchema WHERE schema_name = :s)
                    """;
            ArcadeDbClient.QueryResult c = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql", countSql, Map.of("s", schemaName)));
            long n = 0L;
            if (!c.result().isEmpty()) {
                Object v = c.result().get(0).get("n");
                if (v instanceof Number num) n = num.longValue();
            }

            // Sample up to 10 names so the LLM can mention real examples without flooding context
            String sampleSql = """
                    SELECT table_name
                    FROM (SELECT expand(out("CONTAINS_TABLE")) FROM DaliSchema WHERE schema_name = :s)
                    ORDER BY table_name
                    LIMIT 10
                    """;
            ArcadeDbClient.QueryResult s = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql", sampleSql, Map.of("s", schemaName)));

            eventEmitter.toolCallCompleted(sessionId, "count_tables_in_schema",
                    System.currentTimeMillis() - start, (int) n);

            return Map.of(
                    "schemaName", schemaName,
                    "tableCount", n,
                    "sample",     s.result());
        } catch (Exception e) {
            LOG.warnf(e, "count_tables_in_schema failed for tenant=%s schema=%s", alias, schemaName);
            eventEmitter.toolCallCompleted(sessionId, "count_tables_in_schema",
                    System.currentTimeMillis() - start, 0);
            return Map.of(
                    "schemaName", schemaName,
                    "tableCount", 0,
                    "sample",     List.of(),
                    "error",      "query_failed");
        }
    }

    @Tool("List schema names available in the user's tenant DB. Returns the names that " +
          "count_tables_in_schema and other tools accept. Use to answer 'what schemas are there?', " +
          "'какие у нас схемы?', or to disambiguate before drilling into a specific one.")
    public Map<String, Object> list_schemas() {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();

        eventEmitter.toolCallStarted(sessionId, "list_schemas", Map.of());

        try {
            ArcadeDbClient.QueryResult r = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("sql",
                            "SELECT schema_name FROM DaliSchema ORDER BY schema_name", Map.of()));
            eventEmitter.toolCallCompleted(sessionId, "list_schemas",
                    System.currentTimeMillis() - start, r.result().size());
            return Map.of(
                    "schemaCount", r.result().size(),
                    "schemas",     r.result().stream()
                            .map(row -> row.get("schema_name"))
                            .filter(java.util.Objects::nonNull)
                            .toList());
        } catch (Exception e) {
            LOG.warnf(e, "list_schemas failed for tenant=%s", alias);
            eventEmitter.toolCallCompleted(sessionId, "list_schemas",
                    System.currentTimeMillis() - start, 0);
            return Map.of("schemaCount", 0, "schemas", List.of(), "error", "query_failed");
        }
    }

    @Tool("Count incoming/outgoing dependencies for a node — answers " +
          "'how critical is this object?'. Returns counts for in / out / both directions.")
    public Map<String, Object> count_dependencies(
            @P("Node geoid (canonical id, e.g. HR.ORDERS or full SCHEMA.TABLE.COLUMN)") String nodeId,
            @P("Direction: in (incoming READS_FROM/WRITES_TO) | out | both") String direction
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();

        Map<String, Object> countArgs = new HashMap<>();
        countArgs.put("nodeId",    nodeId == null ? "" : nodeId);
        countArgs.put("direction", direction == null ? "both" : direction);
        eventEmitter.toolCallStarted(sessionId, "count_dependencies", countArgs);

        try {
            String dir = direction == null ? "both" : direction.toLowerCase();
            String cypher = switch (dir) {
                case "in"  -> """
                        MATCH (n)-[r:READS_FROM|WRITES_TO]->(t {geoid: $id})
                        RETURN count(r) AS cnt
                        """;
                case "out" -> """
                        MATCH (s {geoid: $id})-[r:READS_FROM|WRITES_TO]->(n)
                        RETURN count(r) AS cnt
                        """;
                default    -> """
                        MATCH (n {geoid: $id})
                        OPTIONAL MATCH (n)-[ro:READS_FROM|WRITES_TO]->(out)
                        OPTIONAL MATCH (in)-[ri:READS_FROM|WRITES_TO]->(n)
                        RETURN count(DISTINCT ro) + count(DISTINCT ri) AS cnt
                        """;
            };

            ArcadeDbClient.QueryResult r = arcade.query(dbName,
                    new ArcadeDbClient.ArcadeQuery("cypher", cypher, Map.of("id", nodeId)));

            long count = 0L;
            if (!r.result().isEmpty()) {
                Object v = r.result().get(0).get("cnt");
                if (v instanceof Number n) count = n.longValue();
            }

            eventEmitter.toolCallCompleted(sessionId, "count_dependencies",
                    System.currentTimeMillis() - start, 1);
            return Map.of(
                    "nodeId",    nodeId,
                    "direction", dir,
                    "count",     count);
        } catch (Exception e) {
            LOG.warnf(e, "count_dependencies failed for tenant=%s nodeId=%s", alias, nodeId);
            eventEmitter.toolCallCompleted(sessionId, "count_dependencies",
                    System.currentTimeMillis() - start, 0);
            return Map.of(
                    "nodeId",    nodeId,
                    "direction", direction == null ? "both" : direction,
                    "count",     0,
                    "error",     "query_failed");
        }
    }
}
