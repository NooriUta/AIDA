// src/main/java/com/hound/metrics/PipelineTimer.java
package com.hound.metrics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight accumulating phase timer for pipeline profiling.
 *
 * Usage:
 *   timer.start("parse");
 *   ... do work ...
 *   timer.stop("parse");          // accumulates — safe to start/stop multiple times
 *   long ms = timer.ms("parse");  // milliseconds
 *
 * Not thread-safe — one instance per file, single-threaded processing.
 */
public class PipelineTimer {

    private final Map<String, Long>    starts    = new LinkedHashMap<>();
    private final Map<String, Long>    durations = new LinkedHashMap<>();
    private final Map<String, Integer> counts    = new LinkedHashMap<>();

    /** Record start of a phase (nanosecond precision). */
    public void start(String phase) {
        starts.put(phase, System.nanoTime());
    }

    /** Stop a phase and accumulate its duration. Safe to call without a prior start. */
    public void stop(String phase) {
        Long s = starts.get(phase);
        if (s == null) return;
        durations.merge(phase, System.nanoTime() - s, Long::sum);
    }

    /** Add delta to an integer counter (e.g. token count, line count). */
    public void count(String key, int delta) {
        counts.merge(key, delta, Integer::sum);
    }

    /** Duration of a named phase in milliseconds (0 if never started). */
    public long ms(String phase) {
        Long ns = durations.get(phase);
        return ns == null ? 0L : ns / 1_000_000L;
    }

    /**
     * Sum of all pipeline phases: parse + walk + resolve + write.
     * Write phase is write.vtx+write.edge (EMBEDDED/REMOTE) or write.batch (REMOTE_BATCH).
     */
    public long totalMs() {
        return ms("parse") + ms("walk") + ms("resolve") + writeMs();
    }

    /** DB write time regardless of mode (write.batch takes priority over write.vtx+write.edge). */
    public long writeMs() {
        long batch = ms("write.batch");
        return batch > 0 ? batch : ms("write.vtx") + ms("write.edge");
    }

    /** Integer counter value (0 if never counted). */
    public int count(String key) {
        return counts.getOrDefault(key, 0);
    }

    /** Reset all state — allows reuse across files if needed. */
    public void reset() {
        starts.clear();
        durations.clear();
        counts.clear();
    }
}
