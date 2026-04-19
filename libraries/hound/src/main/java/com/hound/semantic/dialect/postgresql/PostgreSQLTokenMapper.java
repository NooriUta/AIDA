package com.hound.semantic.dialect.postgresql;

import com.hound.semantic.engine.CanonicalTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps PostgreSQL ANTLR token symbolic names to {@link CanonicalTokenType}.
 *
 * Mapping rules (in priority order):
 * <ol>
 *   <li>Direct lookup in {@link #DIRECT} map</li>
 *   <li>Contains "Identifier" → IDENTIFIER</li>
 *   <li>Contains "Constant" or "Const" → STRING_LITERAL</li>
 *   <li>Fallback: UNKNOWN (logged once per unmapped name)</li>
 * </ol>
 *
 * @see CanonicalTokenType
 */
public final class PostgreSQLTokenMapper {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLTokenMapper.class);

    private PostgreSQLTokenMapper() {}

    /** Names already warned about — avoids log spam for repeated tokens. */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    // ═══════ Direct mappings ═══════

    private static final Map<String, CanonicalTokenType> DIRECT = Map.ofEntries(
            // Identifiers
            Map.entry("Identifier",                     CanonicalTokenType.IDENTIFIER),
            Map.entry("QuotedIdentifier",               CanonicalTokenType.QUOTED_IDENTIFIER),
            Map.entry("UnicodeQuotedIdentifier",        CanonicalTokenType.QUOTED_IDENTIFIER),

            // String literals
            Map.entry("StringConstant",                 CanonicalTokenType.STRING_LITERAL),
            Map.entry("BeginEscapeStringConstant",      CanonicalTokenType.STRING_LITERAL),
            Map.entry("EscapeStringConstant",           CanonicalTokenType.STRING_LITERAL),
            Map.entry("UnicodeEscapeStringConstant",    CanonicalTokenType.STRING_LITERAL),
            Map.entry("DollarQuotedStringConstant",     CanonicalTokenType.STRING_LITERAL),
            Map.entry("BeginDollarStringConstant",      CanonicalTokenType.STRING_LITERAL),
            Map.entry("Text_between_Dollar",            CanonicalTokenType.STRING_LITERAL),
            Map.entry("EndDollarStringConstant",        CanonicalTokenType.STRING_LITERAL),

            // Numeric literals
            Map.entry("Integral",                       CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("BinaryIntegral",                 CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("OctalIntegral",                  CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("HexadecimalIntegral",            CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("Numeric",                        CanonicalTokenType.NUMERIC_LITERAL),

            // Keywords-as-values
            Map.entry("NULL_P",         CanonicalTokenType.NULL),
            Map.entry("TRUE_P",         CanonicalTokenType.TRUE),
            Map.entry("FALSE_P",        CanonicalTokenType.FALSE),
            Map.entry("DEFAULT",        CanonicalTokenType.DEFAULT),
            Map.entry("CURRENT_DATE",   CanonicalTokenType.CURRENT_DATE),
            Map.entry("CURRENT_TIME",   CanonicalTokenType.CURRENT_DATE),
            Map.entry("CURRENT_TIMESTAMP", CanonicalTokenType.CURRENT_DATE),

            // Structural
            Map.entry("DOT",            CanonicalTokenType.PERIOD),
            Map.entry("OPEN_PAREN",     CanonicalTokenType.LEFT_PAREN),
            Map.entry("CLOSE_PAREN",    CanonicalTokenType.RIGHT_PAREN),
            Map.entry("COMMA",          CanonicalTokenType.COMMA),

            // Bind variable ($1, $2, ...)
            Map.entry("PARAM",          CanonicalTokenType.BIND_VARIABLE),

            // Operators
            Map.entry("TYPECAST",       CanonicalTokenType.OPERATOR),
            Map.entry("PLUS",           CanonicalTokenType.OPERATOR),
            Map.entry("MINUS",          CanonicalTokenType.OPERATOR),
            Map.entry("STAR",           CanonicalTokenType.OPERATOR),
            Map.entry("SLASH",          CanonicalTokenType.OPERATOR),
            Map.entry("EQUAL",          CanonicalTokenType.OPERATOR),
            Map.entry("NOT_EQUALS",     CanonicalTokenType.OPERATOR),
            Map.entry("LT",             CanonicalTokenType.OPERATOR),
            Map.entry("GT",             CanonicalTokenType.OPERATOR),
            Map.entry("LTH",            CanonicalTokenType.OPERATOR),
            Map.entry("GTH",            CanonicalTokenType.OPERATOR),
            Map.entry("PERCENT",        CanonicalTokenType.OPERATOR),
            Map.entry("CARET",          CanonicalTokenType.OPERATOR),
            Map.entry("PIPE",           CanonicalTokenType.OPERATOR),
            Map.entry("PIPE_PIPE",      CanonicalTokenType.OPERATOR),
            Map.entry("AMPERSAND",      CanonicalTokenType.OPERATOR),
            Map.entry("Operator",       CanonicalTokenType.OPERATOR),

            // SQL keywords
            Map.entry("AND",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("OR",         CanonicalTokenType.SQL_KEYWORD),
            Map.entry("NOT",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("IN_P",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("IS",         CanonicalTokenType.SQL_KEYWORD),
            Map.entry("BETWEEN",    CanonicalTokenType.SQL_KEYWORD),
            Map.entry("LIKE",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ILIKE",      CanonicalTokenType.SQL_KEYWORD),
            Map.entry("SIMILAR",    CanonicalTokenType.SQL_KEYWORD),
            Map.entry("CASE",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("WHEN",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("THEN",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ELSE",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("END_P",      CanonicalTokenType.SQL_KEYWORD),
            Map.entry("EXISTS",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ALL",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ANY",        CanonicalTokenType.SQL_KEYWORD),
            Map.entry("SOME",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("DISTINCT",   CanonicalTokenType.SQL_KEYWORD),
            Map.entry("NULLS_P",    CanonicalTokenType.SQL_KEYWORD),

            // Whitespace
            Map.entry("Whitespace", CanonicalTokenType.WHITESPACE),
            Map.entry("Newline",    CanonicalTokenType.WHITESPACE)
    );

    /**
     * Maps a PostgreSQL ANTLR symbolic token name to a canonical type.
     *
     * @param tokenName the symbolic name from {@code PostgreSQLLexer.VOCABULARY}
     * @return the canonical type, never null (returns UNKNOWN for unmapped names)
     */
    public static CanonicalTokenType map(String tokenName) {
        if (tokenName == null || tokenName.isEmpty()) {
            return CanonicalTokenType.UNKNOWN;
        }

        // 1. Direct lookup
        CanonicalTokenType direct = DIRECT.get(tokenName);
        if (direct != null) return direct;

        // 2. Contains "Identifier" → IDENTIFIER
        if (tokenName.contains("Identifier")) {
            return CanonicalTokenType.IDENTIFIER;
        }

        // 3. Contains "Constant" or "Const" → STRING_LITERAL
        if (tokenName.contains("Constant") || tokenName.contains("Const")) {
            return CanonicalTokenType.STRING_LITERAL;
        }

        // 4. Unmapped — warn once, return UNKNOWN
        if (WARNED.add(tokenName)) {
            logger.warn("Unmapped PostgreSQL token: '{}' → UNKNOWN", tokenName);
        }
        return CanonicalTokenType.UNKNOWN;
    }
}
