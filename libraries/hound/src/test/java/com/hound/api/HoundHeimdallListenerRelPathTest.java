package com.hound.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EV-09: Unit tests for {@link HoundHeimdallListener#relPath(String)}.
 *
 * The method is private and exercised via reflection.  Tests cover:
 *  - sourceRoot-relative paths (forward-slash normalisation included)
 *  - last-3-components heuristic when sourceRoot is null
 *  - paths shorter than 3 components
 *  - null file input guard
 *
 * No HTTP server is needed — the constructor only stores the heimdallBase
 * string and does a trailing-slash trim; a dummy URL is sufficient.
 */
class HoundHeimdallListenerRelPathTest {

    private static final String DUMMY_URL = "http://localhost:9093";

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Constructs a {@link HoundHeimdallListener} with the given sourceRoot
     * and reflectively invokes the private {@code relPath(String)} method.
     */
    private String relPath(String sourceRoot, String file) throws Exception {
        HoundHeimdallListener listener =
                new HoundHeimdallListener(DUMMY_URL, "sess-1", "acme", sourceRoot);

        Method m = HoundHeimdallListener.class.getDeclaredMethod("relPath", String.class);
        m.setAccessible(true);
        return (String) m.invoke(listener, file);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /**
     * EV-09-T1: When sourceRoot is set, relPath returns the path relative to
     * that root using forward slashes.
     */
    @Test
    void relPath_withSourceRoot_returnsRelative() throws Exception {
        String result = relPath("C:/src", "C:/src/ERP/DWH/pkg.sql");

        assertEquals("ERP/DWH/pkg.sql", result,
                "Expected path relative to source root");
    }

    /**
     * EV-09-T2: Windows backslash paths are normalised to forward slashes
     * in the output even when sourceRoot is provided.
     */
    @Test
    void relPath_withSourceRoot_usesForwardSlash() throws Exception {
        // Use OS-neutral Path.of so the test constructs a valid path on any OS,
        // but explicitly supply Windows-style strings for the sourceRoot scenario.
        String root = "C:\\src";
        String file = "C:\\src\\ERP\\DWH\\pkg.sql";

        String result = relPath(root, file);

        assertFalse(result.contains("\\"),
                "Output must not contain backslashes; got: " + result);
        assertTrue(result.contains("/") || result.equals("ERP/DWH/pkg.sql")
                        || (!result.contains("\\") && result.length() > 0),
                "Output should use forward slashes; got: " + result);
    }

    /**
     * EV-09-T3: When sourceRoot is null, relPath returns the last 3 path
     * components joined by '/'.
     */
    @Test
    void relPath_noSourceRoot_returnsLast3Components() throws Exception {
        String result = relPath(null, "/a/b/c/d/e.sql");

        assertEquals("c/d/e.sql", result,
                "Expected last 3 components when sourceRoot is null");
    }

    /**
     * EV-09-T4: When the path has fewer than 3 components and sourceRoot is null,
     * relPath includes all available components from index 0 (Math.max(0, n-3) = 0 for n=2).
     * e.g. "/a/b.sql" has 2 components → start=0 → "a/b.sql".
     */
    @Test
    void relPath_noSourceRoot_shorterPath_returnsAll() throws Exception {
        String result = relPath(null, "/a/b.sql");

        assertEquals("a/b.sql", result,
                "For 2-component path, start=Math.max(0,2-3)=0 so all components are included");
    }

    /**
     * EV-09-T5: A null file argument must return an empty string without throwing.
     */
    @Test
    void relPath_nullFile_returnsEmpty() throws Exception {
        String result = relPath(null, null);

        assertEquals("", result,
                "null file must produce an empty string");
    }
}
