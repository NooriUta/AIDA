// File: src/main/java/com/hound/storage/WriteStats.java
package com.hound.storage;

import java.util.*;

/**
 * Accumulates actual per-type write statistics during a REMOTE_BATCH operation.
 *
 * <p>Populated by {@link JsonlBatchBuilder} (in-batch vertices / edges) and by
 * {@link RemoteWriter} (canonical types inserted or skipped via rcmd before the batch).
 *
 * <p>The distinction:
 * <ul>
 *   <li><b>inserted</b> — vertex was actually written to YGG (new rcmd INSERT or sent in batch)</li>
 *   <li><b>duplicate</b> — vertex was skipped because it already exists in DB
 *       (canonical pool hit, pre-existing schema, or in-batch dedup guard)</li>
 * </ul>
 */
public class WriteStats {

    /** type → [inserted, duplicate] */
    private final Map<String, int[]> vtx = new LinkedHashMap<>();
    private int edges;
    private int droppedEdges;

    // ── Mutation API (called by JsonlBatchBuilder and RemoteWriter) ────────────

    public void insert(String type)    { vtx.computeIfAbsent(type, k -> new int[2])[0]++; }
    public void markDuplicate(String type) { vtx.computeIfAbsent(type, k -> new int[2])[1]++; }
    public void addEdge()              { edges++; }
    public void dropEdge()             { droppedEdges++; }

    // ── Query API ──────────────────────────────────────────────────────────────

    public int inserted(String type)   { int[] a = vtx.get(type); return a == null ? 0 : a[0]; }
    public int duplicate(String type)  { int[] a = vtx.get(type); return a == null ? 0 : a[1]; }
    public int total(String type)      { int[] a = vtx.get(type); return a == null ? 0 : a[0] + a[1]; }

    public int totalInserted()         { return vtx.values().stream().mapToInt(a -> a[0]).sum(); }
    public int totalDuplicate()        { return vtx.values().stream().mapToInt(a -> a[1]).sum(); }
    public int edgeCount()             { return edges; }
    public int droppedEdgeCount()      { return droppedEdges; }

    /** Shortcut: DaliAtom vertices actually written. */
    public int atomsInserted()         { return inserted("DaliAtom"); }

    /** Ordered type names (insertion order). */
    public Set<String> types()         { return Collections.unmodifiableSet(vtx.keySet()); }

    /**
     * Returns a defensive snapshot: type → [inserted, duplicate].
     * Callers should treat the arrays as read-only.
     */
    public Map<String, int[]> snapshot() {
        Map<String, int[]> copy = new LinkedHashMap<>();
        vtx.forEach((k, v) -> copy.put(k, new int[]{v[0], v[1]}));
        return Collections.unmodifiableMap(copy);
    }
}
