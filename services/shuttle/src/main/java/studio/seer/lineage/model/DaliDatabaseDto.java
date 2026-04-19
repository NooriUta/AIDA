package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("Database within an application in the application hierarchy")
public record DaliDatabaseDto(
    String           id,
    String           dbName,
    String           dbGeoid,
    List<DaliSchemaDto> schemas
) {}
