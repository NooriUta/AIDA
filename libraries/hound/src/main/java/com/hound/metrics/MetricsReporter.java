package com.hound.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Периодический отчёт по метрикам.
 * Исправлено: теперь использует printReport() вместо удалённого snapshot().
 */
public class MetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(MetricsReporter.class);

    private final MetricsCollector metricsCollector;
    private final ScheduledExecutorService scheduler;
    private final long reportIntervalSeconds;
    private final Consumer<String> customReporter;   // изменено на String для простоты

    public MetricsReporter(MetricsCollector metricsCollector, long reportIntervalSeconds) {
        this(metricsCollector, reportIntervalSeconds, null);
    }

    public MetricsReporter(MetricsCollector metricsCollector, long reportIntervalSeconds,
                           Consumer<String> customReporter) {
        this.metricsCollector = metricsCollector;
        this.reportIntervalSeconds = reportIntervalSeconds;
        this.customReporter = customReporter;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::report, reportIntervalSeconds, reportIntervalSeconds, TimeUnit.SECONDS);
        logger.info("Metrics reporter started with interval {} seconds", reportIntervalSeconds);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Metrics reporter stopped");
    }

    private void report() {
        try {
            // Основной способ вывода метрик
            metricsCollector.printReport();

            // Если нужен кастомный обработчик (например, отправка в Prometheus или файл)
            if (customReporter != null) {
                customReporter.accept("Metrics reported at " + java.time.Instant.now());
            }

        } catch (Exception e) {
            logger.error("Error during metrics reporting", e);
        }
    }
}