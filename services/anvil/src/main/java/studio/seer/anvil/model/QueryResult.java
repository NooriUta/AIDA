package studio.seer.anvil.model;

import java.util.List;
import java.util.Map;

public record QueryResult(
        String                    language,
        List<ImpactNode>          nodes,
        List<ImpactEdge>          edges,
        List<Map<String, Object>> rows,
        int                       totalRows,
        boolean                   hasMore,
        long                      executionMs,
        String                    queryId
) {}
