package com.hound.semantic;

import com.hound.semantic.engine.CanonicalTokenType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CanonicalTokenTypeTest {

    @Test
    void isIdentifier_coversAllIdentifierTypes() {
        assertTrue(CanonicalTokenType.IDENTIFIER.isIdentifier());
        assertTrue(CanonicalTokenType.QUOTED_IDENTIFIER.isIdentifier());
        assertTrue(CanonicalTokenType.KEYWORD_AS_IDENTIFIER.isIdentifier());
        assertFalse(CanonicalTokenType.STRING_LITERAL.isIdentifier());
        assertFalse(CanonicalTokenType.PERIOD.isIdentifier());
        assertFalse(CanonicalTokenType.UNKNOWN.isIdentifier());
    }

    @Test
    void isConstant_coversLiteralsAndKeywordValues() {
        assertTrue(CanonicalTokenType.STRING_LITERAL.isConstant());
        assertTrue(CanonicalTokenType.INTEGER_LITERAL.isConstant());
        assertTrue(CanonicalTokenType.NUMERIC_LITERAL.isConstant());
        assertTrue(CanonicalTokenType.APPROX_NUM_LITERAL.isConstant());
        assertTrue(CanonicalTokenType.DATE_LITERAL.isConstant());
        assertTrue(CanonicalTokenType.TIMESTAMP_LITERAL.isConstant());
        assertTrue(CanonicalTokenType.NULL.isConstant());
        assertTrue(CanonicalTokenType.TRUE.isConstant());
        assertTrue(CanonicalTokenType.FALSE.isConstant());
        assertTrue(CanonicalTokenType.DEFAULT.isConstant());
        assertTrue(CanonicalTokenType.SYSDATE.isConstant());
        assertTrue(CanonicalTokenType.CURRENT_DATE.isConstant());
        assertTrue(CanonicalTokenType.MINVALUE.isConstant());

        assertFalse(CanonicalTokenType.IDENTIFIER.isConstant());
        assertFalse(CanonicalTokenType.PERIOD.isConstant());
        assertFalse(CanonicalTokenType.LEVEL.isConstant());
    }

    @Test
    void isSystemPseudoColumn_coversAllPseudoColumns() {
        assertTrue(CanonicalTokenType.LEVEL.isSystemPseudoColumn());
        assertTrue(CanonicalTokenType.ROWID.isSystemPseudoColumn());
        assertTrue(CanonicalTokenType.ROWNUM.isSystemPseudoColumn());
        assertTrue(CanonicalTokenType.ROW_NUMBER.isSystemPseudoColumn());
        assertTrue(CanonicalTokenType.UNBOUNDED.isSystemPseudoColumn());

        assertFalse(CanonicalTokenType.IDENTIFIER.isSystemPseudoColumn());
        assertFalse(CanonicalTokenType.NULL.isSystemPseudoColumn());
    }

    @Test
    void bindVariable_isConstant() {
        assertTrue(CanonicalTokenType.BIND_VARIABLE.isConstant(),
                "BIND_VARIABLE must be treated as a constant (S1.BUG-2)");
        assertFalse(CanonicalTokenType.BIND_VARIABLE.isIdentifier());
        assertFalse(CanonicalTokenType.BIND_VARIABLE.isSystemPseudoColumn());
    }

    @Test
    void isSequenceOp_coversNextvalAndCurrval() {
        assertTrue(CanonicalTokenType.NEXTVAL.isSequenceOp());
        assertTrue(CanonicalTokenType.CURRVAL.isSequenceOp());
        assertFalse(CanonicalTokenType.IDENTIFIER.isSequenceOp());
    }

    @Test
    void fromString_validNames() {
        assertEquals(CanonicalTokenType.IDENTIFIER, CanonicalTokenType.fromString("IDENTIFIER"));
        assertEquals(CanonicalTokenType.PERIOD, CanonicalTokenType.fromString("PERIOD"));
        assertEquals(CanonicalTokenType.STRING_LITERAL, CanonicalTokenType.fromString("STRING_LITERAL"));
    }

    @Test
    void fromString_invalidOrNull_returnsUnknown() {
        assertEquals(CanonicalTokenType.UNKNOWN, CanonicalTokenType.fromString(null));
        assertEquals(CanonicalTokenType.UNKNOWN, CanonicalTokenType.fromString(""));
        assertEquals(CanonicalTokenType.UNKNOWN, CanonicalTokenType.fromString("NONEXISTENT_TYPE"));
        assertEquals(CanonicalTokenType.UNKNOWN, CanonicalTokenType.fromString("REGULAR_ID"));
    }
}
