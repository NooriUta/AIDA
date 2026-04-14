package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — lazy table detail: columns with PK/FK/type info and SQL snippet")
public record KnotTableDetail(
    String           tableGeoid,
    String           dataSource,      // 'master' | 'reconstructed' | ''
    List<KnotColumn> columns,
    String           snippet           // SQL text of one statement that uses this table
) {}
