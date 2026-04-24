package studio.seer.lineage.client.anvil.model;

import java.util.List;
import java.util.Map;

public record AnvilQueryResponse(
        String language,
        List<Map<String, Object>> rows,
        int totalRows,
        boolean hasMore,
        long executionMs,
        String queryId
) {}
