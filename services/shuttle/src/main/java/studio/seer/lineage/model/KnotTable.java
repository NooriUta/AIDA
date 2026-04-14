package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — table summary (column detail lazy-loaded via knotTableDetail)")
public record KnotTable(
    String       id,
    String       geoid,
    String       name,
    String       schema,
    String       tableType,       // TABLE, VIEW
    int          columnCount,
    int          sourceCount,     // times used as READS_FROM source
    int          targetCount,     // times used as WRITES_TO target
    String       dataSource,      // 'master' | 'reconstructed' | ''
    List<String> aliases          // from DaliTable.aliases (JSON array)
) {}
