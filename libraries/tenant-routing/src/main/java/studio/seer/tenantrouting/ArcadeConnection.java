package studio.seer.tenantrouting;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over a tenant-scoped ArcadeDB connection.
 * Returned by {@link YggLineageRegistry} and {@link YggSourceArchiveRegistry}.
 * Implementations back this with HTTP calls to the ArcadeDB REST API.
 */
public interface ArcadeConnection {

    /** Execute a SQL query and return a list of result rows. */
    List<Map<String, Object>> sql(String query, Map<String, Object> params);

    /** Execute a Cypher query and return a list of result rows. */
    List<Map<String, Object>> cypher(String query, Map<String, Object> params);

    /** The ArcadeDB database name this connection routes to, e.g. {@code hound_acme_corp}. */
    String databaseName();
}
