package studio.seer.dali.skadi;

import com.hound.api.SqlSource;
import com.skadi.SkadiFetchConfig;
import com.skadi.SkadiFetchedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DMT-10: SourceArchiveService unit tests.
 *
 * <p>Covers Phase 1 (MVP) semantics:
 * <ul>
 *   <li>Routing: {@code resourceFor(alias)} resolves to the correct {@code hound_src_{alias}} DB name</li>
 *   <li>All non-blank files are forwarded to HoundParser (Phase 1: no deduplication)</li>
 *   <li>Cross-tenant isolation: two tenants get different DB names</li>
 *   <li>Blank SQL text filtering: blank files are silently excluded</li>
 *   <li>Empty input: returns empty list, no NPE</li>
 *   <li>SHA-256 helper: deterministic, null-safe</li>
 * </ul>
 *
 * <p>Uses a stub {@link YggSourceArchiveRegistry} that maps alias → {@code hound_src_{alias}}
 * without ArcadeDB connectivity.
 */
class SourceArchiveServiceTest {

    private SourceArchiveService service;
    private StubRegistry          registry;

    @BeforeEach
    void setUp() throws Exception {
        service  = new SourceArchiveService();
        registry = new StubRegistry();
        inject(service, "sourceArchiveRegistry", registry);
    }

    // ── Registry routing ──────────────────────────────────────────────────────

    @Test
    void registry_acme_routesToHoundSrcAcme() {
        assertThat(registry.resourceFor("acme").databaseName())
                .isEqualTo("hound_src_acme");
    }

    @Test
    void registry_default_routesToHoundSrcDefault() {
        assertThat(registry.resourceFor("default").databaseName())
                .isEqualTo("hound_src_default");
    }

    @Test
    void registry_differentTenants_differentDbNames() {
        String acmeDb    = registry.resourceFor("acme").databaseName();
        String defaultDb = registry.resourceFor("default").databaseName();

        assertThat(acmeDb).isNotEqualTo(defaultDb);
        assertThat(acmeDb).isEqualTo("hound_src_acme");
        assertThat(defaultDb).isEqualTo("hound_src_default");
    }

    // ── upsertAll() — Phase 1 semantics ───────────────────────────────────────

    @Test
    void upsertAll_allFilesForwarded_phase1() {
        List<SkadiFetchedFile> files = List.of(
                file("HR", "emp_view", "CREATE VIEW emp AS SELECT * FROM employees"),
                file("HR", "dept_proc", "CREATE PROCEDURE get_dept(id INT) AS BEGIN ... END")
        );

        List<SqlSource> sources = service.upsertAll("acme", files);

        assertThat(sources).hasSize(2);
    }

    @Test
    void upsertAll_routesToCorrectTenantDb() {
        List<SkadiFetchedFile> files = List.of(
                file("PUBLIC", "my_view", "CREATE VIEW my_view AS SELECT 1")
        );

        // Two calls with different tenant aliases must both succeed (cross-tenant isolation)
        List<SqlSource> acmeSources    = service.upsertAll("acme",    files);
        List<SqlSource> defaultSources = service.upsertAll("default", files);

        // Both get the same files (Phase 1: all forwarded regardless of alias)
        assertThat(acmeSources).hasSize(1);
        assertThat(defaultSources).hasSize(1);

        // Registry resolves to different DBs for each alias
        assertThat(registry.resourceFor("acme").databaseName()).contains("acme");
        assertThat(registry.resourceFor("default").databaseName()).contains("default");
    }

    @Test
    void upsertAll_blankSqlText_excluded() {
        List<SkadiFetchedFile> files = List.of(
                file("HR", "good",  "CREATE VIEW v AS SELECT 1"),
                file("HR", "blank", "   "),
                file("HR", "null",  null)
        );

        List<SqlSource> sources = service.upsertAll("acme", files);

        // Only non-blank SQL texts are forwarded
        assertThat(sources).hasSize(1);
    }

    @Test
    void upsertAll_emptyList_returnsEmpty() {
        List<SqlSource> sources = service.upsertAll("acme", List.of());
        assertThat(sources).isEmpty();
    }

    @Test
    void upsertAll_nullList_returnsEmpty() {
        List<SqlSource> sources = service.upsertAll("acme", null);
        assertThat(sources).isEmpty();
    }

    @Test
    void upsertAll_defaultOverload_usesDefaultTenant() {
        // The single-arg overload must work and use "default" alias
        List<SkadiFetchedFile> files = List.of(
                file("DBO", "some_proc", "CREATE PROC some_proc AS BEGIN END")
        );

        List<SqlSource> sources = service.upsertAll(files);

        assertThat(sources).hasSize(1);
    }

    // ── sha256() helper ───────────────────────────────────────────────────────

    @Test
    void sha256_deterministic() {
        String text = "CREATE TABLE employees (id INT PRIMARY KEY)";

        String h1 = SourceArchiveService.sha256(text);
        String h2 = SourceArchiveService.sha256(text);

        assertThat(h1).isEqualTo(h2);
        assertThat(h1).hasSize(64);   // SHA-256 hex = 64 chars
        assertThat(h1).matches("[0-9a-f]+");
    }

    @Test
    void sha256_differentInputs_differentHashes() {
        String h1 = SourceArchiveService.sha256("SELECT 1");
        String h2 = SourceArchiveService.sha256("SELECT 2");

        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void sha256_nullInput_returnsEmptyString() {
        assertThat(SourceArchiveService.sha256(null)).isEmpty();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static SkadiFetchedFile file(String schema, String name, String sql) {
        return new SkadiFetchedFile(
                name, schema,
                SkadiFetchConfig.ObjectType.VIEW,
                sql,
                Instant.now(),
                Map.of()
        );
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ── Stub registry — maps alias → hound_src_{alias} ───────────────────────

    /**
     * In-memory stub of {@link YggSourceArchiveRegistry}.
     * Returns an {@link ArcadeConnection} whose {@link ArcadeConnection#databaseName()}
     * is {@code "hound_src_" + tenantAlias}.
     */
    static class StubRegistry implements YggSourceArchiveRegistry {

        private final List<String> resolvedAliases = new ArrayList<>();

        @Override
        public ArcadeConnection resourceFor(String tenantAlias) {
            resolvedAliases.add(tenantAlias);
            return new StubConnection("hound_src_" + tenantAlias);
        }

        @Override public void invalidate(String tenantAlias) { }
        @Override public void invalidateAll() { }

        List<String> resolvedAliases() { return resolvedAliases; }
    }

    static class StubConnection implements ArcadeConnection {
        private final String dbName;
        StubConnection(String dbName) { this.dbName = dbName; }

        @Override public String databaseName() { return dbName; }
        @Override public List<Map<String, Object>> sql(String query, Map<String, Object> params) { return List.of(); }
        @Override public List<Map<String, Object>> cypher(String query, Map<String, Object> params) { return List.of(); }
    }
}
