package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("Aggregated schema node for L1 overview")
public record SchemaNode(
    String id,
    String name,
    int    tableCount,
    int    routineCount,
    int    packageCount,
    // L1 hierarchy — null when not linked in the graph
    String databaseGeoid,    // @rid of parent DaliDatabase  (via CONTAINS_SCHEMA edge)
    String databaseName,     // DaliDatabase.db_name
    String databaseEngine,   // DaliDatabase.db_engine (empty until populated)
    String applicationGeoid, // @rid of parent DaliApplication (empty until DaliApplication is loaded)
    String applicationName   // DaliApplication.app_name
) {}
