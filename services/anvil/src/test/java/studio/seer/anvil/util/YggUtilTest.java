package studio.seer.anvil.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3-A: YggUtil coverage — basicAuth() and escape().
 * Pure utility, no Quarkus context needed → plain JUnit 5.
 */
class YggUtilTest {

    // ── basicAuth ─────────────────────────────────────────────────────────────

    @Test
    void basicAuth_encodesUserColonPass() {
        // "user:pass" → Base64 → "dXNlcjpwYXNz"
        String header = YggUtil.basicAuth("user", "pass");
        assertEquals("Basic dXNlcjpwYXNz", header);
    }

    @Test
    void basicAuth_emptyCredentials() {
        // ":" → Base64 → "Og=="
        String header = YggUtil.basicAuth("", "");
        assertEquals("Basic Og==", header);
    }

    @Test
    void basicAuth_specialCharacters() {
        // Credentials with colon in password — first colon is separator
        String header = YggUtil.basicAuth("admin", "p@ss:w0rd");
        // "admin:p@ss:w0rd" → Base64
        String expected = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("admin:p@ss:w0rd".getBytes());
        assertEquals(expected, header);
    }

    // ── escape ────────────────────────────────────────────────────────────────

    @Test
    void escape_replacesApostrophe() {
        assertEquals("it\\'s", YggUtil.escape("it's"));
    }

    @Test
    void escape_noApostrophe_unchanged() {
        assertEquals("hello world", YggUtil.escape("hello world"));
    }

    @Test
    void escape_multipleApostrophes() {
        assertEquals("a\\'b\\'c", YggUtil.escape("a'b'c"));
    }

    @Test
    void escape_nullInput_returnsEmpty() {
        assertEquals("", YggUtil.escape(null));
    }

    @Test
    void escape_emptyString_returnsEmpty() {
        assertEquals("", YggUtil.escape(""));
    }
}
