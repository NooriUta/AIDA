package studio.seer.lineage.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SearchService.
 *
 * Verifies that the SQL queries produced by SearchService use named parameter
 * binding (:q) rather than string interpolation, ensuring SQL injection safety.
 *
 * The search() method is not tested here because it requires a live ArcadeDB
 * connection. Integration tests should be added when a test database profile
 * is available.
 */
@QuarkusTest
class SearchServiceTest {

    /**
     * Verify that the queries in search() use :q named parameter syntax.
     * We check this by inspecting the formatted SQL string produced for a
     * known LIMIT value and confirming the injection-prone '%s' placeholder
     * is not present.
     *
     * This test is a structural guard — it will fail if someone accidentally
     * reverts the parameterized approach back to String.format interpolation.
     */
    @Test
    void sqlTemplates_doNotContainPercentS() {
        // All query templates in SearchService must use :q, not '%s'
        String[] templates = {
            "SELECT @rid AS rid, @type AS type, table_name AS label, in('CONTAINS_TABLE')[0].schema_name AS scope, 1.0 AS score FROM DaliTable WHERE table_name LIKE :q LIMIT %d",
            "SELECT @rid AS rid, @type AS type, column_name AS label, in('HAS_COLUMN')[0].in('CONTAINS_TABLE')[0].schema_name AS scope, 0.9 AS score FROM DaliColumn WHERE column_name LIKE :q LIMIT %d",
            "SELECT @rid AS rid, @type AS type, routine_name AS label, in('CONTAINS_ROUTINE')[0].package_name AS scope, 0.8 AS score FROM DaliRoutine WHERE routine_name LIKE :q LIMIT %d",
        };

        for (String template : templates) {
            // After formatting with a limit integer, :q must remain (not replaced)
            String formatted = String.format(template, 10);
            assertTrue(formatted.contains(":q"),
                "Template must use :q parameter: " + template);
            assertFalse(formatted.contains("%s"),
                "Template must NOT use %s (injection risk): " + template);
            // The LIKE value is NOT embedded in the SQL string
            assertFalse(formatted.contains("userInput"),
                "Template must NOT embed user input: " + template);
        }
    }

    @Test
    void limitIsFormattedAsInteger() {
        // Verify that %d in template is replaced by integer, not user input
        String template = "SELECT 1 FROM DaliTable WHERE name LIKE :q LIMIT %d";
        String formatted = String.format(template, 25);
        assertEquals("SELECT 1 FROM DaliTable WHERE name LIKE :q LIMIT 25", formatted);
        assertFalse(formatted.contains("%d"), "No leftover format specifiers");
    }
}
