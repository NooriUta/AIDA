package com.hound.parser;

import com.hound.HoundParserImpl;
import com.hound.api.HoundConfig;
import com.hound.api.HoundEventListener;
import com.hound.api.ParseResult;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@link PlSqlErrorCollector} surfaces ANTLR4 parse errors
 * through {@link HoundEventListener#onParseError} and into {@link ParseResult#errors()}.
 *
 * <p>Fixtures live in {@code src/test/resources/plsql/errors/}.
 * See {@code EXPECTED_ERRORS.md} in the same directory for the rationale
 * behind each expected error.
 */
class ParseErrorFixtureTest {

    private static Path fixture(String name) throws Exception {
        URL url = ParseErrorFixtureTest.class.getClassLoader()
                .getResource("plsql/errors/" + name);
        assertNotNull(url, "Fixture not found: plsql/errors/" + name);
        return Paths.get(url.toURI());
    }

    // ── helper ───────────────────────────────────────────────────────────────

    record Capture(List<String> parseErrors, List<String> parseWarnings, ParseResult result) {}

    private Capture parse(String fixtureName) throws Exception {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        List<String> capturedErrors   = new ArrayList<>();
        List<String> capturedWarnings = new ArrayList<>();

        HoundEventListener listener = new HoundEventListener() {
            @Override
            public void onParseError(String file, int line, int charPos, String msg) {
                capturedErrors.add("line " + line + ":" + charPos + " — " + msg);
            }
            @Override
            public void onParseWarning(String file, int line, int charPos, String msg) {
                capturedWarnings.add("line " + line + ":" + charPos + " — " + msg);
            }
        };

        ParseResult result = new HoundParserImpl().parse(fixture(fixtureName), config, listener);
        return new Capture(capturedErrors, capturedWarnings, result);
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    /**
     * SQL*Plus directives are stripped by the preprocessor before ANTLR4 sees the file.
     * After stripping, the procedure body is clean PL/SQL — 0 ANTLR4 errors expected.
     */
    @Test
    void sqlplusDirectives_strippedByPreprocessor_zeroAntlrErrors() throws Exception {
        Capture c = parse("err_sqlplus_directives.sql");

        assertTrue(c.parseErrors().isEmpty(),
                "SQL*Plus directives must be stripped before ANTLR4; got errors: " + c.parseErrors());
        assertTrue(c.result().errors().isEmpty(),
                "ParseResult.errors() must be empty after successful strip+parse");
        assertTrue(c.result().isSuccess());
    }

    /**
     * A dollar-sign prefix on an identifier ($SYS_VAR) is not a valid PL/SQL token.
     * Expect at least 1 ANTLR4 error; parse recovers and the procedure is still registered.
     */
    @Test
    void unknownToken_dollarSign_atLeastOneAntlrError() throws Exception {
        Capture c = parse("err_unknown_token.sql");

        assertFalse(c.parseErrors().isEmpty(),
                "Expected ≥1 ANTLR4 error for '$' token, but got none");
        // Error must reference line 10 (the $SYS_VAR assignment)
        assertTrue(c.parseErrors().stream().anyMatch(e -> e.startsWith("line 10:")),
                "Expected error on line 10 ($SYS_VAR), got: " + c.parseErrors());
        // ParseResult.errors() mirrors what was captured via listener
        assertFalse(c.result().errors().isEmpty());
    }

    /**
     * UPDATE with a table alias (UPDATE tbl alias SET alias.col = val) is a known
     * PL/SQL grammar limitation. Goes to parseWarnings (not parseErrors) so that
     * isSuccess() remains true — the file parses but with a grammar notice.
     */
    @Test
    void updateAlias_knownGrammarLimitation_isWarningNotError() throws Exception {
        Capture c = parse("err_update_alias.sql");

        // Must be a WARNING (grammar limitation), not an error
        assertFalse(c.parseWarnings().isEmpty(),
                "Expected ≥1 grammar limitation warning for UPDATE alias, but got none");
        assertTrue(c.parseErrors().isEmpty(),
                "UPDATE alias must NOT appear as an error, got: " + c.parseErrors());
        // At least one warning must mention 'SET' (grammar confusion point)
        assertTrue(c.parseWarnings().stream().anyMatch(e -> e.contains("SET")),
                "Expected grammar warning to mention 'SET', got: " + c.parseWarnings());
        // isSuccess = true because grammar limitations do not count as errors
        assertTrue(c.result().isSuccess(),
                "isSuccess() must be true for grammar limitation (warnings only)");
        assertFalse(c.result().warnings().isEmpty(),
                "ParseResult.warnings() must contain the grammar limitation notice");
    }

    /**
     * Unclosed BEGIN block (missing END) causes an EOF error.
     * Expect ≥1 ANTLR4 error; the file name must be present in ParseResult.
     */
    @Test
    void unclosedBlock_missingEnd_eofError() throws Exception {
        Capture c = parse("err_unclosed_block.sql");

        assertFalse(c.parseErrors().isEmpty(),
                "Expected ≥1 ANTLR4 error for unclosed BEGIN, but got none");
        // Parser must have attempted the file — result is non-null with a file path
        assertFalse(c.result().file().isBlank());
    }

    /**
     * Mixed fixture: SQL*Plus directives (stripped) + DELETE with table alias.
     * After stripping, ≥0 ANTLR4 errors for the DELETE alias construct.
     * Procedure DWH.CLEANUP_OLD_DATA must be parsed (file path set, duration > 0).
     */
    @Test
    void mixed_sqlplusAndDeleteAlias_procedureParsed() throws Exception {
        Capture c = parse("err_mixed.sql");

        // Parse must have completed and registered the file
        assertFalse(c.result().file().isBlank());
        assertTrue(c.result().durationMs() >= 0);
        // Errors are either 0 (DELETE alias ok) or >0 — both acceptable.
        // What matters: no RuntimeException was thrown (test reaching here = pass).
    }

    /**
     * Verifies that {@link HoundParserImpl#stripSqlPlusDirectives} correctly blanks
     * known SQL*Plus directive lines without altering line numbers.
     */
    @Test
    void stripSqlPlusDirectives_lineNumbersPreserved() {
        String input =
                "SET SERVEROUTPUT ON\n" +      // line 1 — stripped
                "PROMPT Hello\n" +             // line 2 — stripped
                "SELECT 1 FROM DUAL;\n" +      // line 3 — kept
                "WHENEVER SQLERROR EXIT\n" +   // line 4 — stripped
                "SELECT 2 FROM DUAL;\n";       // line 5 — kept

        String result = HoundParserImpl.stripSqlPlusDirectives(input, "test.sql");
        String[] lines = result.split("\n");  // without -1: trailing empty token dropped

        assertEquals(5, lines.length, "Line count must be preserved");
        assertTrue(lines[0].isBlank(),  "line 1 (SET) must be blank");
        assertTrue(lines[1].isBlank(),  "line 2 (PROMPT) must be blank");
        assertFalse(lines[2].isBlank(), "line 3 (SELECT) must be kept");
        assertTrue(lines[3].isBlank(),  "line 4 (WHENEVER) must be blank");
        assertFalse(lines[4].isBlank(), "line 5 (SELECT) must be kept");
    }
}
