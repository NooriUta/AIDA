package com.hound.graph;

import java.util.*;

/**
 * Универсальный узел AST.
 *
 * FIX BUG-1: toGraphNode() передаёт this.id в GraphNode(id, label),
 * чтобы id в БД совпадал с id в GraphRelationship.
 *
 * FIX DEF-3: Все методы реализованы (addChild, getChildren, getId и т.д.)
 */
public class UniversalAstNode {
    private final String id;
    private final String type;
    private final String nodeType;
    private final Map<String, Object> properties;
    private final List<UniversalAstNode> children;
    private UniversalAstNode parent;
    private Position position;

    public UniversalAstNode(String type, String nodeType) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.nodeType = nodeType;
        this.properties = new LinkedHashMap<>();
        this.children = new ArrayList<>();
    }

    // ========================= Properties =========================

    public void addProperty(String key, Object value) {
        if (key != null && value != null) {
            properties.put(key, value);
        }
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    // ========================= Children =========================

    public void addChild(UniversalAstNode child) {
        if (child != null) {
            child.parent = this;
            children.add(child);
        }
    }

    public List<UniversalAstNode> getChildren() {
        return children;
    }

    // ========================= Position =========================

    public void setPosition(int line, int column, int startIndex, int stopIndex) {
        this.position = new Position(line, column, startIndex, stopIndex);
    }

    public Position getPosition() {
        return position;
    }

    // ========================= Getters =========================

    public String getId() { return id; }
    public String getType() { return type; }
    public String getNodeType() { return nodeType; }
    public UniversalAstNode getParent() { return parent; }

    // ========================= Graph conversion =========================

    /**
     * Конвертирует в GraphNode для записи в БД.
     *
     * КРИТИЧНО: передаём this.id в GraphNode, чтобы id совпадал
     * с fromNodeId/toNodeId в GraphRelationship. Без этого
     * MATCH (a {id: ...}) не найдёт узел и связи не создадутся.
     */
    public GraphNode toGraphNode() {
        // FIX BUG-1: передаём this.id, а не позволяем GraphNode генерировать новый
        GraphNode node = new GraphNode(this.id, type);

        node.addProperty("nodeType", nodeType);

        // Копируем все properties AST-узла
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = entry.getValue();
            // Пропускаем вложенные объекты — в графовую БД кладём только примитивы и строки
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                node.addProperty(entry.getKey(), value);
            }
        }

        // Для корневого узла — информация о файле
        if ("Program".equals(type) || "ROOT".equals(nodeType)) {
            Object fileName = properties.get("fileName");
            if (fileName != null) {
                node.addProperty("fileName", fileName);
                node.addProperty("filePath", fileName);
            }
        }

        // Позиция в исходном коде
        if (position != null) {
            node.addProperty("line", position.line());
            node.addProperty("column", position.column());
            node.addProperty("startIndex", position.startIndex());
            node.addProperty("stopIndex", position.stopIndex());
        }

        return node;
    }

    // ========================= Tree traversal =========================

    /**
     * Собирает все узлы поддерева (включая текущий).
     */
    public List<UniversalAstNode> getAllNodes() {
        List<UniversalAstNode> result = new ArrayList<>();
        collectAllNodes(result);
        return result;
    }

    private void collectAllNodes(List<UniversalAstNode> result) {
        result.add(this);
        for (UniversalAstNode child : children) {
            child.collectAllNodes(result);
        }
    }

    // ========================= Position record =========================

    /**
     * Позиция узла в исходном коде.
     */
    public record Position(int line, int column, int startIndex, int stopIndex) {}

    @Override
    public String toString() {
        return String.format("AstNode{type='%s', nodeType='%s', children=%d}",
                type, nodeType, children.size());
    }
}