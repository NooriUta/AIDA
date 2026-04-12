package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — table with column list and lineage role counts")
public record KnotTable(
    String id,
    String geoid,
    String name,
    String schema,
    String tableType,        // TABLE, VIEW
    int    columnCount,
    int    sourceCount,      // times used as READS_FROM source
    int    targetCount,      // times used as WRITES_TO target
    List<KnotColumn>  columns,
    List<String>      aliases  // from DaliTable.aliases (JSON array)
) {}
