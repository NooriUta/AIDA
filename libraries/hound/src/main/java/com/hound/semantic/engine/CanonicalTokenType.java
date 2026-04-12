package com.hound.semantic.engine;

/**
 * Canonical token types for dialect-agnostic atom classification.
 *
 * Each ANTLR grammar defines its own token names (REGULAR_ID, ID_, Identifier, etc.).
 * This enum provides a single canonical vocabulary so that AtomProcessor and other
 * engine components never depend on dialect-specific token names.
 *
 * Dialect listeners normalize raw ANTLR token names into these canonical types
 * via a TokenMapper (e.g. PlSqlTokenMapper) before passing tokenDetails to the engine.
 *
 * @see com.hound.semantic.dialect.plsql.PlSqlTokenMapper
 */
public enum CanonicalTokenType {

    // ═══════ Identifiers ═══════
    IDENTIFIER,
    QUOTED_IDENTIFIER,
    KEYWORD_AS_IDENTIFIER,

    // ═══════ Literals ═══════
    STRING_LITERAL,
    INTEGER_LITERAL,
    NUMERIC_LITERAL,
    APPROX_NUM_LITERAL,

    // ═══════ Date/Time ═══════
    DATE_LITERAL,
    TIMESTAMP_LITERAL,

    // ═══════ Keywords-as-values ═══════
    NULL,
    TRUE,
    FALSE,
    DEFAULT,
    SYSDATE,
    CURRENT_DATE,
    MINVALUE,

    // ═══════ Pseudo-columns ═══════
    LEVEL,
    ROWID,
    ROWNUM,
    ROW_NUMBER,
    UNBOUNDED,

    // ═══════ Structural ═══════
    PERIOD,
    LEFT_PAREN,
    RIGHT_PAREN,
    COMMA,

    // ═══════ Sequences ═══════
    NEXTVAL,
    CURRVAL,

    // ═══════ Operators / SQL keywords ═══════
    OPERATOR,
    SQL_KEYWORD,

    // ═══════ Bind variables ═══════
    BIND_VARIABLE,

    // ═══════ Special ═══════
    WHITESPACE,
    UNKNOWN;

    public boolean isIdentifier() {
        return this == IDENTIFIER || this == QUOTED_IDENTIFIER || this == KEYWORD_AS_IDENTIFIER;
    }

    public boolean isConstant() {
        return switch (this) {
            case STRING_LITERAL, INTEGER_LITERAL, NUMERIC_LITERAL, APPROX_NUM_LITERAL,
                 DATE_LITERAL, TIMESTAMP_LITERAL,
                 NULL, TRUE, FALSE, DEFAULT, SYSDATE, CURRENT_DATE, MINVALUE,
                 BIND_VARIABLE -> true;
            default -> false;
        };
    }

    public boolean isSystemPseudoColumn() {
        return this == LEVEL || this == ROWID || this == ROWNUM
                || this == ROW_NUMBER || this == UNBOUNDED;
    }

    public boolean isSequenceOp() {
        return this == NEXTVAL || this == CURRVAL;
    }

    /**
     * Safe parse — returns UNKNOWN instead of throwing for invalid names.
     */
    public static CanonicalTokenType fromString(String name) {
        if (name == null || name.isEmpty()) return UNKNOWN;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
