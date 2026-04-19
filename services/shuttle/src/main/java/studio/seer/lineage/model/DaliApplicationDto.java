package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("Application node — top of the App → DB → Schema hierarchy")
public record DaliApplicationDto(
    String              id,
    String              name,
    String              geoid,
    List<DaliDatabaseDto> databases
) {}
