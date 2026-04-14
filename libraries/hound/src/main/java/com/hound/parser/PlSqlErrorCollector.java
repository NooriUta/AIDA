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
 * <p>Collects every syntax error reported by the lexer and parser into an in-memory list
 * that callers can inspect after parsing. Each error is also:
 * <ul>
 *   <li>Logged at WARN level (file + line:col + message).
 *   <li>Forwarded to the supplied {@link HoundEventListener#onParseError} so that
 *       Dali/HEIMDALL can display it in the event stream and in {@code FileResult.errors}.
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 *   PlSqlErrorCollector errors = new PlSqlErrorCollector(filePath, listener);
 *   lexer.removeErrorListeners();
 *   lexer.addErrorListener(errors);
 *   parser.removeErrorListeners();
 *   parser.addErrorListener(errors);
 *   // ... parse ...
 *   List<String> parseErrors = errors.getErrors();
 * }</pre>
 */
public class PlSqlErrorCollector extends BaseErrorListener {

    private static final Logger log = LoggerFactory.getLogger(PlSqlErrorCollector.class);

    /** Maximum number of syntax errors collected per file (to avoid log spam). */
    private static final int MAX_ERRORS = 50;

    private final String                file;
    private final HoundEventListener    listener;
    private final List<String>          errors = new ArrayList<>();

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
        if (errors.size() >= MAX_ERRORS) return;

        String entry = "line " + line + ":" + charPositionInLine + " — " + msg;
        errors.add(entry);

        log.warn("[ANTLR4] {} | {}", basename(file), entry);

        try {
            listener.onParseError(file, line, charPositionInLine, msg);
        } catch (Exception ex) {
            log.debug("[PlSqlErrorCollector] onParseError callback failed: {}", ex.getMessage());
        }
    }

    /** Returns an unmodifiable snapshot of collected syntax errors. */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /** True if any syntax errors were collected. */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private static String basename(String path) {
        if (path == null) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
