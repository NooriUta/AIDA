package studio.seer.anvil;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.anvil.model.*;
import studio.seer.anvil.service.AnvilCache;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AnvilCacheTest {

    @Inject
    AnvilCache cache;

    @BeforeEach
    void clear() {
        cache.asMap().clear();  // true full wipe — invalidateDb("") only removes keys ending in ":"
    }

    @Test
    void putAndGet_returnsCachedResult() {
        String key = AnvilCache.key("default", "LOAD_FX_RATES", "downstream", 5, "hound_default");
        ImpactResult result = sampleResult();

        cache.put(key, result);
        ImpactResult fetched = cache.get(key);

        assertNotNull(fetched);
        assertEquals("LOAD_FX_RATES", fetched.rootNode().id());
        assertEquals(2, fetched.totalAffected());
    }

    @Test
    void get_missingKey_returnsNull() {
        assertNull(cache.get("nonexistent:key"));
    }

    @Test
    void invalidateDb_removesOnlyMatchingDb() {
        String keyA = AnvilCache.key("default", "NODE_A", "downstream", 5, "hound_default");
        String keyB = AnvilCache.key("default", "NODE_B", "upstream",   3, "other_db");

        cache.put(keyA, sampleResult());
        cache.put(keyB, sampleResult());

        cache.invalidateDb("hound_default");

        assertNull(cache.get(keyA), "hound_default key should be evicted");
        assertNotNull(cache.get(keyB), "other_db key should remain");
    }

    @Test
    void size_reflectsCacheContents() {
        assertEquals(0, cache.size());
        cache.put("k1", sampleResult());
        cache.put("k2", sampleResult());
        assertEquals(2, cache.size());
    }

    @Test
    void key_includesTenantAlias_soCrossTenantKeysDiffer() {
        // MTN-30: same nodeId + direction + maxHops + dbName across two tenants
        // must not collide in the cache.
        String keyTenantA = AnvilCache.key("acme", "NODE_X", "downstream", 3, "hound_acme");
        String keyTenantB = AnvilCache.key("beta", "NODE_X", "downstream", 3, "hound_acme");
        assertNotEquals(keyTenantA, keyTenantB);
        assertTrue(keyTenantA.startsWith("acme:"));
        assertTrue(keyTenantB.startsWith("beta:"));
    }

    @Test
    void invalidateTenant_removesOnlyMatchingTenant() {
        String keyAcme = AnvilCache.key("acme", "NODE_A", "downstream", 5, "hound_acme");
        String keyBeta = AnvilCache.key("beta", "NODE_A", "downstream", 5, "hound_beta");
        cache.put(keyAcme, sampleResult());
        cache.put(keyBeta, sampleResult());

        cache.invalidateTenant("acme");

        assertNull(cache.get(keyAcme));
        assertNotNull(cache.get(keyBeta));
    }

    private ImpactResult sampleResult() {
        ImpactNode root = new ImpactNode("LOAD_FX_RATES", "DaliRoutine", "LOAD_FX_RATES", 0);
        return new ImpactResult(root,
                List.of(new ImpactNode("g1", "DaliTable", "HR.ORDERS", 1),
                        new ImpactNode("g2", "DaliColumn", "HR.ORDERS.STATUS", 2)),
                List.of(), 2, false, false, 45L);
    }
}
