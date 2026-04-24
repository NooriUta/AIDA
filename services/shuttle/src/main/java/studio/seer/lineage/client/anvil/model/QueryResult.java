package studio.seer.lineage.client.anvil.model;

public record QueryResult(
        String language,
        String rowsJson,
        int totalRows,
        boolean hasMore,
        long executionMs,
        String queryId
) {}
