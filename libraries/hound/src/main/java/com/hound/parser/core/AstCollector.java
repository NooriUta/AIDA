package com.hound.parser.core;

import com.hound.graph.GraphRelationship;
import com.hound.graph.UniversalAstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Собирает узлы и связи без дублирования (Вариант A).
 */
public final class AstCollector {

    private AstCollector() {}

    public static CollectionResult collect(UniversalAstNode root) {
        List<UniversalAstNode> nodes = new ArrayList<>();
        List<GraphRelationship> relationships = new ArrayList<>();

        collectRecursive(root, nodes, relationships);

        return new CollectionResult(nodes, relationships);
    }

    private static void collectRecursive(UniversalAstNode node,
                                         List<UniversalAstNode> nodes,
                                         List<GraphRelationship> relationships) {
        if (node == null) return;

        nodes.add(node);

        // Связь только от родителя к прямому ребёнку
        for (UniversalAstNode child : node.getChildren()) {
            relationships.add(new GraphRelationship(node.getId(), child.getId(), "CONTAINS"));
        }

        for (UniversalAstNode child : node.getChildren()) {
            collectRecursive(child, nodes, relationships);
        }
    }

    public record CollectionResult(
            List<UniversalAstNode> nodes,
            List<GraphRelationship> relationships
    ) {}
}