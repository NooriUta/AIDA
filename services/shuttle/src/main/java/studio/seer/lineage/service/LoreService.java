package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.LoreEntry;

import java.util.List;
import java.util.Map;

/**
 * SHT-10 Option 3: Lore queries run against the shared singleton {@code hound_lore} DB.
 *
 * No join with tenant lineage data — callers receive canonical Lore entries only.
 * If Q-MT6-b resolves to Option 1 or 2, this service gets extended; it is never
 * modified to cross-query tenant databases directly.
 *
 * DB name is intentionally hardcoded: hound_lore is a singleton shared resource,
 * not a per-tenant database, so registry routing does not apply.
 */
@ApplicationScoped
public class LoreService {

    static final String LORE_DB = "hound_lore";

    @Inject ArcadeGateway arcade;

    public Uni<LoreEntry> findByGeoid(String geoid) {
        return arcade.sqlIn(LORE_DB,
                "SELECT @rid.toString() as id, geoid, kind, label, description " +
                "FROM LoreEntry WHERE geoid = :geoid LIMIT 1",
                Map.of("geoid", geoid))
                .map(rows -> rows.isEmpty() ? null : toEntry(rows.get(0)));
    }

    public Uni<List<LoreEntry>> search(String query, int limit) {
        if (query == null || query.isBlank()) return Uni.createFrom().item(List.of());
        String like = "%" + query.replace("%", "\\%") + "%";
        return arcade.sqlIn(LORE_DB,
                "SELECT @rid.toString() as id, geoid, kind, label, description " +
                "FROM LoreEntry WHERE label LIKE :q OR description LIKE :q LIMIT :lim",
                Map.of("q", like, "lim", Math.min(limit, 100)))
                .map(rows -> rows.stream().map(LoreService::toEntry).toList());
    }

    private static LoreEntry toEntry(Map<String, Object> row) {
        return new LoreEntry(
                (String) row.getOrDefault("id", ""),
                (String) row.getOrDefault("geoid", ""),
                (String) row.getOrDefault("kind", ""),
                (String) row.getOrDefault("label", ""),
                (String) row.getOrDefault("description", ""));
    }
}
