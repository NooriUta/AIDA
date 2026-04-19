package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("Schema within a database in the application hierarchy")
public record DaliSchemaDto(
    String id,
    String schemaName,
    String schemaGeoid,
    int    tableCount,
    int    routineCount,
    int    packageCount
) {}
