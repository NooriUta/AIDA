package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.Map;

@Description("A node in the lineage graph (table, routine, column, atom…)")
public record GraphNode(
    String id,
    String type,
    String label,
    String scope,
    Map<String, String> meta,
    String dataSource
) {}
