package studio.seer.lineage.client.mimir.model;

public record AskInput(String question, String sessionId, String dbName, int maxToolCalls) {}
