package com.hound.parser;

import com.hound.api.HoundEventListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ANTLR4 {@link org.antlr.v4.runtime.ANTLRErrorListener} for the PL/SQL grammar.
 *
 * <p>Classifies every ANTLR4 syntax event into two buckets:
 * <ul>
 *   <li><b>errors</b> — hard failures where the parser cannot recover:
 *       {@code token recognition error}, {@code no viable alternative}, and any message
 *       involving {@code '<EOF>'} (truncated / structurally broken file).
 *       These go into {@code ParseResult.errors()} and cause {@code isSuccess() = false}.
 *   <li><b>grammarLimitations</b> — recoverable ANTLR4 events (the parser continued):
 *       {@code extraneous input}, {@code missing <token>}, {@code mismatched input}.
 *       These typically mean a valid Oracle construct not fully covered by the grammar.
 *       They go into {@code ParseResult.warnings()} and do NOT affect {@code isSuccess()}.
 * </ul>
 *
 * <p>All items are also logged (errors at WARN, grammar limitations at DEBUG) and forwarded
 * to the supplied {@link HoundEventListener} ({@code onParseError} / {@code onParseWarning}).
 */
public class PlSqlErrorCollector extends BaseErrorListener {

    private static final Logger log = LoggerFactory.getLogger(PlSqlErrorCollector.class);

    /** Maximum number of errors/warnings collected per file (to avoid log spam). */
    private static final int MAX_ERRORS   = 50;
    private static final int MAX_WARNINGS = 100;

    /**
     * ANTLR4 error classification:
     *
     *   token recognition error at: 'X'         → lexer: char not in language       → ERROR
     *   no viable alternative at input 'X'       → parser cannot recover at all       → ERROR
     *   no viable alternative at input '<EOF>'   → structural, file cut off           → ERROR
     *   mismatched input '<EOF>' expecting {…}   → file truncated / missing END      → ERROR *
     *   missing 'X' at '<EOF>'                   → file truncated                     → ERROR *
     *   ─────────────────────────────────────────────────────────────────────────────────────
     *   extraneous input 'X' expecting {…}       → parser skips extra token, recovers → WARNING
     *   missing 'X' at 'Y'  / missing X at 'Y'  → parser inserts token,   recovers   → WARNING
     *   mismatched input 'X' expecting {…}       → type-ref not in grammar            → WARNING
     *
     * Rule: anything that causes the parser to RECOVER and CONTINUE → WARNING.
     *       Anything that causes the parser to FAIL at a statement  → ERROR.
     * (*) EOF variants are promoted back to ERROR via ERROR_OVERRIDE_PATTERNS.
     */

    // Substrings that classify a message as a grammar limitation (→ WARNING).
    // All three ANTLR4 recoverable-event prefixes are listed here.
    private static final List<String> GRAMMAR_LIMITATION_PATTERNS = List.of(
            // Parser skipped an unexpected token and continued — always recoverable.
            "extraneous input ",

            // Parser inserted a synthetic token and continued — always recoverable.
            // The token name may or may not be quoted, so we match the keyword alone.
            "missing ",

            // Parser found a token of the wrong type but could substitute/recover.
            // Covers all "mismatched input 'X' expecting {…}" messages regardless of
            // what X or the expecting-set are — grammar gaps, not source bugs.
            "mismatched input "

            // NOTE: "no viable alternative at input …" is intentionally absent.
            // The parser cannot recover from it; the statement is fully lost.
    );

    // Substrings that OVERRIDE grammar-limitation classification back to ERROR.
    // Applied after GRAMMAR_LIMITATION_PATTERNS — if any matches, it's an ERROR.
    private static final List<String> ERROR_OVERRIDE_PATTERNS = List.of(
            "'<EOF>'"   // …at '<EOF>' → file truncated / structurally broken → ERROR
    );

    private final String             file;
    private final HoundEventListener listener;
    private final List<String>       errors             = new ArrayList<>();
    private final List<String>       grammarLimitations = new ArrayList<>();

    public PlSqlErrorCollector(String file, HoundEventListener listener) {
        this.file     = file;
        this.listener = listener;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        String entry = "line " + line + ":" + charPositionInLine + " — " + msg;

        if (isGrammarLimitation(msg)) {
            if (grammarLimitations.size() >= MAX_WARNINGS) return;
            grammarLimitations.add(entry);
            log.debug("[ANTLR4-grammar] {} | {}", basename(file), entry);
            try {
                listener.onParseWarning(file, line, charPositionInLine, msg);
            } catch (Exception ex) {
                log.debug("[PlSqlErrorCollector] onParseWarning callback failed: {}", ex.getMessage());
            }
        } else {
            if (errors.size() >= MAX_ERRORS) return;
            errors.add(entry);
            log.warn("[ANTLR4] {} | {}", basename(file), entry);
            try {
                listener.onParseError(file, line, charPositionInLine, msg);
            } catch (Exception ex) {
                log.debug("[PlSqlErrorCollector] onParseError callback failed: {}", ex.getMessage());
            }
        }
    }

    /** Returns an unmodifiable list of genuine syntax errors (→ {@code ParseResult.errors()}). */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns an unmodifiable list of known grammar limitation notices
     * (→ {@code ParseResult.warnings()}).
     */
    public List<String> getGrammarLimitations() {
        return Collections.unmodifiableList(grammarLimitations);
    }

    /** True if any genuine syntax errors were collected. */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private static boolean isGrammarLimitation(String msg) {
        if (msg == null) return false;
        boolean matched = false;
        for (String pattern : GRAMMAR_LIMITATION_PATTERNS) {
            if (msg.contains(pattern)) { matched = true; break; }
        }
        if (!matched) return false;
        // Override: if message also contains an error-override pattern → treat as ERROR
        for (String override : ERROR_OVERRIDE_PATTERNS) {
            if (msg.contains(override)) return false;
        }
        return true;
    }

    private static String basename(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
