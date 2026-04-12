package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("A single full-text search hit")
public record SearchResult(
    String id,
    String type,
    String label,
    String scope,
    double score
) {}
