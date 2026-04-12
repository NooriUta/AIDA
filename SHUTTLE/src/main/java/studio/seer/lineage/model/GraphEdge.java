package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("A directed edge in the lineage graph")
public record GraphEdge(
    String id,
    String source,
    String target,
    String type
) {}
