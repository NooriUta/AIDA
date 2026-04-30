package studio.seer.heimdall.snapshot;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SnapshotManager — save/list with FriggGateway mocked.
 */
@QuarkusTest
class SnapshotManagerTest {

    @InjectMock FriggGateway frigg;
    @Inject     SnapshotManager manager;

    @BeforeEach
    void setUp() {
        // Default: all FRIGG calls succeed with empty result
        when(frigg.sql(anyString(), isNull()))
                .thenReturn(Uni.createFrom().item(List.of()));
        when(frigg.sql(anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));
    }

    // ── save() ────────────────────────────────────────────────────────────────

    @Test
    void save_validNameAndEvents_returnsSnapshotId() {
        List<HeimdallEvent> events = List.of(sampleEvent());

        String id = manager.save("my-snap", events).await().indefinitely();

        assertNotNull(id);
        assertFalse(id.isBlank());
        // ID should be a UUID-like string
        assertTrue(id.length() > 10);
    }

    @Test
    void save_nullName_usesUnnamedFallback() {
        String id = manager.save(null, List.of()).await().indefinitely();

        assertNotNull(id);
        // INSERT INTO HeimdallSnapshot was called
        verify(frigg, atLeast(1)).sql(contains("INSERT INTO HeimdallSnapshot"), anyMap());
    }

    @Test
    void save_blankName_usesUnnamedFallback() {
        String id = manager.save("   ", List.of()).await().indefinitely();
        assertNotNull(id);
    }

    @Test
    void save_emptyEventList_persistsZeroCount() {
        manager.save("empty-snap", List.of()).await().indefinitely();

        verify(frigg, atLeast(1)).sql(contains("INSERT INTO HeimdallSnapshot"),
                argThat(p -> p.containsKey("count") && Integer.valueOf(0).equals(p.get("count"))));
    }

    @Test
    void save_friggFailure_propagatesError() {
        when(frigg.sql(contains("INSERT INTO HeimdallSnapshot"), anyMap()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("FRIGG down")));

        assertThrows(RuntimeException.class,
                () -> manager.save("fail-snap", List.of()).await().indefinitely());
    }

    // ── list() ────────────────────────────────────────────────────────────────

    @Test
    void list_emptyResult_returnsEmptyList() {
        when(frigg.sql(contains("SELECT snapshotId"), isNull()))
                .thenReturn(Uni.createFrom().item(List.of()));

        List<SnapshotInfo> result = manager.list().await().indefinitely();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void list_withRows_mapsToSnapshotInfo() {
        var row = Map.<String, Object>of(
                "snapshotId", "abc-123",
                "name",       "my-snap",
                "ts",         1700000000000L,
                "eventCount", 42);

        when(frigg.sql(contains("SELECT snapshotId"), isNull()))
                .thenReturn(Uni.createFrom().item(List.of(row)));

        List<SnapshotInfo> result = manager.list().await().indefinitely();

        assertEquals(1, result.size());
        assertEquals("abc-123", result.get(0).id());
        assertEquals("my-snap", result.get(0).name());
        assertEquals(42, result.get(0).eventCount());
    }

    @Test
    void list_rowWithNullNumericFields_defaultsToZero() {
        var row = Map.<String, Object>of("snapshotId", "x", "name", "y");

        when(frigg.sql(contains("SELECT snapshotId"), isNull()))
                .thenReturn(Uni.createFrom().item(List.of(row)));

        List<SnapshotInfo> result = manager.list().await().indefinitely();

        assertEquals(1, result.size());
        assertEquals(0L, result.get(0).timestamp());
        assertEquals(0, result.get(0).eventCount());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static HeimdallEvent sampleEvent() {
        return new HeimdallEvent(
                System.currentTimeMillis(), "test", "TEST_EVENT",
                EventLevel.INFO, null, null, null, 0, Map.of());
    }
}
