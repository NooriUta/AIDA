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

    // ── kind enum (ADR-HND-009) ─────────────────────────────────────────────────
    public static final String KIND_COLUMN        = "COLUMN";
    public static final String KIND_OUTPUT_COL    = "OUTPUT_COL";
    public static final String KIND_VARIABLE      = "VARIABLE";
    public static final String KIND_PARAMETER     = "PARAMETER";
    public static final String KIND_FUNCTION_CALL = "FUNCTION_CALL";
    public static final String KIND_SEQUENCE      = "SEQUENCE";
    public static final String KIND_RECORD_FIELD  = "RECORD_FIELD";
    public static final String KIND_CURSOR_RECORD = "CURSOR_RECORD";
    public static final String KIND_CONSTANT      = "CONSTANT";
    public static final String KIND_AMBIGUOUS     = "AMBIGUOUS";
    public static final String KIND_UNKNOWN       = "UNKNOWN";

    // ── qualifier enum (ADR-HND-002) ───────────────────────────────────────────
    public static final String QUALIFIER_LINKED       = "LINKED";
    public static final String QUALIFIER_CTE          = "CTE";
    public static final String QUALIFIER_SUBQUERY     = "SUBQUERY";
    public static final String QUALIFIER_INFERRED     = "INFERRED";
    public static final String QUALIFIER_FUZZY        = "FUZZY";
    public static final String QUALIFIER_CTRL_FLOW    = "CTRL_FLOW";
    public static final String QUALIFIER_FN_VERIFIED  = "FN_VERIFIED";
    public static final String QUALIFIER_FN_UNVERIFIED = "FN_UNVERIFIED";

    // ── confidence enum (ADR-HND-003) ─────────────────────────────────────────
    public static final String CONFIDENCE_HIGH   = "HIGH";
    public static final String CONFIDENCE_MEDIUM = "MEDIUM";
    public static final String CONFIDENCE_LOW    = "LOW";
    public static final String CONFIDENCE_FUZZY  = "FUZZY";

    // ── pending_kind enum (ADR-HND-008) ─────────────────────────────────────────
    public static final String PENDING_KIND_PIPELINED = "PIPELINED";
    public static final String PENDING_KIND_ROWTYPE   = "ROWTYPE";
    public static final String PENDING_KIND_MULTISET  = "MULTISET";

    /** HAL2-03: default TTL for PENDING_INJECT atoms (milliseconds). */
    public static final long PENDING_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    // ── Legacy constants (used only for data migration) ────────────────────────
    public static final String LEGACY_STATUS_RESOLVED   = "Обработано";
    public static final String LEGACY_STATUS_UNRESOLVED = "Не разобрано";
    public static final String LEGACY_STATUS_UNBOUND    = "Не связано";

    private AtomInfo() {}
}
