package studio.seer.heimdall.prefs;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.heimdall.snapshot.FriggGateway;

import java.util.List;
import java.util.Map;

/**
 * FRIGG persistence layer for per-user UI preferences.
 *
 * Document type: UserPrefs
 * Primary key:   sub  (Keycloak UUID, UNIQUE index)
 *
 * UPSERT pattern (ArcadeDB):
 *   UPDATE UserPrefs SET ... UPSERT WHERE sub = :sub
 * Creates the document if absent, updates it if present — single round-trip.
 */
@ApplicationScoped
public class UserPrefsRepository {

    private static final Logger LOG = Logger.getLogger(UserPrefsRepository.class);

    @Inject FriggGateway frigg;

    private volatile boolean schemaEnsured = false;

    // ── Public API ────────────────────────────────────────────────────────────

    public Uni<UserPrefsRecord> findBySub(String sub) {
        return ensureSchema()
                .chain(() -> frigg.sql(
                        "SELECT sub, theme, palette, density, uiFont, monoFont, fontSize " +
                        "FROM UserPrefs WHERE sub = :sub LIMIT 1",
                        Map.of("sub", sub)))
                .map(rows -> rows.isEmpty() ? null : mapRow(rows.get(0)));
    }

    public Uni<Void> upsert(UserPrefsRecord prefs) {
        final Map<String, Object> params = Map.of(
                "sub",      prefs.sub(),
                "theme",    coalesce(prefs.theme(),    "dark"),
                "palette",  coalesce(prefs.palette(),  "amber-forest"),
                "density",  coalesce(prefs.density(),  "normal"),
                "uiFont",   coalesce(prefs.uiFont(),   "inter"),
                "monoFont", coalesce(prefs.monoFont(), "jetbrains"),
                "fontSize", coalesce(prefs.fontSize(), "14")
        );
        return ensureSchema()
                .chain(() -> frigg.sql(
                        "UPDATE UserPrefs SET " +
                        "theme = :theme, palette = :palette, density = :density, " +
                        "uiFont = :uiFont, monoFont = :monoFont, fontSize = :fontSize " +
                        "UPSERT WHERE sub = :sub",
                        params))
                .invoke(__ -> LOG.debugf("[PREFS] upsert sub=%s", prefs.sub()))
                .replaceWithVoid();
    }

    // ── Schema init (lazy, one-time) ─────────────────────────────────────────

    private Uni<Void> ensureSchema() {
        if (schemaEnsured) return Uni.createFrom().voidItem();
        return frigg.sql("CREATE DOCUMENT TYPE UserPrefs IF NOT EXISTS", null)
                .chain(__ -> frigg.sql(
                        "CREATE INDEX IF NOT EXISTS ON UserPrefs (sub) UNIQUE", null))
                .invoke(__ -> {
                    schemaEnsured = true;
                    LOG.info("[PREFS] UserPrefs schema ready");
                })
                .replaceWithVoid();
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private static UserPrefsRecord mapRow(Map<String, Object> row) {
        return new UserPrefsRecord(
                str(row, "sub"),
                str(row, "theme"),
                str(row, "palette"),
                str(row, "density"),
                str(row, "uiFont"),
                str(row, "monoFont"),
                str(row, "fontSize")
        );
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v instanceof String s ? s : null;
    }

    private static String coalesce(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    // ── Bulk list (admin only) ────────────────────────────────────────────────

    public Uni<List<UserPrefsRecord>> listAll() {
        return ensureSchema()
                .chain(() -> frigg.sql(
                        "SELECT sub, theme, palette, density, uiFont, monoFont, fontSize " +
                        "FROM UserPrefs LIMIT 500", null))
                .map(rows -> rows.stream().map(UserPrefsRepository::mapRow).toList());
    }
}
