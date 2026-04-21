package studio.seer.anvil.model;

import java.util.Map;

public record QueryRequest(
        String              language,
        String              query,
        String              dbName,
        Map<String, Object> params
) {}
