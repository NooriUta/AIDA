package com.hound.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Узел графа для записи в БД.
 *
 * FIX BUG-1: Конструктор принимает внешний id.
 *
 * Проблема: GraphNode генерировал собственный UUID, а GraphRelationship
 * ссылался на id из UniversalAstNode. В результате:
 *   - flushNodes() записывала узел с id = GraphNode.UUID
 *   - flushRelationships() искала MATCH (a {id: UniversalAstNode.UUID})
 *   - MATCH ничего не находил → связи не создавались
 *
 * Решение: toGraphNode() передаёт this.id из UniversalAstNode →
 * в БД у узла тот же id, что и в GraphRelationship.
 */
public class GraphNode {
    private final String id;
    private final String label;
    private final Map<String, Object> properties;

    /**
     * Основной конструктор — принимает id извне.
     * Используется из UniversalAstNode.toGraphNode() для сохранения единого id
     * между узлом и связями (GraphRelationship).
     */
    public GraphNode(String id, String label) {
        this.id = id;
        this.label = label;
        this.properties = new HashMap<>();
        // id сразу в properties — чтобы при SET n = props он попал в БД
        this.properties.put("id", id);
    }

    /**
     * Конструктор с авто-генерацией id.
     * Только для standalone-узлов, не связанных через GraphRelationship.
     */
    public GraphNode(String label) {
        this(UUID.randomUUID().toString(), label);
    }

    public GraphNode(String id, String label, Map<String, Object> properties) {
        this(id, label);
        this.properties.putAll(properties);
    }

    public GraphNode addProperty(String key, Object value) {
        if (value != null) {
            properties.put(key, escapeValue(value));
        }
        return this;
    }

    private Object escapeValue(Object value) {
        if (value instanceof String str) {
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("'", "\\'");
        }
        return value;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public Map<String, Object> getProperties() { return properties; }
}