package studio.seer.heimdall.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FenceTokenProvider — direct instantiation (no CDI, no FRIGG).
 * Only tests the pure counter logic; onStart/safeSeed require FRIGG and are omitted.
 */
class FenceTokenProviderTest {

    private FenceTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new FenceTokenProvider();
        // frigg field is null — onStart not called; counter starts at 0.
    }

    // ── next() ────────────────────────────────────────────────────────────────

    @Test
    void next_firstCall_returnsOne() {
        assertEquals(1L, provider.next());
    }

    @Test
    void next_consecutiveCalls_returnStrictlyIncreasing() {
        long a = provider.next();
        long b = provider.next();
        long c = provider.next();
        assertTrue(b > a, "second > first");
        assertTrue(c > b, "third > second");
    }

    @Test
    void next_afterSetCounter_continuesFromSeed() {
        provider.setCounterForTests(100L);
        assertEquals(101L, provider.next());
        assertEquals(102L, provider.next());
    }

    @Test
    void next_counterAtMaxLong_throwsIllegalStateException() {
        // AtomicLong.incrementAndGet on MAX_VALUE wraps to MIN_VALUE (negative → overflowed)
        provider.setCounterForTests(Long.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> provider.next());
    }

    // ── current() ────────────────────────────────────────────────────────────

    @Test
    void current_initialState_returnsZero() {
        assertEquals(0L, provider.current());
    }

    @Test
    void current_doesNotIncrementCounter() {
        provider.setCounterForTests(50L);
        long first  = provider.current();
        long second = provider.current();
        assertEquals(50L, first);
        assertEquals(50L, second, "current() must not side-effect the counter");
    }

    @Test
    void current_reflectsNextCalls() {
        provider.next(); // 1
        provider.next(); // 2
        provider.next(); // 3
        assertEquals(3L, provider.current());
    }

    // ── setCounterForTests() ──────────────────────────────────────────────────

    @Test
    void setCounterForTests_updatesCounterDirectly() {
        provider.setCounterForTests(999L);
        assertEquals(999L, provider.current());
    }

    @Test
    void setCounterForTests_toZero_nextReturnsOne() {
        provider.next(); // advance to 1
        provider.setCounterForTests(0L);
        assertEquals(1L, provider.next());
    }
}
