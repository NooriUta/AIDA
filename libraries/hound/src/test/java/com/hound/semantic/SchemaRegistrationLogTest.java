package com.hound.semantic;

import com.hound.semantic.engine.StructureAndLineageBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S1.SCH functional tests — suspicious schema name detection and logging.
 *
 * Verifies that StructureAndLineageBuilder.ensureSchema() correctly identifies
 * suspicious schema names (quotes, parens, dots, $, :, spaces) and logs them
 * with the right reason and a non-empty com.hound backtrace.
 */
class SchemaRegistrationLogTest {

    private StructureAndLineageBuilder builder() {
        return new StructureAndLineageBuilder();
    }

    // ═══════════════════════════════════════════════════════
    // Clean schema names — must NOT be logged
    // ═══════════════════════════════════════════════════════

    @Test
    void cleanSchemaName_notLogged() {
        var b = builder();
        b.ensureSchema("BUDM_RMS", null);
        b.ensureSchema("HR", null);
        b.ensureSchema("MY_SCHEMA_123", null);

        assertTrue(b.getSchemaRegistrationLog().isEmpty(),
                "Clean schema names must not appear in schema registration log");
    }

    // ═══════════════════════════════════════════════════════
    // Suspicious reasons — each variant detected correctly
    // ═══════════════════════════════════════════════════════

    @Test
    void quotedSchema_reason_quotedIdentifierNotStripped() {
        var b = builder();
        b.ensureSchema("\"MY_SCHEMA\"", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Quoted schema name must be logged");
        assertEquals("quoted_identifier_not_stripped", log.get(0).get("reason"));
    }

    @Test
    void singleQuotedSchema_reason_quotedIdentifierNotStripped() {
        var b = builder();
        b.ensureSchema("'MY_SCHEMA'", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Single-quoted schema name must be logged");
        assertEquals("quoted_identifier_not_stripped", log.get(0).get("reason"));
    }

    @Test
    void parenSchema_reason_parenthesisInSchemaName() {
        var b = builder();
        b.ensureSchema("TABLE(vtSourceData)", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Schema name with parens must be logged");
        assertEquals("parenthesis_in_schema_name", log.get(0).get("reason"));
    }

    @Test
    void dotSchema_reason_dotInSchemaName() {
        var b = builder();
        b.ensureSchema("MY.SCHEMA", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Schema name with dot must be logged");
        assertEquals("dot_in_schema_name", log.get(0).get("reason"));
    }

    @Test
    void dollarSchema_reason_dollarSign() {
        var b = builder();
        b.ensureSchema("SYS$SCHEMA", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Schema name with $ must be logged");
        assertEquals("dollar_sign_in_schema_name", log.get(0).get("reason"));
    }

    @Test
    void colonSchema_reason_colonInSchemaName() {
        var b = builder();
        b.ensureSchema(":schema", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Schema name with colon must be logged");
        assertEquals("colon_in_schema_name", log.get(0).get("reason"));
    }

    @Test
    void spaceSchema_reason_spaceInSchemaName() {
        var b = builder();
        b.ensureSchema("MY SCHEMA", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertFalse(log.isEmpty(), "Schema name with space must be logged");
        assertEquals("space_in_schema_name", log.get(0).get("reason"));
    }

    // ═══════════════════════════════════════════════════════
    // Log entry structure
    // ═══════════════════════════════════════════════════════

    @Test
    void logEntry_hasRequiredFields() {
        var b = builder();
        b.ensureSchema("\"BAD_SCHEMA\"", null);

        Map<String, Object> entry = b.getSchemaRegistrationLog().get(0);
        assertTrue(entry.containsKey("schema_name"), "Log entry must have schema_name");
        assertTrue(entry.containsKey("reason"),      "Log entry must have reason");
        assertTrue(entry.containsKey("backtrace"),   "Log entry must have backtrace");
    }

    @Test
    void logEntry_schemaNamePreservedAsIs() {
        var b = builder();
        b.ensureSchema("\"MY_SCHEMA\"", null);

        Map<String, Object> entry = b.getSchemaRegistrationLog().get(0);
        assertEquals("\"MY_SCHEMA\"", entry.get("schema_name"),
                "schema_name in log must match the raw input (not uppercased or stripped)");
    }

    @Test
    void logEntry_backtraceContainsHoundClass() {
        var b = builder();
        b.ensureSchema("TABLE(x)", null);

        Map<String, Object> entry = b.getSchemaRegistrationLog().get(0);
        String backtrace = (String) entry.get("backtrace");
        assertNotNull(backtrace, "backtrace must not be null");
        assertFalse(backtrace.isBlank(), "backtrace must not be blank");
        assertTrue(backtrace.contains("StructureAndLineageBuilder"),
                "backtrace must contain calling com.hound class name");
    }

    // ═══════════════════════════════════════════════════════
    // Deduplication — same name logged only once
    // ═══════════════════════════════════════════════════════

    @Test
    void suspiciousSchema_loggedOnlyOnFirstRegistration() {
        var b = builder();
        b.ensureSchema("\"DUP_SCHEMA\"", null);
        b.ensureSchema("\"DUP_SCHEMA\"", null);  // second call — already in schemas map
        b.ensureSchema("\"DUP_SCHEMA\"", null);  // third call

        assertEquals(1, b.getSchemaRegistrationLog().size(),
                "Suspicious schema must be logged only once (on first registration)");
    }

    // ═══════════════════════════════════════════════════════
    // Log is unmodifiable from outside
    // ═══════════════════════════════════════════════════════

    @Test
    void schemaRegistrationLog_isUnmodifiable() {
        var b = builder();
        b.ensureSchema("\"SCHEMA\"", null);

        List<Map<String, Object>> log = b.getSchemaRegistrationLog();
        assertThrows(UnsupportedOperationException.class, () -> log.add(Map.of()),
                "getSchemaRegistrationLog() must return an unmodifiable list");
    }
}
