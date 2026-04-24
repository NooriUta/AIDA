package studio.seer.anvil.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

/**
 * AV-06 — Blocks write operations in user-submitted Cypher/SQL queries.
 * Decision #36/#40/#41: ANVIL IDE must not allow mutations to the graph.
 */
@ApplicationScoped
public class QuerySanitizer {

    private static final Set<String> BLOCKED_CYPHER =
            Set.of("CREATE", "DELETE", "REMOVE", "SET", "MERGE", "DROP");

    private static final Set<String> BLOCKED_SQL =
            Set.of("INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "TRUNCATE", "ALTER");

    public void validate(String query, String language) {
        String upper = query.toUpperCase();
        Set<String> blocked = "sql".equalsIgnoreCase(language) ? BLOCKED_SQL : BLOCKED_CYPHER;
        for (String kw : blocked) {
            // Match as whole word to avoid false positives (e.g. "CREATED_AT" should pass)
            if (upper.matches("(?s).*\\b" + kw + "\\b.*")) {
                throw new QuerySecurityException("Write operations not allowed: " + kw);
            }
        }
    }
}
