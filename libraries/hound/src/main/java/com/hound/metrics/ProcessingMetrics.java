package com.hound.metrics;

import java.time.Instant;

/**
 * Снимок метрик обработки
 */
public class ProcessingMetrics {
    private final long filesProcessed;
    private final long filesSkipped;
    private final long filesFailed;
    private final long nodesCreated;
    private final long relationshipsCreated;
    private final long errors;
    private final long totalProcessingTimeMs;
    private final double averageProcessingTimeMs;
    private final long minProcessingTimeMs;
    private final long maxProcessingTimeMs;
    private final long elapsedTimeMs;
    private final Instant timestamp;

    public ProcessingMetrics(long filesProcessed, long filesSkipped, long filesFailed,
                             long nodesCreated, long relationshipsCreated, long errors,
                             long totalProcessingTimeMs, double averageProcessingTimeMs,
                             long minProcessingTimeMs, long maxProcessingTimeMs, long elapsedTimeMs) {
        this.filesProcessed = filesProcessed;
        this.filesSkipped = filesSkipped;
        this.filesFailed = filesFailed;
        this.nodesCreated = nodesCreated;
        this.relationshipsCreated = relationshipsCreated;
        this.errors = errors;
        this.totalProcessingTimeMs = totalProcessingTimeMs;
        this.averageProcessingTimeMs = averageProcessingTimeMs;
        this.minProcessingTimeMs = minProcessingTimeMs;
        this.maxProcessingTimeMs = maxProcessingTimeMs;
        this.elapsedTimeMs = elapsedTimeMs;
        this.timestamp = Instant.now();
    }

    // Getters
    public long getFilesProcessed() { return filesProcessed; }
    public long getFilesSkipped() { return filesSkipped; }
    public long getFilesFailed() { return filesFailed; }
    public long getNodesCreated() { return nodesCreated; }
    public long getRelationshipsCreated() { return relationshipsCreated; }
    public long getErrors() { return errors; }
    public long getTotalProcessingTimeMs() { return totalProcessingTimeMs; }
    public double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
    public long getMinProcessingTimeMs() { return minProcessingTimeMs; }
    public long getMaxProcessingTimeMs() { return maxProcessingTimeMs; }
    public long getElapsedTimeMs() { return elapsedTimeMs; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format(
                "ProcessingMetrics{files=%d, nodes=%d, rels=%d, errors=%d, time=%.2fms avg, %dms elapsed}",
                filesProcessed, nodesCreated, relationshipsCreated, errors,
                averageProcessingTimeMs, elapsedTimeMs
        );
    }
}