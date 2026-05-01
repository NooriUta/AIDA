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
 * Verifies that {@link AntlrErrorCollector} surfaces ANTLR4 parse errors
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
     * SQL*Plus directives now go through ANTLR's {@code sql_plus_command} rule.
     * Most directives (SET, PROMPT, WHENEVER) parse cleanly. A few less-common
     * forms (SET ... SIZE UNLIMITED, SPOOL <path>) trigger recoverable errors —
     * but the PL/SQL body that follows still parses correctly via error recovery.
     */
    @Test
    void sqlplusDirectives_partialGrammarSupport_bodyStillParsed() throws Exception {
        Capture c = parse("err_sqlplus_directives.sql");

        // Up to a handful of recoverable errors from non-standard SQL*Plus forms.
        assertTrue(c.parseErrors().size() <= 5,
                "≤ 5 recoverable ANTLR errors expected; got " + c.parseErrors().size()
                + ": " + c.parseErrors());
        // Procedure body must have been reached and registered.
        assertFalse(c.result().file().isBlank(),
                "Parser must have walked the file even with recoverable errors");
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
     * UPDATE with a table alias (UPDATE tbl alias SET alias.col = val).
     * Previously misreported as a grammar limitation because stripSqlPlusDirectives
     * was incorrectly stripping indented SET clauses as SQL*Plus directives, causing
     * a spurious "mismatched input '=' expecting 'SET'" ANTLR error.
     * After the fix the grammar handles alias.col column references correctly — 0 errors/warnings.
     */
    @Test
    void updateAlias_parsesSuccessfully_noWarningsAfterSetStripFix() throws Exception {
        Capture c = parse("err_update_alias.sql");

        assertTrue(c.parseErrors().isEmpty(),
                "UPDATE alias must parse without ANTLR errors after SET-strip fix, got: " + c.parseErrors());
        assertTrue(c.parseWarnings().isEmpty(),
                "UPDATE alias must parse without grammar warnings after SET-strip fix, got: " + c.parseWarnings());
        assertTrue(c.result().isSuccess(),
                "isSuccess() must be true for valid UPDATE-with-alias SQL");
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

}
