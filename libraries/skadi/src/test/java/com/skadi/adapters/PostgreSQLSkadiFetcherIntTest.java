package com.skadi.adapters;

import com.skadi.SkadiFetchConfig;
import com.skadi.SkadiFetchException;
import com.skadi.SkadiFetchResult;
import com.skadi.SkadiFetchedFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link PostgreSQLSkadiFetcher} using Testcontainers.
 *
 * <p>Spins up a {@code postgres:16-alpine} container, seeds 3 functions + 2 views
 * from {@code sql/postgres/init/seed.sql}, and verifies SKADI fetch behaviour.
 *
 * <p>These tests require Docker. CI runs them unconditionally; locally they are skipped
 * automatically by Testcontainers if no Docker daemon is available.
 */
@Testcontainers(disabledWithoutDocker = true)
class PostgreSQLSkadiFetcherIntTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withInitScript("sql/postgres/init/seed.sql");

    static SkadiFetchConfig config;
    static PostgreSQLSkadiFetcher fetcher;

    @BeforeAll
    static void setUp() {
        config = SkadiFetchConfig.fullHarvest(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword(),
                "public");
        fetcher = new PostgreSQLSkadiFetcher();
    }

    @AfterAll
    static void tearDown() {
        fetcher.close();
    }

    // ── T1: connectivity ──────────────────────────────────────────────────────

    @Test
    void ping_returnsTrue() {
        assertThat(fetcher.ping(config)).isTrue();
    }

    // ── T2: functions ─────────────────────────────────────────────────────────

    @Test
    void fetchScripts_returnsFunctions() throws SkadiFetchException {
        SkadiFetchResult result = fetcher.fetchScripts(config);

        List<String> functionNames = result.files().stream()
                .filter(f -> f.objectType() == SkadiFetchConfig.ObjectType.FUNCTION)
                .map(SkadiFetchedFile::name)
                .toList();

        assertThat(functionNames)
                .as("seed.sql declares fn_add, fn_subtract, fn_multiply")
                .contains("fn_add", "fn_subtract", "fn_multiply");
    }

    // ── T3: views ─────────────────────────────────────────────────────────────

    @Test
    void fetchScripts_returnsViews() throws SkadiFetchException {
        SkadiFetchResult result = fetcher.fetchScripts(config);

        List<String> viewNames = result.files().stream()
                .filter(f -> f.objectType() == SkadiFetchConfig.ObjectType.VIEW)
                .map(SkadiFetchedFile::name)
                .toList();

        assertThat(viewNames)
                .as("seed.sql declares v_sum, v_product")
                .contains("v_sum", "v_product");
    }

    // ── T4: SqlSource conversion pattern (SI-03 contract) ─────────────────────

    @Test
    void fetchedFiles_haveNonBlankSqlTextSuitableForParsing() throws SkadiFetchException {
        SkadiFetchResult result = fetcher.fetchScripts(config);

        assertThat(result.files())
                .as("at least 5 objects expected (3 functions + 2 views)")
                .hasSizeGreaterThanOrEqualTo(5);

        for (SkadiFetchedFile f : result.files()) {
            assertThat(f.sqlText())
                    .as("DDL text for %s should not be blank", f.name())
                    .isNotBlank();
            assertThat(f.suggestedFilename())
                    .as("suggested filename for %s should follow naming convention", f.name())
                    .matches("[a-z0-9_]+__[a-z0-9_]+\\.(function|procedure|view|trigger|table)\\.sql");
        }

        // Demonstrate the SqlSource.FromText conversion pattern used in SourceArchiveService.upsertAll():
        // Each file maps to (sqlText, suggestedFilename) — the exact arguments for SqlSource.FromText.
        List<String> sourceNames = result.files().stream()
                .map(SkadiFetchedFile::suggestedFilename)
                .toList();
        assertThat(sourceNames).allMatch(name -> name.endsWith(".sql"));
    }

    // ── T5: stats ─────────────────────────────────────────────────────────────

    @Test
    void fetchStats_hasCorrectAdapterName() throws SkadiFetchException {
        SkadiFetchResult result = fetcher.fetchScripts(config);

        assertThat(result.stats().sourceAdapter()).isEqualTo("postgresql");
        assertThat(result.stats().durationMs()).isGreaterThan(0L);
        assertThat(result.stats().totalFetched()).isEqualTo(result.files().size());
        assertThat(result.stats().errors()).isZero();
    }
}
