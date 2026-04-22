package studio.seer.tenantrouting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FenceGateTest {

    private FenceGate gate;

    @BeforeEach
    void setUp() {
        gate = new FenceGate();
    }

    @Test
    void admit_firstEvent_returnsApply() {
        assertEquals(FenceGate.Decision.APPLY, gate.admit("acme", 10L));
        assertEquals(10L, gate.lastSeen("acme"));
    }

    @Test
    void admit_monotonicallyIncreasing_allApply() {
        assertEquals(FenceGate.Decision.APPLY, gate.admit("acme", 10L));
        assertEquals(FenceGate.Decision.APPLY, gate.admit("acme", 11L));
        assertEquals(FenceGate.Decision.APPLY, gate.admit("acme", 100L));
        assertEquals(100L, gate.lastSeen("acme"));
    }

    @Test
    void admit_equalToLastSeen_returnsReplay() {
        gate.admit("acme", 10L);
        assertEquals(FenceGate.Decision.REPLAY, gate.admit("acme", 10L));
        assertEquals(10L, gate.lastSeen("acme"));
    }

    @Test
    void admit_olderThanLastSeen_returnsStale() {
        gate.admit("acme", 100L);
        assertEquals(FenceGate.Decision.STALE, gate.admit("acme", 50L));
        assertEquals(100L, gate.lastSeen("acme"));  // unchanged
    }

    @Test
    void admit_zeroToken_returnsLegacy() {
        assertEquals(FenceGate.Decision.LEGACY, gate.admit("acme", 0L));
        assertEquals(0L, gate.lastSeen("acme"));  // legacy does not advance
    }

    @Test
    void admit_legacyAfterNormalEvents_doesNotAdvanceOrReject() {
        gate.admit("acme", 50L);
        assertEquals(FenceGate.Decision.LEGACY, gate.admit("acme", 0L));
        assertEquals(50L, gate.lastSeen("acme"));
    }

    @Test
    void admit_separateTenantsHaveIndependentState() {
        gate.admit("acme", 100L);
        gate.admit("beta", 5L);
        assertEquals(100L, gate.lastSeen("acme"));
        assertEquals(5L,   gate.lastSeen("beta"));

        // Stale for acme doesn't affect beta
        assertEquals(FenceGate.Decision.STALE, gate.admit("acme", 50L));
        assertEquals(FenceGate.Decision.APPLY, gate.admit("beta", 6L));
    }

    @Test
    void admit_payloadOverload_extractsToken() {
        assertEquals(FenceGate.Decision.APPLY,
                gate.admit("acme", Map.of("_fenceToken", 42L, "action", "invalidated")));
        assertEquals(42L, gate.lastSeen("acme"));
    }

    @Test
    void admit_payloadOverload_absentTokenIsLegacy() {
        assertEquals(FenceGate.Decision.LEGACY,
                gate.admit("acme", Map.of("action", "invalidated")));
    }

    @Test
    void admit_rejectsBlankTenant() {
        assertThrows(IllegalArgumentException.class, () -> gate.admit("", 10L));
        assertThrows(IllegalArgumentException.class, () -> gate.admit(null, 10L));
    }

    @Test
    void admit_rejectsNegativeToken() {
        assertThrows(IllegalArgumentException.class, () -> gate.admit("acme", -1L));
    }

    @Test
    void reset_clearsTenantState() {
        gate.admit("acme", 100L);
        gate.reset("acme");
        assertEquals(0L, gate.lastSeen("acme"));
        // After reset, any positive token applies again
        assertEquals(FenceGate.Decision.APPLY, gate.admit("acme", 50L));
    }

    @Test
    void admit_concurrentRace_preservesMonotonicity() throws InterruptedException {
        // 8 threads, each issuing tokens 1..500 in order; final lastSeen must be 500.
        ExecutorService exec = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(8);
        ConcurrentHashMap<FenceGate.Decision, Long> tallies = new ConcurrentHashMap<>();
        try {
            for (int t = 0; t < 8; t++) {
                exec.submit(() -> {
                    try { start.await(); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    for (long i = 1; i <= 500; i++) {
                        FenceGate.Decision d = gate.admit("race", i);
                        tallies.merge(d, 1L, Long::sum);
                    }
                    done.countDown();
                });
            }
            start.countDown();
            assertTrue(done.await(30, TimeUnit.SECONDS));
        } finally {
            exec.shutdown();
        }
        assertEquals(500L, gate.lastSeen("race"));
        // Exactly 500 APPLY decisions across all threads (the winner of each token race).
        assertEquals(500L, tallies.getOrDefault(FenceGate.Decision.APPLY, 0L));
        // Remaining 7 * 500 decisions split between REPLAY (equal) and STALE (older).
        long replayStale = tallies.getOrDefault(FenceGate.Decision.REPLAY, 0L)
                         + tallies.getOrDefault(FenceGate.Decision.STALE,  0L);
        assertEquals(7L * 500L, replayStale);
    }
}
