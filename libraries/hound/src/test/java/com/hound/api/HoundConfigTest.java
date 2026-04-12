package com.hound.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HoundConfig} record — factory methods and compact constructor.
 */
class HoundConfigTest {

    @Test
    void defaultDisabled_hasCorrectDefaults() {
        HoundConfig cfg = HoundConfig.defaultDisabled("plsql");

        assertEquals("plsql", cfg.dialect());
        assertEquals(ArcadeWriteMode.DISABLED, cfg.writeMode());
        assertNull(cfg.arcadeUrl());
        assertNull(cfg.targetSchema());
        assertEquals("hound", cfg.arcadeDbName());
        assertEquals("root", cfg.arcadeUser());
        assertFalse(cfg.strictResolution());
        assertEquals(5000, cfg.batchSize());
        assertTrue(cfg.workerThreads() > 0, "workerThreads should be > 0");
    }

    @Test
    void defaultRemoteBatch_hasRemoteBatchMode() {
        HoundConfig cfg = HoundConfig.defaultRemoteBatch("plsql", "http://localhost:2480");

        assertEquals("plsql", cfg.dialect());
        assertEquals(ArcadeWriteMode.REMOTE_BATCH, cfg.writeMode());
        assertEquals("http://localhost:2480", cfg.arcadeUrl());
    }

    @Test
    void defaultRemote_hasRemoteMode() {
        HoundConfig cfg = HoundConfig.defaultRemote("plsql", "http://db-host:2480");

        assertEquals(ArcadeWriteMode.REMOTE, cfg.writeMode());
        assertEquals("http://db-host:2480", cfg.arcadeUrl());
    }

    @Test
    void withTargetSchema_returnsNewRecord() {
        HoundConfig cfg = HoundConfig.defaultDisabled("plsql").withTargetSchema("MY_APP");

        assertEquals("MY_APP", cfg.targetSchema());
        assertEquals("plsql", cfg.dialect()); // unchanged
    }

    @Test
    void withCredentials_updatesCredentials() {
        HoundConfig cfg = HoundConfig.defaultRemoteBatch("plsql", "http://localhost:2480")
                .withCredentials("my_db", "admin", "secret");

        assertEquals("my_db", cfg.arcadeDbName());
        assertEquals("admin", cfg.arcadeUser());
        assertEquals("secret", cfg.arcadePassword());
        assertEquals(ArcadeWriteMode.REMOTE_BATCH, cfg.writeMode()); // unchanged
    }

    @Test
    void withWorkerThreads_updatesThreadCount() {
        HoundConfig cfg = HoundConfig.defaultDisabled("plsql").withWorkerThreads(4);

        assertEquals(4, cfg.workerThreads());
    }

    @Test
    void nullExtra_isNormalisedToEmptyMap() {
        HoundConfig cfg = new HoundConfig("plsql", null, ArcadeWriteMode.DISABLED,
                null, "hound", "root", "pwd", 1, false, 5000, null);

        assertNotNull(cfg.extra());
        assertTrue(cfg.extra().isEmpty());
    }

    @Test
    void extraMap_isImmutable() {
        HoundConfig cfg = HoundConfig.defaultDisabled("plsql");

        assertThrows(UnsupportedOperationException.class,
                () -> cfg.extra().put("key", "value"));
    }

    @Test
    void extraMap_isDefensivelyCopied() {
        var mutable = new java.util.HashMap<>(Map.of("k", "v"));
        HoundConfig cfg = new HoundConfig("plsql", null, ArcadeWriteMode.DISABLED,
                null, "hound", "root", "pwd", 1, false, 5000, mutable);

        mutable.put("new", "entry");
        assertFalse(cfg.extra().containsKey("new"), "extra should be a defensive copy");
    }
}
