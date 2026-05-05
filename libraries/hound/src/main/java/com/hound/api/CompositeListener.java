package com.hound.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chains two {@link HoundEventListener}s — fires {@code primary} first, then {@code secondary}.
 *
 * <p>Each listener is invoked in a separate try-catch so that a failure in one
 * does NOT prevent the other from receiving the event.
 *
 * <p>Usage: combine Dali's {@code DaliHoundListener} with {@link HoundHeimdallListener}
 * (or any other listener) without either interfering with the other.
 */
public class CompositeListener implements HoundEventListener {

    private static final Logger log = LoggerFactory.getLogger(CompositeListener.class);

    private final HoundEventListener primary;
    private final HoundEventListener secondary;

    public CompositeListener(HoundEventListener primary, HoundEventListener secondary) {
        this.primary   = primary;
        this.secondary = secondary;
    }

    @Override
    public void onFileParseStarted(String file, String dialect) {
        invoke(() -> primary.onFileParseStarted(file, dialect),   "onFileParseStarted/primary");
        invoke(() -> secondary.onFileParseStarted(file, dialect), "onFileParseStarted/secondary");
    }

    @Override
    public void onAtomExtracted(String file, int atomCount, String atomType) {
        invoke(() -> primary.onAtomExtracted(file, atomCount, atomType),   "onAtomExtracted/primary");
        invoke(() -> secondary.onAtomExtracted(file, atomCount, atomType), "onAtomExtracted/secondary");
    }

    @Override
    public void onRecordRegistered(String file, String varName) {
        invoke(() -> primary.onRecordRegistered(file, varName),   "onRecordRegistered/primary");
        invoke(() -> secondary.onRecordRegistered(file, varName), "onRecordRegistered/secondary");
    }

    @Override
    public void onFileParseCompleted(String file, ParseResult result) {
        invoke(() -> primary.onFileParseCompleted(file, result),   "onFileParseCompleted/primary");
        invoke(() -> secondary.onFileParseCompleted(file, result), "onFileParseCompleted/secondary");
    }

    @Override
    public void onError(String file, Throwable error) {
        invoke(() -> primary.onError(file, error),   "onError/primary");
        invoke(() -> secondary.onError(file, error), "onError/secondary");
    }

    @Override
    public void onParseError(String file, int line, int charPos, String msg) {
        invoke(() -> primary.onParseError(file, line, charPos, msg),   "onParseError/primary");
        invoke(() -> secondary.onParseError(file, line, charPos, msg), "onParseError/secondary");
    }

    @Override
    public void onParseWarning(String file, int line, int charPos, String msg) {
        invoke(() -> primary.onParseWarning(file, line, charPos, msg),   "onParseWarning/primary");
        invoke(() -> secondary.onParseWarning(file, line, charPos, msg), "onParseWarning/secondary");
    }

    @Override
    public void onSemanticWarning(String file, String category, String message) {
        invoke(() -> primary.onSemanticWarning(file, category, message),   "onSemanticWarning/primary");
        invoke(() -> secondary.onSemanticWarning(file, category, message), "onSemanticWarning/secondary");
    }

    @Override
    public void onSemanticError(String file, String category, String message) {
        invoke(() -> primary.onSemanticError(file, category, message),   "onSemanticError/primary");
        invoke(() -> secondary.onSemanticError(file, category, message), "onSemanticError/secondary");
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void invoke(Runnable action, String label) {
        try {
            action.run();
        } catch (Exception e) {
            log.debug("[CompositeListener] {} threw: {}", label, e.getMessage());
        }
    }
}
