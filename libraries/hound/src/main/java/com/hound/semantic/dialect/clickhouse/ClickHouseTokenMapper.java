package com.hound.semantic.dialect.clickhouse;

import com.hound.semantic.engine.CanonicalTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps ClickHouse ANTLR token symbolic names to {@link CanonicalTokenType}.
 *
 * ClickHouse identifiers are CASE-SENSITIVE — this mapper must NOT apply toLowerCase().
 *
 * Mapping rules (in priority order):
 * <ol>
 *   <li>Direct lookup in {@link #DIRECT} map</li>
 *   <li>Fallback: UNKNOWN (logged once per unmapped name)</li>
 * </ol>
 *
 * @see CanonicalTokenType
 */
public final class ClickHouseTokenMapper {

    private static final Logger logger = LoggerFactory.getLogger(ClickHouseTokenMapper.class);

    private ClickHouseTokenMapper() {}

    /** Names already warned about — avoids log spam for repeated tokens. */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    // ═══════ Direct mappings ═══════

    private static final Map<String, CanonicalTokenType> DIRECT = Map.ofEntries(
            // Identifiers
            Map.entry("IDENTIFIER",                         CanonicalTokenType.IDENTIFIER),

            // Integer literals
            Map.entry("DECIMAL_LITERAL",                    CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("OCTAL_LITERAL",                      CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("HEXADECIMAL_NUMERIC_LITERAL",        CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("BINARY_NUMERIC_LITERAL",             CanonicalTokenType.INTEGER_LITERAL),

            // Floating-point literals
            Map.entry("FLOATING_LITERAL",                   CanonicalTokenType.NUMERIC_LITERAL),

            // Special numeric values
            Map.entry("INF",                                CanonicalTokenType.NUMERIC_LITERAL),
            Map.entry("NAN_SQL",                            CanonicalTokenType.NUMERIC_LITERAL),

            // String literals
            Map.entry("STRING_LITERAL",                     CanonicalTokenType.STRING_LITERAL),
            Map.entry("HEXADECIMAL_STRING_LITERAL",         CanonicalTokenType.STRING_LITERAL),
            Map.entry("BINARY_STRING_LITERAL",              CanonicalTokenType.STRING_LITERAL),

            // Keywords-as-values
            Map.entry("NULL_SQL",       CanonicalTokenType.NULL),
            Map.entry("JSON_TRUE",      CanonicalTokenType.TRUE),
            Map.entry("JSON_FALSE",     CanonicalTokenType.FALSE),

            // Structural
            Map.entry("DOT",            CanonicalTokenType.PERIOD),
            Map.entry("LPAREN",         CanonicalTokenType.LEFT_PAREN),
            Map.entry("RPAREN",         CanonicalTokenType.RIGHT_PAREN),
            Map.entry("COMMA",          CanonicalTokenType.COMMA),

            // Operators
            Map.entry("DOUBLE_COLON",   CanonicalTokenType.OPERATOR),
            Map.entry("ARROW",          CanonicalTokenType.OPERATOR),
            Map.entry("ASTERISK",       CanonicalTokenType.OPERATOR),
            Map.entry("SLASH",          CanonicalTokenType.OPERATOR),
            Map.entry("DASH",           CanonicalTokenType.OPERATOR),
            Map.entry("PLUS",           CanonicalTokenType.OPERATOR),
            Map.entry("PERCENT",        CanonicalTokenType.OPERATOR),
            Map.entry("CONCAT",         CanonicalTokenType.OPERATOR),
            Map.entry("EQ_SINGLE",      CanonicalTokenType.OPERATOR),
            Map.entry("EQ_DOUBLE",      CanonicalTokenType.OPERATOR),
            Map.entry("NOT_EQ",         CanonicalTokenType.OPERATOR),
            Map.entry("LE",             CanonicalTokenType.OPERATOR),
            Map.entry("GE",             CanonicalTokenType.OPERATOR),
            Map.entry("LT",             CanonicalTokenType.OPERATOR),
            Map.entry("GT",             CanonicalTokenType.OPERATOR),
            Map.entry("HASH",           CanonicalTokenType.OPERATOR),
            Map.entry("TILDE",          CanonicalTokenType.OPERATOR),

            // SQL keywords
            Map.entry("AND",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("OR",         CanonicalTokenType.SQL_KEYWORD),
            Map.entry("NOT",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("IN",         CanonicalTokenType.SQL_KEYWORD),
            Map.entry("IS",         CanonicalTokenType.SQL_KEYWORD),
            Map.entry("LIKE",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ILIKE",      CanonicalTokenType.SQL_KEYWORD),
            Map.entry("BETWEEN",    CanonicalTokenType.SQL_KEYWORD),
            Map.entry("CASE",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("WHEN",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("THEN",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ELSE",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("END",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("GLOBAL",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ASOF",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("SEMI",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ANTI",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("EXISTS",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ALL",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ANY",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("DISTINCT",   CanonicalTokenType.SQL_KEYWORD),

            // Whitespace
            Map.entry("WS",             CanonicalTokenType.WHITESPACE),
            Map.entry("COMMENT",        CanonicalTokenType.WHITESPACE),
            Map.entry("LINE_COMMENT",   CanonicalTokenType.WHITESPACE)
    );

    /**
     * Maps a ClickHouse ANTLR symbolic token name to a canonical type.
     *
     * @param tokenName the symbolic name from {@code ClickHouseLexer.VOCABULARY}
     * @return the canonical type, never null (returns UNKNOWN for unmapped names)
     */
    public static CanonicalTokenType map(String tokenName) {
        if (tokenName == null || tokenName.isEmpty()) {
            return CanonicalTokenType.UNKNOWN;
        }

        // 1. Direct lookup
        CanonicalTokenType direct = DIRECT.get(tokenName);
        if (direct != null) return direct;

        // 2. Unmapped — warn once, return UNKNOWN
        if (WARNED.add(tokenName)) {
            logger.warn("Unmapped ClickHouse token: '{}' → UNKNOWN", tokenName);
        }
        return CanonicalTokenType.UNKNOWN;
    }
}
