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
 * <p>Classifies every syntax error into two buckets:
 * <ul>
 *   <li><b>errors</b> — genuine syntax problems in the source file (token recognition errors,
 *       structural issues like missing END, mismatched tokens). These go into
 *       {@code ParseResult.errors()} and cause {@code isSuccess() = false}.
 *   <li><b>grammarLimitations</b> — valid Oracle SQL constructs not supported by the ANTLR4
 *       grammar (e.g. {@code DATE 'YYYY-MM-DD'} in DEFAULT clauses, UPDATE with table alias).
 *       These go into {@code ParseResult.warnings()} and do NOT affect {@code isSuccess()}.
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
     * Message substrings that identify known ANTLR4 grammar limitations —
     * valid Oracle SQL constructs missing from the grammar.
     * These become warnings, not errors.
     */
    /**
     * ANTLR4 error classification:
     *
     *   token recognition error at: 'X'         → lexer: char not in language       → ERROR
     *   mismatched input '<EOF>' expecting {…}   → file truncated / missing END      → ERROR
     *   no viable alternative at input '<EOF>'   → structural, file cut off           → ERROR
     *   no viable alternative at input 'X'       → parser cannot recover at all       → ERROR
     *   ─────────────────────────────────────────────────────────────────────────────────────
     *   extraneous input 'X' expecting {…}       → parser skips extra token, recovers → WARNING
     *   missing 'X' at 'Y'                       → parser inserts token,   recovers   → WARNING
     *   mismatched input 'X' expecting {…}       → type-ref not in grammar            → WARNING
     *
     * Rule: anything that causes the parser to RECOVER and CONTINUE → WARNING.
     *       Anything that causes the parser to FAIL at a statement  → ERROR.
     */

    // Prefixes / substrings that classify a message as a grammar limitation (→ WARNING).
    private static final List<String> GRAMMAR_LIMITATION_PATTERNS = List.of(
            // ── ALL extraneous-input messages ────────────────────────────────────────
            // Parser skipped an unexpected token and continued — always recoverable.
            "extraneous input ",

            // ── ALL missing-token messages (except at EOF) ───────────────────────────
            // Parser inserted a synthetic token and continued — always recoverable.
            // EOF variant is structural and handled by the ERROR path below.
            "missing '",

            // ── mismatched input: variable-declaration type reference ─────────────────
            // Signature: expects {DEFAULT, NOT, NULL, :=, ;} → parser is in var-decl
            // context and the type name (e.g. INVOICES%ROWTYPE, pkg.MY_TYPE) was not
            // recognised — a grammar limitation, not a bug in the source.
            "expecting {'DEFAULT', 'NOT', 'NULL', ':=', ';'}"

            // NOTE: "no viable alternative at input 'CREATE INDEX'" etc. are intentionally
            // left as ERRORs — the parser cannot recover from them, so they signal that
            // a statement was fully skipped. Use –grammar-skip-ddl mode to suppress them.
    );

    // Substrings that OVERRIDE grammar-limitation classification back to ERROR.
    // Applied after GRAMMAR_LIMITATION_PATTERNS — if any matches, it's an ERROR.
    private static final List<String> ERROR_OVERRIDE_PATTERNS = List.of(
            "'<EOF>'"   // missing 'X' at '<EOF>' or no viable alternative … '<EOF>' → structural
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
