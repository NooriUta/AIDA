// src/main/java/com/hound/graph/GraphWriter.java
package com.hound.graph;

import com.hound.parser.core.AstCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Записывает AST-дерево в графовую базу.
 * Используется для сохранения структуры AST в ArcadeDB.
 */
public class GraphWriter implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(GraphWriter.class);

    private final GraphDatabaseWriter dbWriter;
    private final String graphName;

    public GraphWriter(GraphDatabaseWriter dbWriter, String graphName) {
        this.dbWriter = dbWriter;
        this.graphName = graphName;
        this.dbWriter.switchGraph(graphName);
    }

    /**
     * Записывает результат семантического анализа в граф
     */
    public void writeResult(AstCollector.CollectionResult result) {
        if (result == null || result.nodes().isEmpty()) {
            logger.warn("Empty result for graph '{}'", graphName);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            // Фаза 1: записываем все узлы
            for (UniversalAstNode node : result.nodes()) {
                dbWriter.writeNode(node.toGraphNode());
            }

            // Фаза 2: записываем связи
            for (GraphRelationship rel : result.relationships()) {
                dbWriter.writeRelationship(rel);
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Written to graph '{}': {} nodes, {} rels [{}]",
                    graphName, result.nodes().size(), result.relationships().size(), formatTime(duration));

        } catch (Exception e) {
            logger.error("Error writing to graph '{}'", graphName, e);
            throw new RuntimeException("Failed to write AST to graph " + graphName, e);
        }
    }

    /**
     * Для обратной совместимости
     */
    public void writeTree(UniversalAstNode root) {
        if (root == null) return;
        AstCollector.CollectionResult result = AstCollector.collect(root);
        writeResult(result);
    }

    private static String formatTime(long ms) {
        if (ms < 1000) return ms + "ms";
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        if (min > 0) return String.format("%dm %02ds", min, sec);
        return String.format("%d.%ds", sec, (ms % 1000) / 100);
    }

    @Override
    public void close() {
        // Управление соединением осуществляется через dbWriter
    }
}