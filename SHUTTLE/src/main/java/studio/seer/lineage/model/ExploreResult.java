package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("Graph payload for L2 explore and L3 column lineage views")
public record ExploreResult(
    List<GraphNode> nodes,
    List<GraphEdge> edges,
    boolean hasMore
) {}
