package studio.seer.heimdall.analytics;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import studio.seer.shared.HeimdallEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UA-03: In-memory 24-hour sliding-window UX metrics aggregator.
 *
 * <p>Accepts {@link HeimdallEvent} records and maintains a ring-deque bounded at
 * {@value MAX_EVENTS} entries. On each {@link #getUxSummary()} call the deque is
 * scanned (O(n)) and events older than {@value WINDOW_MS} ms are dropped.
 *
 * <p>Tracked signals:
 * <ul>
 *   <li><b>Hot nodes</b>     — top-10 {@code LOOM_NODE_SELECTED} nodes by click count</li>
 *   <li><b>Level distribution</b> — INFO/WARN/ERROR event counts in window</li>
 *   <li><b>Slow renders</b>  — most-recent 20 {@code LOOM_VIEW_SLOW} entries</li>
 *   <li><b>Active users</b>  — distinct {@code userId} or {@code sessionId} in window</li>
 * </ul>
 *
 * <p>Thread-safe: the deque is {@link ConcurrentLinkedDeque}; summary computation
 * is done under a brief snapshot copy.
 */
@ApplicationScoped
public class UxAggregator {

    private static final Logger LOG = Logger.getLogger(UxAggregator.class);

    /** Maximum events kept in the deque. Oldest are dropped when exceeded. */
    static final int MAX_EVENTS = 5_000;

    /** Sliding-window width in milliseconds (24 h). */
    static final long WINDOW_MS = 24L * 60 * 60 * 1_000;

    /** Maximum slow-render entries returned in summary. */
    static final int SLOW_RENDER_MAX = 20;

    /** Top-N hot nodes returned in summary. */
    static final int HOT_NODES_TOP_N = 10;

    private final ConcurrentLinkedDeque<HeimdallEvent> window = new ConcurrentLinkedDeque<>();
    private final AtomicInteger size = new AtomicInteger(0);

    // ── Public API ────────────────────────────────────────────────────────────

    /** Called by {@code EventResource} after every accepted event (same path as MetricsCollector). */
    public void record(HeimdallEvent event) {
        if (event == null) return;
        window.addLast(event);
        if (size.incrementAndGet() > MAX_EVENTS) {
            window.pollFirst(); // drop oldest
            size.decrementAndGet();
        }
    }

    /**
     * Compute a {@link UxSummary} from events inside the 24-hour window.
     * Prunes expired entries as a side-effect.
     */
    public UxSummary getUxSummary() {
        long cutoff = Instant.now().toEpochMilli() - WINDOW_MS;

        // Snapshot: collect all events in window, pruning stale ones
        List<HeimdallEvent> recent = new ArrayList<>();
        HeimdallEvent head;
        while ((head = window.peekFirst()) != null && head.timestamp() < cutoff) {
            window.pollFirst();
            size.decrementAndGet();
        }
        recent.addAll(window); // non-atomic read — acceptable for metrics

        // -- Hot nodes (LOOM_NODE_SELECTED → top-N by click count) ---------------
        Map<String, Integer> nodeClicks = new HashMap<>();
        Map<String, String>  nodeTypes  = new HashMap<>();

        // -- Level distribution --------------------------------------------------
        long infoCount = 0, warnCount = 0, errorCount = 0;

        // -- Slow renders --------------------------------------------------------
        List<SlowRender> slowRenders = new ArrayList<>();

        // -- Active sessions (distinct) ------------------------------------------
        HashSet<String> activeSessions = new HashSet<>();

        for (HeimdallEvent e : recent) {
            // Level distribution
            if (e.level() != null) {
                switch (e.level().name()) {
                    case "INFO"  -> infoCount++;
                    case "WARN"  -> warnCount++;
                    case "ERROR" -> errorCount++;
                }
            }

            // Active sessions
            if (e.sessionId() != null) activeSessions.add(e.sessionId());

            if (e.eventType() == null) continue;
            switch (e.eventType()) {
                case "LOOM_NODE_SELECTED" -> {
                    Map<String, Object> p = e.payload() != null ? e.payload() : Map.of();
                    String nodeId   = String.valueOf(p.getOrDefault("node_id",   "?"));
                    String nodeType = String.valueOf(p.getOrDefault("node_type", "unknown"));
                    nodeClicks.merge(nodeId, 1, Integer::sum);
                    nodeTypes.putIfAbsent(nodeId, nodeType);
                }
                case "LOOM_VIEW_SLOW" -> {
                    Map<String, Object> p = e.payload() != null ? e.payload() : Map.of();
                    long renderMs   = toLong(p.getOrDefault("render_time_ms", 0));
                    int  nodesCount = toInt(p.getOrDefault("nodes_count", 0));
                    String sessionId = e.sessionId() != null ? e.sessionId() : "";
                    slowRenders.add(new SlowRender(e.timestamp(), nodesCount, renderMs, sessionId));
                }
                default -> { /* other types not aggregated here */ }
            }
        }

        // -- Top-N hot nodes by click count -----------------------------------
        List<HotNode> hotNodes = nodeClicks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(HOT_NODES_TOP_N)
                .map(e -> new HotNode(e.getKey(), nodeTypes.getOrDefault(e.getKey(), "unknown"), e.getValue()))
                .toList();

        // -- Most-recent slow renders -----------------------------------------
        List<SlowRender> recentSlowRenders = slowRenders.stream()
                .sorted(Comparator.comparingLong(SlowRender::timestamp).reversed())
                .limit(SLOW_RENDER_MAX)
                .toList();

        return new UxSummary(
                hotNodes,
                new LevelDistribution(infoCount, warnCount, errorCount),
                recentSlowRenders,
                activeSessions.size(),
                recent.size(),
                WINDOW_MS
        );
    }

    // ── Records ───────────────────────────────────────────────────────────────

    public record HotNode(String nodeId, String nodeType, int clicks) {}

    public record LevelDistribution(long info, long warn, long error) {}

    public record SlowRender(long timestamp, int nodesCount, long renderMs, String sessionId) {}

    public record UxSummary(
            List<HotNode>        hotNodes,
            LevelDistribution    levelDistribution,
            List<SlowRender>     slowRenders,
            int                  activeSessionCount,
            int                  totalEventsInWindow,
            long                 windowMs
    ) {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }
}
