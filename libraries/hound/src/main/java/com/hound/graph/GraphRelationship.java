package com.hound.graph;

import java.util.HashMap;
import java.util.Map;

public class GraphRelationship {
    private final String fromNodeId;
    private final String toNodeId;
    private final String type;
    private final Map<String, Object> properties;

    public GraphRelationship(String fromNodeId, String toNodeId, String type) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.type = type;
        this.properties = new HashMap<>();
    }

    public GraphRelationship addProperty(String key, Object value) {
        if (value != null) {
            properties.put(key, value);
        }
        return this;
    }

    public String getFromNodeId() { return fromNodeId; }
    public String getToNodeId() { return toNodeId; }
    public String getType() { return type; }
    public Map<String, Object> getProperties() { return properties; }
}