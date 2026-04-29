package studio.seer.analytics.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * ArcadeDB REST command body.
 *
 * <pre>
 * POST /api/v1/command/{db}
 * { "language": "cypher", "command": "CALL algo.pagerank() YIELD ..." }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArcadeCommand(String language, String command, Map<String, Object> params) {

    /** Convenience constructor — no params. */
    public ArcadeCommand(String language, String command) {
        this(language, command, null);
    }
}
