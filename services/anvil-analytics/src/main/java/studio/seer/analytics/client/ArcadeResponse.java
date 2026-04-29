package studio.seer.analytics.client;

import java.util.List;
import java.util.Map;

/**
 * ArcadeDB REST response wrapper.
 *
 * {@code result} is a list of row-maps; for CALL algo.* each row has
 * the algorithm-defined YIELD columns.
 */
public record ArcadeResponse(List<Map<String, Object>> result) {

    /** Returns true if result is non-null and non-empty. */
    public boolean hasResults() {
        return result != null && !result.isEmpty();
    }
}
