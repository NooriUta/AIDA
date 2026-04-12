package com.hound.api;

/**
 * Listener for Hound parse events.
 *
 * <p>Implement this interface to receive real-time progress from {@link HoundParser}.
 * All methods have default no-op implementations — override only what you need.
 *
 * <p>Wire-up inside Hound:
 * <ul>
 *   <li>{@link #onFileParseStarted} / {@link #onFileParseCompleted} / {@link #onError}
 *       — fired from {@code HoundParserImpl}
 *   <li>{@link #onAtomExtracted} — fired from {@code AtomProcessor.registerAtom()} (C.1.3)
 *   <li>{@link #onRecordRegistered} — fired from {@code StructureAndLineageBuilder.ensureRecord()} (C.1.3)
 * </ul>
 *
 * @see NoOpHoundEventListener
 */
public interface HoundEventListener {

    /** Called once before a file's parse phase begins. */
    default void onFileParseStarted(String file, String dialect) {}

    /**
     * Called each time an atom is extracted from the AST.
     * May fire many times per file (one per atom).
     *
     * @param file      file currently being parsed
     * @param atomCount running total of atoms extracted so far in this file
     * @param atomType  type label, e.g. {@code "COLUMN_REF"}, {@code "FUNCTION_CALL"}
     */
    default void onAtomExtracted(String file, int atomCount, String atomType) {}

    /**
     * Called when a DaliRecord (package/type) is registered.
     * Useful for Dali to track DaliRecord events without parsing SemanticResult.
     */
    default void onRecordRegistered(String file, String varName) {}

    /** Called once after a file is fully parsed and written to YGG. */
    default void onFileParseCompleted(String file, ParseResult result) {}

    /** Called if an unrecoverable error occurs while processing {@code file}. */
    default void onError(String file, Throwable error) {}
}
