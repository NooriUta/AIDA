package studio.seer.anvil;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.anvil.model.*;
import studio.seer.anvil.service.AnvilCache;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-ANV-03 / TC-ANV-04 — MTN-30: Anvil cache MUST NOT allow cross-tenant
 * cache poisoning.
 *
 * <ul>
 *   <li>TC-ANV-03: The {@link AnvilCache#key} builder includes {@code tenantAlias}
 *       as the first segment — two calls with the same logical parameters but
 *       different tenant aliases produce distinct keys.</li>
 *   <li>TC-ANV-04: An entry written for tenant A is not retrievable via tenant B's
 *       key, even when all other key components (nodeId, direction, maxHops,
 *       dbName) are identical — proving no collisions across tenant boundaries.</li>
 * </ul>
 *
 * <p>This test is intentionally isolated from {@link AnvilCacheTest}: that test
 * covers functional CRUD behaviour; this one focuses exclusively on the
 * security isolation invariant (MTN-30).
 */
@QuarkusTest
class AnvilCacheIsolationTest {

    @Inject
    AnvilCache cache;

    @BeforeEach
    void wipe() {
        // Full cache wipe — "" prefix matches every key (invalidateDb on empty string)
        cache.invalidateDb("");
    }

    // ── TC-ANV-03: key format includes tenantAlias ────────────────────────────

    @Test
    void key_startsWithTenantAlias() {
        String key = AnvilCache.key("acme", "LOAD_FX", "downstream", 3, "hound_acme");
        assertTrue(key.startsWith("acme:"),
                "Cache key must start with tenantAlias, got: " + key);
    }

    @Test
    void key_sameParamsDifferentTenants_areNotEqual() {
        // MTN-30: identical logical query for two different tenants MUST produce different keys
        String keyA = AnvilCache.key("acme", "NODE_X", "downstream", 5, "hound_shared");
        String keyB = AnvilCache.key("beta", "NODE_X", "downstream", 5, "hound_shared");

        assertNotEquals(keyA, keyB,
                "Keys for two different tenants must differ even with identical query params");
    }

    @Test
    void key_sameParamsDifferentTenants_bothStartWithRespectiveTenantAlias() {
        String keyAcme = AnvilCache.key("acme", "N1", "upstream", 2, "hound_acme");
        String keyBeta = AnvilCache.key("beta", "N1", "upstream", 2, "hound_beta");

        assertTrue(keyAcme.startsWith("acme:"), "acme key: " + keyAcme);
        assertTrue(keyBeta.startsWith("beta:"), "beta key: " + keyBeta);
    }

    // ── TC-ANV-04: cross-tenant read isolation ────────────────────────────────

    @Test
    void putForTenantA_getForTenantB_returnsNull() {
        // Write a result under tenant A's key
        String keyA = AnvilCache.key("acme", "SENSITIVE_NODE", "downstream", 5, "hound_acme");
        cache.put(keyA, sampleResult("SENSITIVE_NODE"));

        // Attempt to read it using tenant B's key (same logical params)
        String keyB = AnvilCache.key("beta", "SENSITIVE_NODE", "downstream", 5, "hound_acme");

        assertNull(cache.get(keyB),
                "MTN-30: Tenant B must NOT be able to read Tenant A's cached result");
    }

    @Test
    void twoTenantsCanHaveSeparateCacheEntriesWithSameNodeId() {
        String keyAcme = AnvilCache.key("acme", "SHARED_NODE", "downstream", 3, "hound_acme");
        String keyBeta = AnvilCache.key("beta", "SHARED_NODE", "downstream", 3, "hound_beta");

        ImpactResult resultAcme = sampleResult("acme-data");
        ImpactResult resultBeta = sampleResult("beta-data");

        cache.put(keyAcme, resultAcme);
        cache.put(keyBeta, resultBeta);

        ImpactResult fetchedAcme = cache.get(keyAcme);
        ImpactResult fetchedBeta = cache.get(keyBeta);

        assertNotNull(fetchedAcme, "Acme entry must be present");
        assertNotNull(fetchedBeta, "Beta entry must be present");
        assertEquals("acme-data", fetchedAcme.rootNode().id());
        assertEquals("beta-data", fetchedBeta.rootNode().id());
    }

    @Test
    void invalidateTenant_removesOnlyThatTenantsCacheEntries() {
        String keyAcme1 = AnvilCache.key("acme", "NODE_A", "downstream", 3, "hound_acme");
        String keyAcme2 = AnvilCache.key("acme", "NODE_B", "upstream",   2, "hound_acme");
        String keyBeta  = AnvilCache.key("beta", "NODE_A", "downstream", 3, "hound_beta");

        cache.put(keyAcme1, sampleResult("a1"));
        cache.put(keyAcme2, sampleResult("a2"));
        cache.put(keyBeta,  sampleResult("b1"));

        cache.invalidateTenant("acme");

        assertNull(cache.get(keyAcme1), "acme/NODE_A must be evicted");
        assertNull(cache.get(keyAcme2), "acme/NODE_B must be evicted");
        assertNotNull(cache.get(keyBeta), "beta entry must survive acme invalidation");
    }

    @Test
    void invalidateTenantA_doesNotEvictTenantBEntry() {
        String keyB = AnvilCache.key("beta", "CROSS_NODE", "downstream", 1, "hound_beta");
        cache.put(keyB, sampleResult("beta-value"));

        cache.invalidateTenant("acme");   // evict a different tenant

        assertNotNull(cache.get(keyB),
                "MTN-30: invalidating tenant A must not affect tenant B's cache entry");
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ImpactResult sampleResult(String rootId) {
        ImpactNode root = new ImpactNode(rootId, "DaliRoutine", rootId, 0);
        return new ImpactResult(root,
                List.of(new ImpactNode("c1", "DaliTable", "HR.ORDERS", 1)),
                List.of(), 1, false, false, 10L);
    }
}
