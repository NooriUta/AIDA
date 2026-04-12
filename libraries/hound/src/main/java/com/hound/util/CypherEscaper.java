package com.hound.graph.util;

/**
 * Централизованное экранирование значений для Cypher-запросов
 */
public final class CypherEscaper {

    private CypherEscaper() {}

    public static Object escape(Object value) {
        if (value instanceof String str) {
            return str
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\0", "");
        }
        return value;
    }

    public static String escapeString(String str) {
        if (str == null) return null;
        return (String) escape(str);
    }
}