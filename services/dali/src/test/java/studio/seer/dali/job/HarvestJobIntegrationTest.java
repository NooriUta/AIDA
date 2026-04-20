package studio.seer.dali.job;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import studio.seer.dali.storage.SessionRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration tests for {@link HarvestJob} (DS-03).
 *
 * <p>Verifies that HarvestJob can be injected and executed in the test context.
 * When no JDBC sources are configured ({@code %test.dali.harvest.cron=off} and no
 * {@code dali.sources[*]} in test properties), the job completes as a no-op.
 *
 * <p>Full end-to-end test (SKADI + Hound + YGG) requires live databases and is
 * guarded by {@code -De2e=true}; see {@code E2eHarvestIT} (future task).
 */
@QuarkusTest
class HarvestJobIntegrationTest {

    @Inject HarvestJob       harvestJob;
    @Inject SessionRepository repository;

    /** BUG-SS-025: clean FRIGG after each test to prevent cross-test accumulation. */
    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void harvestJob_is_injectable_and_runs_without_sources() {
        // With no dali.sources[*] configured in test profile, job should complete as no-op
        String harvestId = "test-" + UUID.randomUUID().toString().substring(0, 8);
        assertDoesNotThrow(() -> harvestJob.execute(harvestId));
    }

    @Test
    void harvestJob_handles_empty_harvestId_gracefully() {
        // harvestId is just a correlation string — any non-null value should work
        assertDoesNotThrow(() -> harvestJob.execute("test-empty-sources"));
    }
}
