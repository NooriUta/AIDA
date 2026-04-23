package studio.seer.anvil.model;

import java.util.List;
import java.util.Map;

public record SchemaResponse(
        List<String>        vertexTypes,
        List<String>        edgeTypes,
        String              dbName,
        Map<String, Object> stats
) {}
