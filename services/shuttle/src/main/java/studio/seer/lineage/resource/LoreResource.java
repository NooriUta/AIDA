package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import studio.seer.lineage.model.LoreEntry;
import studio.seer.lineage.service.LoreService;

import java.util.List;

/**
 * SHT-10 Option 3: Lore resolvers that never join with tenant lineage data.
 *
 * Resolvers are intentionally standalone — no YggLineageRegistry, no TenantContext.
 * All results come from the shared singleton {@code hound_lore} database.
 *
 * If Q-MT6-b decides Option 1/2, extend LoreService; this resource stays thin.
 */
@GraphQLApi
public class LoreResource {

    @Inject LoreService loreService;

    @Query("loreByGeoid")
    @Description("Fetch a Lore entry by canonical geoid. Available to all authenticated roles.")
    public Uni<LoreEntry> loreByGeoid(@Name("geoid") String geoid) {
        return loreService.findByGeoid(geoid);
    }

    @Query("loreSearch")
    @Description("Full-text search in the shared Lore knowledge-base. Role: viewer+")
    public Uni<List<LoreEntry>> loreSearch(
            @Name("query") String query,
            @Name("limit") @DefaultValue("20") int limit) {
        return loreService.search(query, limit);
    }
}
