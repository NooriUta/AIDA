package studio.seer.anvil.model;

import java.util.List;

public record ImpactRequest(
        String       nodeId,
        String       direction,
        int          maxHops,
        String       dbName,
        List<String> includeTypes
) {}
