package studio.seer.lineage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.FriggTenantLookup;
import studio.seer.tenantrouting.FriggYggLineageRegistry;
import studio.seer.tenantrouting.TenantNotAvailableException;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TC-SHT-10 — MTN-01: FriggYggLineageRegistry routes tenantAlias → YGG URL.
 *
 * <p>Shuttle consumer contract test: verifies that the registry used by
 * shuttle service layer correctly maps each tenant alias to a distinct,
 * tenant-scoped ArcadeDB connection. Tests are written from the perspective
 * of shuttle's service layer, complementing the registry-level unit tests
 * in the {@code tenant-routing} library.
 *
 * <p>Uses the same lightweight-stub approach as the library's own test —
 * no real HTTP, no FRIGG database needed.
 */
class FriggYggLineageRegistryTest {

    private FriggYggLineageRegistry registry;
    private StubLookup stub;

    @BeforeEach
    void setUp() throws Exception {
        registry = new FriggYggLineageRegistry();
        setField(registry, "yggBaseUrl",       "http://ygg-test:2480");
        setField(registry, "yggUser",          "root");
        setField(registry, "yggPassword",      "test");
        setField(registry, "cacheMaxEntries",  8);
        setField(registry, "cacheTtl",         Duration.ofMinutes(30));

        stub = new StubLookup();
        setField(registry, "lookup", stub);
        setField(registry, "http",   java.net.http.HttpClient.newHttpClient());
    }

    // ── TC-SHT-10-A: active tenant → correct per-tenant database ──────────────

    @Test
    void activeTenant_resourceFor_returnsConnectionWithCorrectDatabase() {
        stub.add(routing("acme", "ACTIVE", "hound_acme"));

        ArcadeConnection conn = registry.resourceFor("acme");

        assertThat(conn).isNotNull();
        assertThat(conn.databaseName()).isEqualTo("hound_acme");
    }

    @Test
    void twoTenants_resourceFor_returnDistinctConnectionsWithDistinctDatabases() {
        stub.add(routing("acme", "ACTIVE", "hound_acme"));
        stub.add(routing("beta", "ACTIVE", "hound_beta"));

        ArcadeConnection acme = registry.resourceFor("acme");
        ArcadeConnection beta = registry.resourceFor("beta");

        // MTN-01: different tenants → different database names
        assertThat(acme.databaseName()).isEqualTo("hound_acme");
        assertThat(beta.databaseName()).isEqualTo("hound_beta");
        assertThat(acme).isNotSameAs(beta);
    }

    // ── TC-SHT-10-B: per-tenant cache prevents redundant FRIGG lookups ─────────

    @Test
    void calledTwice_returnsCachedInstance_singleFriggLookup() {
        stub.add(routing("acme", "ACTIVE", "hound_acme"));

        ArcadeConnection first  = registry.resourceFor("acme");
        ArcadeConnection second = registry.resourceFor("acme");

        assertThat(first).isSameAs(second);
        assertThat(stub.calls.get()).isEqualTo(1);
    }

    // ── TC-SHT-10-C: status gating — shuttle sees correct exception semantics ──

    @Test
    void suspendedTenant_throwsTenantNotAvailableException() {
        stub.add(routing("suspended", "SUSPENDED", "hound_suspended"));

        assertThatThrownBy(() -> registry.resourceFor("suspended"))
                .isInstanceOf(TenantNotAvailableException.class)
                .extracting("reason")
                .isEqualTo(TenantNotAvailableException.Reason.SUSPENDED);
    }

    @Test
    void unknownTenant_throwsNotFound() {
        // No entry registered in stub → Optional.empty() → NOT_FOUND

        assertThatThrownBy(() -> registry.resourceFor("ghost-tenant"))
                .isInstanceOf(TenantNotAvailableException.class)
                .extracting("reason")
                .isEqualTo(TenantNotAvailableException.Reason.NOT_FOUND);
    }

    @Test
    void nullAlias_throwsNotFound() {
        assertThatThrownBy(() -> registry.resourceFor(null))
                .isInstanceOf(TenantNotAvailableException.class);
    }

    // ── TC-SHT-10-D: cache invalidation restores fresh lookup on next call ─────

    @Test
    void invalidate_thenResourceFor_performsNewFriggLookup() {
        stub.add(routing("acme", "ACTIVE", "hound_acme"));

        registry.resourceFor("acme");                     // lookup #1
        registry.resourceFor("acme");                     // served from cache
        assertThat(stub.calls.get()).isEqualTo(1);

        registry.invalidate("acme");                      // evict cache entry
        registry.resourceFor("acme");                     // lookup #2

        assertThat(stub.calls.get()).isEqualTo(2);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private static FriggTenantLookup.TenantRouting routing(String alias, String status,
                                                             String dbName) {
        return new FriggTenantLookup.TenantRouting(
                alias, status, 1,
                dbName,
                "hound_src_" + alias,
                "dali_" + alias,
                null,          // yggInstanceUrl — use default
                "kc-" + alias,
                null);         // connectionCap
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** Stub for {@link FriggTenantLookup} — no real FRIGG HTTP calls. */
    private static final class StubLookup extends FriggTenantLookup {
        private final Map<String, FriggTenantLookup.TenantRouting> store = new HashMap<>();
        final AtomicInteger calls = new AtomicInteger();

        StubLookup() {
            super(java.net.http.HttpClient.newHttpClient(), "http://nowhere", "u", "p");
        }

        void add(FriggTenantLookup.TenantRouting r) {
            store.put(r.tenantAlias(), r);
        }

        @Override
        public Optional<FriggTenantLookup.TenantRouting> lookup(String alias) {
            calls.incrementAndGet();
            return Optional.ofNullable(store.get(alias));
        }
    }
}
