package studio.seer.anvil;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.anvil.security.QuerySanitizer;
import studio.seer.anvil.security.QuerySecurityException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class QuerySanitizerTest {

    @Inject
    QuerySanitizer sanitizer;

    // ── Cypher ───────────────────────────────────────────────────────────────

    @Test
    void cypher_selectMatch_passes() {
        assertDoesNotThrow(() -> sanitizer.validate(
                "MATCH (t:DaliTable) RETURN t LIMIT 50", "cypher"));
    }

    @Test
    void cypher_withWhere_passes() {
        assertDoesNotThrow(() -> sanitizer.validate(
                "MATCH (t:DaliTable) WHERE t.qualifiedName CONTAINS 'HR' RETURN t", "cypher"));
    }

    @Test
    void cypher_create_blocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("CREATE (n:Hack {evil:true})", "cypher"));
    }

    @Test
    void cypher_deleteNode_blocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("MATCH (n) DELETE n", "cypher"));
    }

    @Test
    void cypher_mergeBlocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("MERGE (n:Test {id:1})", "cypher"));
    }

    @Test
    void cypher_setBlocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("MATCH (n) SET n.name = 'hacked'", "cypher"));
    }

    @Test
    void cypher_columnNameWithCreate_passes() {
        // "CREATED_AT" should not trigger the CREATE keyword block (word-boundary check)
        assertDoesNotThrow(() -> sanitizer.validate(
                "MATCH (t:DaliTable) WHERE t.created_at IS NOT NULL RETURN t", "cypher"));
    }

    // ── SQL ───────────────────────────────────────────────────────────────────

    @Test
    void sql_selectQuery_passes() {
        assertDoesNotThrow(() -> sanitizer.validate(
                "SELECT qualifiedName FROM DaliTable LIMIT 50", "sql"));
    }

    @Test
    void sql_insertBlocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("INSERT INTO DaliTable VALUES ('x')", "sql"));
    }

    @Test
    void sql_updateBlocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("UPDATE DaliTable SET name = 'bad'", "sql"));
    }

    @Test
    void sql_dropBlocked() {
        assertThrows(QuerySecurityException.class, () ->
                sanitizer.validate("DROP TABLE DaliTable", "sql"));
    }
}
