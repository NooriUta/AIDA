package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("A Lore knowledge-base entry (shared singleton DB, no tenant join)")
public record LoreEntry(
        String id,
        String geoid,
        String kind,
        String label,
        String description
) {}
