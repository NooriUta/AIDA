package studio.seer.dali.job;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jobrunr.jobs.annotations.Job;
import org.junit.jupiter.api.Test;
import studio.seer.dali.config.DaliConfig;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTA-04 — verifies harvest concurrency configuration.
 *
 * jobrunr OSS 8.5.2 does not support @Job(concurrentJobsByType).
 * Concurrency is bounded by dali.jobrunr.worker-threads (config).
 * The harvest label enables dashboard filtering and future Pro migration.
 */
@QuarkusTest
class HarvestConcurrencyConfigTest {

    @Inject DaliConfig config;

    @Test
    void harvestJob_execute_has_job_annotation_with_harvest_label() throws NoSuchMethodException {
        Method execute = HarvestJob.class.getMethod("execute", String.class, String.class);
        Job annotation = execute.getAnnotation(Job.class);
        assertNotNull(annotation, "@Job annotation must be present on HarvestJob.execute()");
        assertTrue(Arrays.asList(annotation.labels()).contains("harvest"),
                "labels must contain 'harvest' for dashboard filtering");
    }

    @Test
    void worker_threads_config_is_positive() {
        int threads = config.jobrunr().workerThreads();
        assertTrue(threads >= 1,
                "dali.jobrunr.worker-threads must be >= 1 (controls max concurrent harvest jobs)");
    }
}
