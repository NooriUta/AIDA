package studio.seer.heimdall.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.shared.HeimdallEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Aggregates HEIMDALL event counters using AtomicLong for accuracy.
 *
 * AtomicLong is preferred over Micrometer Counter#count() which accumulates double precision error.
 * MeterRegistry is injected for Prometheus Gauge registration (future Prometheus scrape endpoint).
 *
 * Thread-safe: all fields are AtomicLong, no external locking needed.
 */
@ApplicationScoped
public class MetricsCollector {

    private static final Logger LOG = Logger.getLogger(MetricsCollector.class);

    @Inject MeterRegistry meterRegistry;

    private final AtomicLong atomsExtracted   = new AtomicLong(0);
    private final AtomicLong filesParsed      = new AtomicLong(0);
    private final AtomicLong toolCallsTotal   = new AtomicLong(0);
    private final AtomicLong resolutionsTotal = new AtomicLong(0);
    private final AtomicLong activeWorkers    = new AtomicLong(0);
    private final AtomicLong queueDepth       = new AtomicLong(0);

    public void record(HeimdallEvent event) {
        if (event == null || event.eventType() == null) return;
        switch (event.eventType()) {
            case "ATOM_EXTRACTED"         -> atomsExtracted.incrementAndGet();
            case "FILE_PARSING_COMPLETED" -> filesParsed.incrementAndGet();
            case "TOOL_CALL_COMPLETED"    -> toolCallsTotal.incrementAndGet();
            case "RESOLUTION_COMPLETED"   -> resolutionsTotal.incrementAndGet();
            case "WORKER_ASSIGNED"        -> activeWorkers.incrementAndGet();
            case "JOB_ENQUEUED"           -> queueDepth.incrementAndGet();
            case "JOB_COMPLETED"          -> { long d = queueDepth.decrementAndGet(); if (d < 0) queueDepth.set(0); }
            case "DEMO_RESET"             -> reset();
            default                       -> { /* прочие типы не агрегируются */ }
        }
    }

    public MetricsSnapshot snapshot() {
        long parsed   = filesParsed.get();
        long resolved = resolutionsTotal.get();
        double rate   = parsed == 0 ? Double.NaN : (resolved * 100.0) / parsed;
        return new MetricsSnapshot(
                atomsExtracted.get(), parsed, toolCallsTotal.get(),
                activeWorkers.get(), queueDepth.get(), rate
        );
    }

    public void reset() {
        atomsExtracted.set(0); filesParsed.set(0); toolCallsTotal.set(0);
        resolutionsTotal.set(0); activeWorkers.set(0); queueDepth.set(0);
        LOG.info("MetricsCollector reset");
    }
}
