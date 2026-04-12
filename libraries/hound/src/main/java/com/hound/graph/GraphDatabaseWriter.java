package com.hound.graph;

import java.util.List;

/**
 * Интерфейс для записи в графовую БД.
 * Добавлены batch-методы для эффективной записи (особенно важно для FalkorDB).
 */
public interface GraphDatabaseWriter extends AutoCloseable {

    void connect(String host, int port, String database, String username, String password);

    /**
     * Переключает текущий граф (для каждого файла — свой граф)
     */
    void switchGraph(String graphName);

    // Одиночные методы (для совместимости)
    void writeNode(GraphNode node);
    void writeRelationship(GraphRelationship relationship);

    // Batch-методы — рекомендуемый способ записи
    void writeNodes(List<GraphNode> nodes);
    void writeRelationships(List<GraphRelationship> relationships);

    void executeQuery(String cypher);

    void beginTransaction();
    void commitTransaction();
    void rollbackTransaction();

    boolean isConnected();
    void close();
}