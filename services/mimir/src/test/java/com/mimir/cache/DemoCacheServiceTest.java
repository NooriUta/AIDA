package com.mimir.cache;

import com.mimir.model.MimirAnswer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.QuarkusTestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(DemoCacheServiceTest.DemoModeOnProfile.class)
class DemoCacheServiceTest {

    @Inject DemoCacheService service;

    public static class DemoModeOnProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("mimir.demo-mode", "true");
        }
    }

    @Test
    void demoModeFlagIsRead() {
        assertThat(service.isDemoMode()).isTrue();
    }

    @Test
    void cacheLoadsAllSeedEntries() {
        assertThat(service.entryCount()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void hitReturnsAnswerWithSimulatedDelay() {
        long t0 = System.currentTimeMillis();
        Optional<MimirAnswer> hit = service.tryCache("Какие процедуры читают из ORDERS?");
        long elapsed = System.currentTimeMillis() - t0;

        assertThat(hit).isPresent();
        MimirAnswer a = hit.get();
        assertThat(a.answer()).contains("ORDERS");
        assertThat(a.toolCallsUsed()).isNotEmpty();
        assertThat(a.highlightNodeIds()).isNotEmpty();
        // simulatedDelayMs from fixture is 2800 — at least 2s actually slept
        assertThat(elapsed).isGreaterThanOrEqualTo(2_000L);
    }

    @Test
    void caseInsensitiveMatch() {
        Optional<MimirAnswer> hit = service.tryCache("schema summary please");
        assertThat(hit).isPresent();
    }

    @Test
    void missReturnsEmpty() {
        Optional<MimirAnswer> miss = service.tryCache("totally unrelated question about quantum physics");
        assertThat(miss).isEmpty();
    }

    @Test
    void blankQuestionReturnsEmpty() {
        assertThat(service.tryCache("")).isEmpty();
        assertThat(service.tryCache(null)).isEmpty();
        assertThat(service.tryCache("   ")).isEmpty();
    }
}
