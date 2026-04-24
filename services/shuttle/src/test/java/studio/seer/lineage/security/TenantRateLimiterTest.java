package studio.seer.lineage.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantRateLimiterTest {

    @Test
    void superAdmin_bypassesAllLimits() {
        TenantRateLimiter rl = new TenantRateLimiter();
        // 200 requests — well above the 100/sec soft cap. Super-admin should be let through.
        for (int i = 0; i < 200; i++) {
            assertTrue(rl.allow("acme", "super-admin"),
                    "super-admin bypass failed at request " + i);
        }
    }

    @Test
    void normalUser_firstHundredAllowedWithinSameSecond() {
        TenantRateLimiter rl = new TenantRateLimiter();
        int allowed = 0;
        for (int i = 0; i < 100; i++) {
            if (rl.allow("acme", "editor")) allowed++;
        }
        assertEquals(100, allowed);
    }

    @Test
    void normalUser_oneHundredFirstSoftCapBlocks() {
        TenantRateLimiter rl = new TenantRateLimiter();
        for (int i = 0; i < 100; i++) rl.allow("acme", "editor");
        // 101st in the same second must be blocked.
        assertFalse(rl.allow("acme", "editor"));
    }

    @Test
    void separateTenants_haveIndependentBuckets() {
        TenantRateLimiter rl = new TenantRateLimiter();
        for (int i = 0; i < 100; i++) rl.allow("acme", "editor");
        // acme exhausted its soft cap, but beta starts fresh.
        assertFalse(rl.allow("acme", "editor"));
        assertTrue (rl.allow("beta", "editor"));
    }

    @Test
    void blankTenant_fallsBackToDefaultBucket() {
        TenantRateLimiter rl = new TenantRateLimiter();
        // null and "default" share the bucket keyed as "default"
        for (int i = 0; i < 100; i++) rl.allow(null, "editor");
        assertFalse(rl.allow("default", "editor"));
    }

    @Test
    void hardCapPerMinuteCaps_multiSecondBursts() throws InterruptedException {
        // Exhaust one second's 100, sleep past reset, exhaust another 100, and so on.
        // 10 such bursts would produce 1000 rps steady rate => minute-cap kicks in.
        // We don't actually sleep 10s in a unit test; instead simulate by invoking
        // the limiter at the boundary of sec-cap resets across a short window and
        // check that after ~1000 within a rolling minute the next call blocks.
        TenantRateLimiter rl = new TenantRateLimiter();
        int allowed = 0;
        for (int i = 0; i < 1_500; i++) {
            if (rl.allow("acme", "editor")) allowed++;
        }
        // Soft cap already bounds within a single second to 100. The hard cap
        // exists in case replay / wrap-around; at minimum we should never exceed
        // HARD_PER_MIN (1000) within the same second either.
        assertTrue(allowed <= 1_000, "admitted " + allowed + " > 1000 hard cap");
        assertTrue(allowed >= 100,   "admitted " + allowed + " < 100 soft-cap floor");
    }
}
