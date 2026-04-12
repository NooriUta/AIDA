package com.hound.semantic.dialect.plsql;

import com.hound.semantic.engine.CanonicalTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps PL/SQL ANTLR token symbolic names to {@link CanonicalTokenType}.
 *
 * This is the dialect-specific bridge that keeps the engine layer free from
 * any knowledge of PlSqlLexer token names.
 *
 * Mapping rules (in priority order):
 * <ol>
 *   <li>Direct lookup in {@link #DIRECT} map</li>
 *   <li>Suffix rule: names ending with {@code _LETTER} → IDENTIFIER</li>
 *   <li>Fallback: UNKNOWN (logged once per unmapped name)</li>
 * </ol>
 *
 * @see CanonicalTokenType
 */
public final class PlSqlTokenMapper {

    private static final Logger logger = LoggerFactory.getLogger(PlSqlTokenMapper.class);

    private PlSqlTokenMapper() {}

    /** Names already warned about — avoids log spam for repeated tokens. */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    // ═══════ Direct mappings ═══════

    private static final Map<String, CanonicalTokenType> DIRECT = Map.ofEntries(
            // Identifiers
            Map.entry("REGULAR_ID",   CanonicalTokenType.IDENTIFIER),
            Map.entry("DELIMITED_ID", CanonicalTokenType.QUOTED_IDENTIFIER),
            Map.entry("ID",           CanonicalTokenType.IDENTIFIER),

            // Keywords used as identifiers
            Map.entry("NAME",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("VALUE",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("BRANCH",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("CLIENT",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SYSTEM",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("TYPE",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("OTHER",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("ROLE",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("DESCRIPTION", CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("OWNERSHIP",   CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SUBTYPE",     CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SCALE",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("USER",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("OWNER",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("DATA",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),

            // Literals
            Map.entry("CHAR_STRING",        CanonicalTokenType.STRING_LITERAL),
            Map.entry("UNSIGNED_INTEGER",   CanonicalTokenType.INTEGER_LITERAL),
            Map.entry("NUMERIC_LITERAL",    CanonicalTokenType.NUMERIC_LITERAL),
            Map.entry("APPROXIMATE_NUM_LIT", CanonicalTokenType.APPROX_NUM_LITERAL),

            // Date/Time — literals
            Map.entry("DATE_LITERAL",      CanonicalTokenType.DATE_LITERAL),
            Map.entry("TIMESTAMP_LITERAL", CanonicalTokenType.TIMESTAMP_LITERAL),

            // Date/Time — type keywords (CAST(x AS TIMESTAMP), variable declarations,
            // INTERVAL '1' DAY, DAY TO SECOND, TIMESTAMP WITH TIME ZONE, etc.)
            Map.entry("TIMESTAMP", CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("INTERVAL",  CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("DAY",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("HOUR",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("MINUTE",    CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SECOND",    CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("YEAR",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("MONTH",     CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("ZONE",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LOCAL",     CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("TIME",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),

            // Keywords-as-values
            Map.entry("NULL",         CanonicalTokenType.NULL),
            Map.entry("NULL_",        CanonicalTokenType.NULL),
            Map.entry("TRUE",         CanonicalTokenType.TRUE),
            Map.entry("FALSE",        CanonicalTokenType.FALSE),
            Map.entry("DEFAULT",      CanonicalTokenType.DEFAULT),
            Map.entry("SYSDATE",      CanonicalTokenType.SYSDATE),
            Map.entry("CURRENT_DATE", CanonicalTokenType.CURRENT_DATE),
            Map.entry("MINVALUE",     CanonicalTokenType.MINVALUE),

            // Pseudo-columns
            Map.entry("LEVEL",      CanonicalTokenType.LEVEL),
            Map.entry("ROWID",      CanonicalTokenType.ROWID),
            Map.entry("ROWNUM",     CanonicalTokenType.ROWNUM),
            Map.entry("ROW_NUMBER", CanonicalTokenType.ROW_NUMBER),
            Map.entry("UNBOUNDED",  CanonicalTokenType.UNBOUNDED),

            // Structural
            Map.entry("PERIOD",      CanonicalTokenType.PERIOD),
            Map.entry("LEFT_PAREN",  CanonicalTokenType.LEFT_PAREN),
            Map.entry("RIGHT_PAREN", CanonicalTokenType.RIGHT_PAREN),
            Map.entry("COMMA",       CanonicalTokenType.COMMA),

            // Whitespace
            Map.entry("SPACES", CanonicalTokenType.WHITESPACE),
            Map.entry("WS",     CanonicalTokenType.WHITESPACE),

            // Sequences
            Map.entry("NEXTVAL", CanonicalTokenType.NEXTVAL),
            Map.entry("CURRVAL", CanonicalTokenType.CURRVAL),

            // Special: DATE keyword (not DATE_LITERAL) used as identifier in ANSI date expressions
            Map.entry("DATE", CanonicalTokenType.IDENTIFIER),

            // Built-in functions / identifiers tokenized as keywords by PlSqlLexer
            Map.entry("TRUNC",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("TO_DATE",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("TO_CHAR",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("TO_NUMBER",      CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("NVL",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("NVL2",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("COALESCE",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("EXTRACT",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("UPPER",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LOWER",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("TRIM",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("RTRIM",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LTRIM",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LPAD",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("RPAD",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SUBSTR",         CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("REPLACE",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("INSTR",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LENGTH",         CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("DECODE",         CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("CHR",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("ROUND",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("CEIL",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("FLOOR",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("ABS",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("MOD",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SIGN",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("POWER",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SQRT",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("GREATEST",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LEAST",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("COUNT",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SUM",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("AVG",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("MIN",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("MAX",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LISTAGG",        CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("RANK",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("DENSE_RANK",     CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LEAD",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LAG",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("FIRST",          CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("LAST",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("CAST",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("XMLAGG",         CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("XMLELEMENT",     CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("REGEXP_REPLACE", CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("REGEXP_SUBSTR",  CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("REGEXP_INSTR",   CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("REGEXP_LIKE",    CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("REGEXP_COUNT",   CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("ORA_HASH",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SYS_CONTEXT",    CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SYS_GUID",       CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SYSTIMESTAMP",    CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("SOURCE",         CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("DDL",            CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("HASH",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),
            Map.entry("OVER",           CanonicalTokenType.KEYWORD_AS_IDENTIFIER),

            // Comparison / logical / arithmetic operators
            Map.entry("EQUALS_OP",       CanonicalTokenType.OPERATOR),
            Map.entry("NOT_EQUAL_OP",    CanonicalTokenType.OPERATOR),
            Map.entry("GREATER_THAN_OP", CanonicalTokenType.OPERATOR),
            Map.entry("LESS_THAN_OP",    CanonicalTokenType.OPERATOR),
            Map.entry("GREATER_THAN_OR_EQUALS_OP", CanonicalTokenType.OPERATOR),
            Map.entry("LESS_THAN_OR_EQUALS_OP",    CanonicalTokenType.OPERATOR),
            Map.entry("PLUS_SIGN",       CanonicalTokenType.OPERATOR),
            Map.entry("MINUS_SIGN",      CanonicalTokenType.OPERATOR),
            Map.entry("ASTERISK",        CanonicalTokenType.OPERATOR),
            Map.entry("SOLIDUS",         CanonicalTokenType.OPERATOR),
            Map.entry("BAR",             CanonicalTokenType.OPERATOR),
            Map.entry("CONCATENATION_OP", CanonicalTokenType.OPERATOR),

            // Bind variables (:1, :name, :X)
            Map.entry("BINDVAR",  CanonicalTokenType.BIND_VARIABLE),

            // SQL keywords (structural, not identifiers)
            Map.entry("AND",      CanonicalTokenType.SQL_KEYWORD),
            Map.entry("OR",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("NOT",      CanonicalTokenType.SQL_KEYWORD),
            Map.entry("IN",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("IS",       CanonicalTokenType.SQL_KEYWORD),
            Map.entry("BETWEEN",  CanonicalTokenType.SQL_KEYWORD),
            Map.entry("LIKE",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("CASE",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("WHEN",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("THEN",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("ELSE",     CanonicalTokenType.SQL_KEYWORD),
            Map.entry("END",      CanonicalTokenType.SQL_KEYWORD),
            Map.entry("EXISTS",   CanonicalTokenType.SQL_KEYWORD)
    );

    /**
     * Maps a PL/SQL ANTLR symbolic token name to a canonical type.
     *
     * @param plsqlTokenName the symbolic name from {@code PlSqlLexer.VOCABULARY}
     * @return the canonical type, never null (returns UNKNOWN for unmapped names)
     */
    public static CanonicalTokenType map(String plsqlTokenName) {
        if (plsqlTokenName == null || plsqlTokenName.isEmpty()) {
            return CanonicalTokenType.UNKNOWN;
        }

        // 1. Direct lookup
        CanonicalTokenType direct = DIRECT.get(plsqlTokenName);
        if (direct != null) return direct;

        // 2. Suffix rule: *_LETTER → IDENTIFIER
        if (plsqlTokenName.endsWith("_LETTER")) {
            return CanonicalTokenType.IDENTIFIER;
        }

        // 3. Unmapped — warn once, return UNKNOWN
        if (WARNED.add(plsqlTokenName)) {
            logger.warn("Unmapped PL/SQL token: '{}' → UNKNOWN", plsqlTokenName);
        }
        return CanonicalTokenType.UNKNOWN;
    }
}
