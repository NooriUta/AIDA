package studio.seer.heimdall.prefs;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * User UI preferences stored in FRIGG (ArcadeDB).
 *
 * Storage split:
 *   FRIGG (this record)  — theme, palette, density, uiFont, monoFont, fontSize
 *   Keycloak attributes  — lang  (admin-visible, stored as pref.lang)
 *   localStorage         — client-side cache (hydrated from FRIGG on login)
 *
 * Keyed by Keycloak sub (UUID).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPrefsRecord(
        String sub,
        String theme,       // "dark" | "light"
        String palette,     // "amber-forest" | "ocean" | ...
        String density,     // "normal" | "compact"
        String uiFont,      // "inter" | "roboto" | "system"
        String monoFont,    // "jetbrains" | "fira" | "cascadia"
        String fontSize     // "12" | "13" | "14" | "15" | "16"
) {
    /** Defaults used when no record exists in FRIGG yet. */
    public static UserPrefsRecord defaults(String sub) {
        return new UserPrefsRecord(sub, "dark", "amber-forest", "normal", "inter", "jetbrains", "14");
    }
}
