package studio.seer.tenantrouting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.tenantrouting.FriggTenantLookup.TenantRouting;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MTN-01: Unit coverage for the Frigg-backed lineage registry.
 *
 * Uses a lightweight custom stub for {@link FriggTenantLookup} (to count
 * network calls) rather than Mockito — Mockito can't reliably mock
 * Java 21+ HttpClient. The registry's {@code @PostConstruct init()} is
 * bypassed: we inject config values via reflection and hand-build the
 * lookup stub.
 */
class FriggYggLineageRegistryTest {

    private FriggYggLineageRegistry registry;
    private StubLookup stub;

    @BeforeEach
    void setUp() throws Exception {
        registry = new FriggYggLineageRegistry();
        setField(registry, "yggBaseUrl", "http://ygg:2480");
        setField(registry, "yggUser",    "root");
        setField(registry, "yggPassword", "playwithdata");
        setField(registry, "cacheMaxEntries", 3);                  // small cap for eviction tests
        setField(registry, "cacheTtl",        Duration.ofMinutes(30));

        stub = new StubLookup();
        setField(registry, "lookup", stub);
        setField(registry, "http",   java.net.http.HttpClient.newHttpClient());
    }

    // ── happy path ─────────────────────────────────────────────────────────

    @Test
    void resourceFor_activeTenant_returnsConnectionWithCorrectDb() {
        stub.put(routing("acme", "ACTIVE", 1, "hound_acme", null));

        ArcadeConnection conn = registry.resourceFor("acme");

        assertThat(conn).isNotNull();
        assertThat(conn.databaseName()).isEqualTo("hound_acme");
        assertThat(stub.calls.get()).isEqualTo(1);
    }

    @Test
    void resourceFor_customYggInstanceUrl_isUsed() {
        stub.put(routing("beta", "ACTIVE", 1, "hound_beta", "http://ygg-shard-2:2480"));

        ArcadeConnection conn = registry.resourceFor("beta");

        assertThat(conn).isNotNull();
        assertThat(conn.databaseName()).isEqualTo("hound_beta");
        // Connection is constructed with the override base URL — не проверяем HTTP,
        // но убеждаемся что ошибки не было и кэш заполнен.
        assertThat(registry.cachedAliases()).containsExactly("beta");
    }

    @Test
    void resourceFor_calledTwice_hitsCache_singleLookup() {
        stub.put(routing("acme", "ACTIVE", 1, "hound_acme", null));

        ArcadeConnection a1 = registry.resourceFor("acme");
        ArcadeConnection a2 = registry.resourceFor("acme");

        assertThat(a1).isSameAs(a2);
        assertThat(stub.calls.get()).isEqualTo(1);   // second call served from cache
    }

    // ── status gating ──────────────────────────────────────────────────────

    @Test
    void resourceFor_suspendedTenant_throwsSuspended() {
        stub.put(routing("acme", "SUSPENDED", 1, "hound_acme", null));

        assertThatThrownBy(() -> registry.resourceFor("acme"))
                .isInstanceOf(TenantNotAvailableException.class)
                .extracting("reason").isEqualTo(TenantNotAvailableException.Reason.SUSPENDED);
    }

    @Test
    void resourceFor_archivedTenant_throwsArchived() {
        stub.put(routing("acme", "ARCHIVED", 1, "hound_acme", null));

        assertThatThrownBy(() -> registry.resourceFor("acme"))
                .isInstanceOf(TenantNotAvailableException.class)
                .extracting("reason").isEqualTo(TenantNotAvailableException.Reason.ARCHIVED);
    }

    @Test
    void resourceFor_unknownTenant_throwsNotFound() {
        assertThatThrownBy(() -> registry.resourceFor("unknown"))
                .isInstanceOf(TenantNotAvailableException.class)
                .extracting("reason").isEqualTo(TenantNotAvailableException.Reason.NOT_FOUND);
    }

    @Test
    void resourceFor_nullAlias_throwsNotFound() {
        assertThatThrownBy(() -> registry.resourceFor(null))
                .isInstanceOf(TenantNotAvailableException.class);
    }

    // ── invalidation ───────────────────────────────────────────────────────

    @Test
    void invalidate_forcesReLookup() {
        stub.put(routing("acme", "ACTIVE", 1, "hound_acme", null));

        registry.resourceFor("acme");
        registry.resourceFor("acme");
        assertThat(stub.calls.get()).isEqualTo(1);

        registry.invalidate("acme");
        registry.resourceFor("acme");
        assertThat(stub.calls.get()).isEqualTo(2);
    }

    @Test
    void invalidateAll_clearsAllEntries() {
        stub.put(routing("a", "ACTIVE", 1, "hound_a", null));
        stub.put(routing("b", "ACTIVE", 1, "hound_b", null));
        registry.resourceFor("a");
        registry.resourceFor("b");
        assertThat(registry.cachedAliases()).containsExactlyInAnyOrder("a", "b");

        registry.invalidateAll();
        assertThat(registry.cachedAliases()).isEmpty();
    }

    // ── LRU eviction ──────────────────────────────────────────────────────

    @Test
    void cache_exceedsMaxEntries_evictsLru() {
        stub.put(routing("a", "ACTIVE", 1, "hound_a", null));
        stub.put(routing("b", "ACTIVE", 1, "hound_b", null));
        stub.put(routing("c", "ACTIVE", 1, "hound_c", null));
        stub.put(routing("d", "ACTIVE", 1, "hound_d", null));

        registry.resourceFor("a");
        registry.resourceFor("b");
        registry.resourceFor("c");
        // Touch "a" to make it recently-used; then add "d" → "b" (oldest) evicted
        registry.resourceFor("a");
        registry.resourceFor("d");

        assertThat(registry.cachedAliases())
                .containsExactlyInAnyOrder("a", "c", "d")
                .doesNotContain("b");
    }

    // ── multi-tenant isolation ─────────────────────────────────────────────

    @Test
    void twoTenants_getDistinctConnections() {
        stub.put(routing("acme", "ACTIVE", 1, "hound_acme", null));
        stub.put(routing("beta", "ACTIVE", 1, "hound_beta", null));

        ArcadeConnection a = registry.resourceFor("acme");
        ArcadeConnection b = registry.resourceFor("beta");

        assertThat(a.databaseName()).isEqualTo("hound_acme");
        assertThat(b.databaseName()).isEqualTo("hound_beta");
        assertThat(a).isNotSameAs(b);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static TenantRouting routing(String alias, String status, int version,
                                          String db, String yggUrl) {
        return new TenantRouting(
                alias, status, version,
                db, "hound_src_" + alias, "dali_" + alias,
                yggUrl, "kc-" + alias,
                null /* MTN-48 connectionCap */);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** Stub for {@link FriggTenantLookup} that counts lookup invocations. */
    private static final class StubLookup extends FriggTenantLookup {
        private final java.util.Map<String, TenantRouting> db = new java.util.HashMap<>();
        final AtomicInteger calls = new AtomicInteger();

        StubLookup() {
            super(java.net.http.HttpClient.newHttpClient(),
                  "http://nowhere", "u", "p");
        }
        void put(TenantRouting r) { db.put(r.tenantAlias(), r); }

        @Override
        public Optional<TenantRouting> lookup(String tenantAlias) {
            calls.incrementAndGet();
            if (tenantAlias == null) {
                throw new TenantNotAvailableException("(null)",
                        TenantNotAvailableException.Reason.NOT_FOUND);
            }
            var r = db.get(tenantAlias);
            return r != null ? Optional.of(r) : Optional.empty();
        }
    }
}
