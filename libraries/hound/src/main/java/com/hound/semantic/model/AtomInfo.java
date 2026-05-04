package com.hound.semantic.model;

public class AtomInfo {

    // ── primary_status enum (ADR-HND-002) ──────────────────────────────────────
    public static final String STATUS_RESOLVED           = "RESOLVED";
    public static final String STATUS_UNRESOLVED         = "UNRESOLVED";
    public static final String STATUS_CONSTANT           = "CONSTANT";
    public static final String STATUS_CONSTANT_ORPHAN    = "CONSTANT_ORPHAN";
    public static final String STATUS_FUNCTION_CALL      = "FUNCTION_CALL";
    public static final String STATUS_RECONSTRUCT_DIRECT = "RECONSTRUCT_DIRECT";
    public static final String STATUS_RECONSTRUCT_INVERSE = "RECONSTRUCT_INVERSE";
    public static final String STATUS_PARTIAL            = "PARTIAL";
    public static final String STATUS_PENDING_INJECT     = "PENDING_INJECT";

    // ── qualifier enum (ADR-HND-002) ───────────────────────────────────────────
    public static final String QUALIFIER_LINKED       = "LINKED";
    public static final String QUALIFIER_CTE          = "CTE";
    public static final String QUALIFIER_SUBQUERY     = "SUBQUERY";
    public static final String QUALIFIER_INFERRED     = "INFERRED";
    public static final String QUALIFIER_FUZZY        = "FUZZY";
    public static final String QUALIFIER_CTRL_FLOW    = "CTRL_FLOW";
    public static final String QUALIFIER_FN_VERIFIED  = "FN_VERIFIED";
    public static final String QUALIFIER_FN_UNVERIFIED = "FN_UNVERIFIED";

    // ── Legacy constants (used only for data migration) ────────────────────────
    public static final String LEGACY_STATUS_RESOLVED   = "Обработано";
    public static final String LEGACY_STATUS_UNRESOLVED = "Не разобрано";
    public static final String LEGACY_STATUS_UNBOUND    = "Не связано";

    private AtomInfo() {}
}
