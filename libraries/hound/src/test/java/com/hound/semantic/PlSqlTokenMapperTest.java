package com.hound.semantic;

import com.hound.semantic.dialect.plsql.PlSqlTokenMapper;
import com.hound.semantic.engine.CanonicalTokenType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlSqlTokenMapperTest {

    @Test
    void directMapping_identifiers() {
        assertEquals(CanonicalTokenType.IDENTIFIER, PlSqlTokenMapper.map("REGULAR_ID"));
        assertEquals(CanonicalTokenType.QUOTED_IDENTIFIER, PlSqlTokenMapper.map("DELIMITED_ID"));
        assertEquals(CanonicalTokenType.IDENTIFIER, PlSqlTokenMapper.map("ID"));
    }

    @Test
    void directMapping_keywordsAsIdentifiers() {
        assertEquals(CanonicalTokenType.KEYWORD_AS_IDENTIFIER, PlSqlTokenMapper.map("NAME"));
        assertEquals(CanonicalTokenType.KEYWORD_AS_IDENTIFIER, PlSqlTokenMapper.map("VALUE"));
        assertEquals(CanonicalTokenType.KEYWORD_AS_IDENTIFIER, PlSqlTokenMapper.map("TYPE"));
        assertEquals(CanonicalTokenType.KEYWORD_AS_IDENTIFIER, PlSqlTokenMapper.map("ROLE"));
        assertEquals(CanonicalTokenType.KEYWORD_AS_IDENTIFIER, PlSqlTokenMapper.map("USER"));
        assertEquals(CanonicalTokenType.KEYWORD_AS_IDENTIFIER, PlSqlTokenMapper.map("DATA"));
    }

    @Test
    void directMapping_literals() {
        assertEquals(CanonicalTokenType.STRING_LITERAL, PlSqlTokenMapper.map("CHAR_STRING"));
        assertEquals(CanonicalTokenType.INTEGER_LITERAL, PlSqlTokenMapper.map("UNSIGNED_INTEGER"));
        assertEquals(CanonicalTokenType.NUMERIC_LITERAL, PlSqlTokenMapper.map("NUMERIC_LITERAL"));
        assertEquals(CanonicalTokenType.APPROX_NUM_LITERAL, PlSqlTokenMapper.map("APPROXIMATE_NUM_LIT"));
    }

    @Test
    void directMapping_dateTime() {
        assertEquals(CanonicalTokenType.DATE_LITERAL, PlSqlTokenMapper.map("DATE_LITERAL"));
        assertEquals(CanonicalTokenType.TIMESTAMP_LITERAL, PlSqlTokenMapper.map("TIMESTAMP_LITERAL"));
    }

    @Test
    void directMapping_keywordsAsValues() {
        assertEquals(CanonicalTokenType.NULL, PlSqlTokenMapper.map("NULL"));
        assertEquals(CanonicalTokenType.NULL, PlSqlTokenMapper.map("NULL_"));
        assertEquals(CanonicalTokenType.TRUE, PlSqlTokenMapper.map("TRUE"));
        assertEquals(CanonicalTokenType.FALSE, PlSqlTokenMapper.map("FALSE"));
        assertEquals(CanonicalTokenType.DEFAULT, PlSqlTokenMapper.map("DEFAULT"));
        assertEquals(CanonicalTokenType.SYSDATE, PlSqlTokenMapper.map("SYSDATE"));
        assertEquals(CanonicalTokenType.CURRENT_DATE, PlSqlTokenMapper.map("CURRENT_DATE"));
        assertEquals(CanonicalTokenType.MINVALUE, PlSqlTokenMapper.map("MINVALUE"));
    }

    @Test
    void directMapping_pseudoColumns() {
        assertEquals(CanonicalTokenType.LEVEL, PlSqlTokenMapper.map("LEVEL"));
        assertEquals(CanonicalTokenType.ROWID, PlSqlTokenMapper.map("ROWID"));
        assertEquals(CanonicalTokenType.ROWNUM, PlSqlTokenMapper.map("ROWNUM"));
        assertEquals(CanonicalTokenType.ROW_NUMBER, PlSqlTokenMapper.map("ROW_NUMBER"));
        assertEquals(CanonicalTokenType.UNBOUNDED, PlSqlTokenMapper.map("UNBOUNDED"));
    }

    @Test
    void directMapping_structural() {
        assertEquals(CanonicalTokenType.PERIOD, PlSqlTokenMapper.map("PERIOD"));
        assertEquals(CanonicalTokenType.LEFT_PAREN, PlSqlTokenMapper.map("LEFT_PAREN"));
        assertEquals(CanonicalTokenType.RIGHT_PAREN, PlSqlTokenMapper.map("RIGHT_PAREN"));
        assertEquals(CanonicalTokenType.COMMA, PlSqlTokenMapper.map("COMMA"));
    }

    @Test
    void directMapping_whitespace() {
        assertEquals(CanonicalTokenType.WHITESPACE, PlSqlTokenMapper.map("SPACES"));
        assertEquals(CanonicalTokenType.WHITESPACE, PlSqlTokenMapper.map("WS"));
    }

    @Test
    void directMapping_sequences() {
        assertEquals(CanonicalTokenType.NEXTVAL, PlSqlTokenMapper.map("NEXTVAL"));
        assertEquals(CanonicalTokenType.CURRVAL, PlSqlTokenMapper.map("CURRVAL"));
    }

    @Test
    void directMapping_dateKeyword() {
        // DATE keyword (not DATE_LITERAL) maps to IDENTIFIER
        assertEquals(CanonicalTokenType.IDENTIFIER, PlSqlTokenMapper.map("DATE"));
    }

    @Test
    void suffixRule_letterTokens() {
        assertEquals(CanonicalTokenType.IDENTIFIER, PlSqlTokenMapper.map("A_LETTER"));
        assertEquals(CanonicalTokenType.IDENTIFIER, PlSqlTokenMapper.map("X_LETTER"));
        assertEquals(CanonicalTokenType.IDENTIFIER, PlSqlTokenMapper.map("SOME_LONG_LETTER"));
    }

    @Test
    void unmapped_returnsUnknown() {
        assertEquals(CanonicalTokenType.UNKNOWN, PlSqlTokenMapper.map("COMPLETELY_UNKNOWN_TOKEN"));
        assertEquals(CanonicalTokenType.UNKNOWN, PlSqlTokenMapper.map("SEMICOLON"));
    }

    @Test
    void directMapping_bindVariable() {
        // S1.BUG-2: BINDVAR token → BIND_VARIABLE canonical type
        assertEquals(CanonicalTokenType.BIND_VARIABLE, PlSqlTokenMapper.map("BINDVAR"),
                "BINDVAR PlSql token must map to BIND_VARIABLE");
    }

    @Test
    void nullAndEmpty_returnsUnknown() {
        assertEquals(CanonicalTokenType.UNKNOWN, PlSqlTokenMapper.map(null));
        assertEquals(CanonicalTokenType.UNKNOWN, PlSqlTokenMapper.map(""));
    }
}
