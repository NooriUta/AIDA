package studio.seer.anvil.model;

public record LineageRequest(
        String nodeId,
        String direction,
        String dbName,
        int    maxHops
) {}
