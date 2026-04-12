// File: src/main/java/com/hound/storage/WriteHelpers.java
package com.hound.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hound.semantic.model.AtomInfo;
import com.hound.semantic.model.StatementInfo;
import com.hound.semantic.model.Structure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Static utilities shared by EmbeddedWriter and RemoteWriter.
 * No database references — pure computation helpers.
 */
final class WriteHelpers {

    static final int         SNIPPET_MAX   = Integer.MAX_VALUE;
    static final ObjectMapper MAPPER       = new ObjectMapper();

    /** data_source values for DaliTable, DaliColumn, DaliRoutine */
    static final String MASTER       = "master";
    static final String RECONSTRUCTED = "reconstructed";

    private WriteHelpers() {}

    // ── data_source helpers ─────────────────────────────────────────────────

    /**
     * Returns true if this table/view is *defined* in the current session
     * (i.e. targeted by a DDL statement — CREATE TABLE/VIEW/etc.).
     * False = table is only *referenced* (reconstructed from DML/PL-SQL).
     */
    static boolean isMasterTable(String tableGeoid, Structure str) {
        if (str == null || tableGeoid == null) return false;
        return str.getStatements().values().stream()
                .anyMatch(s -> isDdl(s.getType()) && s.getTargetTables().containsKey(tableGeoid));
    }

    // ── JSON / hash / string ────────────────────────────────────────────────

    static String toJson(Object value) {
        if (value == null) return null;
        try { return MAPPER.writeValueAsString(value); }
        catch (JsonProcessingException e) { return null; }
    }

    static String md5(String s) {
        if (s == null) return "";
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    static String formatTime(long ms) {
        if (ms < 1000) return ms + "ms";
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        if (min > 0) return String.format("%dm %02ds", min, sec);
        return String.format("%d.%ds", sec, (ms % 1000) / 100);
    }

    // ── Statement type predicates ───────────────────────────────────────────

    static boolean isDml(String type) {
        return type != null && switch (type) {
            case "INSERT", "UPDATE", "DELETE", "MERGE" -> true;
            default -> false;
        };
    }

    static boolean isDdl(String type) {
        if (type == null) return false;
        return type.startsWith("CREATE") || type.startsWith("ALTER") || type.startsWith("DROP");
    }

    static boolean isCursorStatementType(String type) {
        return type != null && switch (type) {
            case "CURSOR", "ANONYMOUS_BLOCK", "TRIGGER", "FUNCTION", "PROCEDURE" -> true;
            default -> false;
        };
    }

    static String parentType(String parentGeoid, Map<String, StatementInfo> statements) {
        if (parentGeoid == null) return null;
        StatementInfo p = statements.get(parentGeoid);
        return p != null ? p.getType() : null;
    }

    // ── Statement quality / graph metrics ──────────────────────────────────

    static int computeDepth(String parentGeoid, Map<String, StatementInfo> allStatements) {
        int depth = 0;
        String current = parentGeoid;
        while (current != null && depth < 50) {
            StatementInfo parent = allStatements.get(current);
            if (parent == null) break;
            depth++;
            current = parent.getParentStatementGeoid();
        }
        return depth;
    }

    static double computeStatementQuality(StatementInfo s) {
        Map<String, Map<String, Object>> atoms = s.getAtoms();
        if (atoms.isEmpty()) return 0.0;
        int resolved = 0, constants = 0, functions = 0;
        for (Map<String, Object> a : atoms.values()) {
            if (AtomInfo.STATUS_RESOLVED.equals(a.get("status")))           resolved++;
            if (Boolean.TRUE.equals(a.get("is_constant")))      constants++;
            if (Boolean.TRUE.equals(a.get("is_function_call"))) functions++;
        }
        return (resolved + constants + functions) / (double) atoms.size();
    }

    static boolean hasCte(StatementInfo s, Map<String, StatementInfo> allStatements) {
        for (String childGeoid : s.getChildStatements()) {
            StatementInfo child = allStatements.get(childGeoid);
            if (child != null && "CTE".equals(child.getType())) return true;
        }
        return false;
    }

    static int countInputColumns(StatementInfo s) {
        return (int) s.getAtoms().values().stream()
                .filter(a -> Boolean.TRUE.equals(a.get("is_column_reference")))
                .count();
    }

    // ── Flow type resolution ────────────────────────────────────────────────

    static String resolveFlowType(Map<String, Object> atom, StatementInfo stmt) {
        if (stmt != null && stmt.isHasAggregation()
                && "GROUP BY".equals(atom.get("parent_context")))
            return "AGGREGATE";
        if (Boolean.TRUE.equals(atom.get("is_function_call")))
            return "TRANSFORM";
        return "DIRECT";
    }

    static String resolveDmlFlowType(StatementInfo stmt) {
        if (stmt == null) return "DIRECT";
        return switch (stmt.getType()) {
            case "UPDATE" -> "UPDATE";
            case "MERGE"  -> "MERGE";
            case "INSERT" -> "INSERT";
            default       -> "DIRECT";
        };
    }
}
