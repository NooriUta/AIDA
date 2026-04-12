package studio.seer.heimdall.metrics;

/**
 * Immutable snapshot of aggregated HEIMDALL metrics.
 *
 * @param atomsExtracted  число событий ATOM_EXTRACTED
 * @param filesParsed     FILE_PARSING_COMPLETED
 * @param toolCallsTotal  TOOL_CALL_COMPLETED
 * @param activeWorkers   WORKER_ASSIGNED (монотонный счётчик в Sprint 2)
 * @param queueDepth      JOB_ENQUEUED - JOB_COMPLETED (gauge)
 * @param resolutionRate  resolutions/filesParsed * 100.0, NaN если filesParsed==0
 */
public record MetricsSnapshot(
        long   atomsExtracted,
        long   filesParsed,
        long   toolCallsTotal,
        long   activeWorkers,
        long   queueDepth,
        double resolutionRate
) {}
