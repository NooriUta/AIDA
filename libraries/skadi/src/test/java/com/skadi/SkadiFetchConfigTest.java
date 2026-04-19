package com.skadi;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SkadiFetchConfigTest {

    @Test
    void fullHarvest_hasAllObjectTypes() {
        var cfg = SkadiFetchConfig.fullHarvest(
                "jdbc:postgresql://localhost/test", "user", "pass", "public");
        assertThat(cfg.objectTypes())
                .containsExactlyInAnyOrder(SkadiFetchConfig.ObjectType.values());
        assertNull(cfg.modifiedSince());
        assertEquals(0, cfg.limit());
    }

    @Test
    void fullHarvest_modifiedSinceIsNull() {
        var cfg = SkadiFetchConfig.fullHarvest("url", "u", "p", "s");
        assertNull(cfg.modifiedSince());
    }

    @Test
    void incrementalHarvest_hasModifiedSince() {
        Instant since = Instant.parse("2026-01-01T00:00:00Z");
        var cfg = SkadiFetchConfig.incrementalHarvest("url", "u", "p", "s", since);
        assertEquals(since, cfg.modifiedSince());
        assertThat(cfg.objectTypes())
                .containsExactlyInAnyOrder(SkadiFetchConfig.ObjectType.values());
    }

    @Test
    void selectiveHarvest_hasOnlyRequestedTypes() {
        var cfg = SkadiFetchConfig.selectiveHarvest("url", "u", "p", "s",
                SkadiFetchConfig.ObjectType.FUNCTION,
                SkadiFetchConfig.ObjectType.PROCEDURE);
        assertThat(cfg.objectTypes())
                .containsOnly(SkadiFetchConfig.ObjectType.FUNCTION,
                               SkadiFetchConfig.ObjectType.PROCEDURE);
    }

    @Test
    void toSafeString_doesNotContainPassword() {
        var cfg = SkadiFetchConfig.fullHarvest("url", "user", "s3cr3t", "schema");
        assertThat(cfg.toSafeString()).doesNotContain("s3cr3t");
        assertThat(cfg.toSafeString()).contains("user");
        assertThat(cfg.toSafeString()).contains("schema");
    }
}
