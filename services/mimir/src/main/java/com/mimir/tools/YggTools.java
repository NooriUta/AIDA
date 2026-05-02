package com.mimir.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

/**
 * Tools backed by YGG (ArcadeDB :2480) — source code + dependency stats.
 * Stubs in MP-04 (SPRINT_MIMIR_PREP). Full implementation in MIMIR Foundation.
 *
 * Decision #65: Tool 5 = count_dependencies (validate_columns deferred to MIMIR v2).
 */
@ApplicationScoped
public class YggTools {

    @ConfigProperty(name = "mimir.ygg.url",      defaultValue = "http://localhost:2480")
    String yggUrl;

    @ConfigProperty(name = "mimir.ygg.user",     defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "mimir.ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    @Tool("Get the full SQL source code (DDL) of a stored procedure or view by its node ID.")
    public Map<String, String> get_procedure_source(
        @P("Node ID of the stored procedure or view") String nodeId,
        @P("Database name, e.g. hound_acme") String dbName
    ) {
        // TODO(MIMIR-Foundation): GET yggUrl/api/v1/document/dbName/nodeId
        return Map.of(
            "stub", "get_procedure_source not yet implemented",
            "nodeId", nodeId,
            "dbName", dbName
        );
    }

    @Tool("Count how many routines read from or write to a given table. " +
          "Returns upstream (writers) and downstream (readers) counts.")
    public Map<String, Object> count_dependencies(
        @P("Table node ID to count dependencies for") String tableNodeId,
        @P("Database name, e.g. hound_acme") String dbName
    ) {
        // TODO(MIMIR-Foundation): ArcadeDB SQL query on yggUrl
        return Map.of(
            "stub",            "count_dependencies not yet implemented",
            "tableNodeId",     tableNodeId,
            "upstreamCount",   0,
            "downstreamCount", 0
        );
    }
}
