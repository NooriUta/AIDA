package studio.seer.lineage.service;

/**
 * Static parsing utilities for Hound stmt_geoid and atom-id strings.
 *
 * <p>Extracted from {@link KnotService} (LOC refactor — QG-ARCH-INVARIANTS §2.4).
 *
 * <p>stmt_geoid format:
 * {@code "SCHEMA.PACKAGE:ROUTINE_TYPE:ROUTINE_NAME:STMT_TYPE:LINE[:nested...]"}
 * e.g. {@code "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:152"}
 *
 * <p>atom_id / atom_text tilde-encoding:
 * {@code "NAME~line:col"} or {@code "NAME~line1:col1~line2:col2"} (last segment wins)
 */
public final class KnotGeoidParser {

    private KnotGeoidParser() {}

    /**
     * Parse line number from atom_id tilde-encoding: last ~-segment before optional ':'.
     * "CODE_FIELD~78:0" → 78, "SOME~45:3~152:7" → 152, no-tilde or empty → 0.
     */
    public static int atomLine(String atomId) {
        if (atomId == null || atomId.isEmpty() || !atomId.contains("~")) return 0;
        String seg = atomId.substring(atomId.lastIndexOf('~') + 1);
        if (seg.isEmpty()) return 0;
        int colon = seg.indexOf(':');
        String part = colon >= 0 ? seg.substring(0, colon) : seg;
        try { return Integer.parseInt(part); }
        catch (NumberFormatException ignored) { return 0; }
    }

    /**
     * Parse column position from atom_id tilde-encoding: last ~-segment after ':'.
     * "CODE_FIELD~78:0" → 0, "SOME~152:7" → 7, no-colon → 0.
     */
    public static int atomPos(String atomId) {
        if (atomId == null || atomId.isEmpty() || !atomId.contains("~")) return 0;
        String seg = atomId.substring(atomId.lastIndexOf('~') + 1);
        int colon = seg.indexOf(':');
        if (colon < 0) return 0;
        try { return Integer.parseInt(seg.substring(colon + 1)); }
        catch (NumberFormatException ignored) { return 0; }
    }

    /**
     * Parse stmt type from geoid: "SCHEMA.PKG:RTYPE:RNAME:STMT_TYPE:LINE"
     * Returns part[3] (e.g. "INSERT", "SELECT"), or "UNKNOWN" if not present.
     */
    public static String parseStmtType(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "UNKNOWN";
        String[] parts = geoid.split(":");
        String t = parts.length >= 4 ? parts[3] : "";
        return t.isEmpty() ? "UNKNOWN" : t;
    }

    /**
     * Parse line number from geoid: part[4].
     */
    public static int parseLineNumber(String geoid) {
        if (geoid == null || geoid.isEmpty()) return 0;
        String[] parts = geoid.split(":");
        if (parts.length >= 5) {
            try { return Integer.parseInt(parts[4]); }
            catch (NumberFormatException ignored) { return 0; }
        }
        return 0;
    }

    /**
     * Short display name for a statement used as a source (CTE/subquery).
     * Returns "ROUTINE_NAME:STMT_TYPE:LINE" — enough to identify without full geoid noise.
     * E.g. "DWH.PKG:PROCEDURE:LOAD:SELECT:42" → "LOAD:SELECT:42"
     */
    public static String parseStmtShortName(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "?";
        String[] parts = geoid.split(":");
        // parts: [0]=pkg, [1]=routineType, [2]=routineName, [3]=stmtType, [4]=line, ...
        if (parts.length >= 5) return parts[2] + ":" + parts[3] + ":" + parts[4];
        if (parts.length >= 3) return parts[2];
        return geoid;
    }

    /**
     * Parse package name from geoid: part[0] (e.g. "DWH.CALC_PKL_CRED").
     */
    public static String parsePackageName(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "";
        int idx = geoid.indexOf(':');
        return idx > 0 ? geoid.substring(0, idx) : geoid;
    }

    /**
     * Derive a display name: use session_name if present,
     * otherwise filename without extension from filePath.
     */
    public static String deriveName(String sessionName, String filePath) {
        if (sessionName != null && !sessionName.isBlank()) return sessionName;
        if (filePath == null || filePath.isBlank()) return "";
        int slash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        String filename = slash >= 0 ? filePath.substring(slash + 1) : filePath;
        int dot = filename.indexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
