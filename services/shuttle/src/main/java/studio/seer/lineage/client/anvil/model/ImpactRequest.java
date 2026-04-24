package studio.seer.lineage.client.anvil.model;

import java.util.List;

public record ImpactRequest(
        String nodeId,
        String direction,
        String dbName,
        int maxHops,
        List<String> includeTypes
) {}
