package studio.seer.heimdall.control;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.snapshot.FriggGateway;
import studio.seer.shared.ControlEvent;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ControlEventStore — persist() branches: success, idempotent duplicate, fatal error.
 */
@QuarkusTest
class ControlEventStoreTest {

    @InjectMock FriggGateway frigg;
    @Inject     ControlEventStore store;

    private static final ControlEvent SAMPLE = ControlEvent.newEvent(
            "acme", "tenant_invalidated", 1L, 1, Map.of("cause", "test"));

    // ── persist() ─────────────────────────────────────────────────────────────

    @Test
    void persist_success_returnsTrue() {
        when(frigg.sql(anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(Map.of("@rid", "#1:1"))));

        boolean result = store.persist(SAMPLE);

        assertTrue(result);
        verify(frigg).sql(contains("INSERT INTO ControlEvent"), anyMap());
    }

    @Test
    void persist_duplicateKey_returnsFalse() {
        // ArcadeDB reports UNIQUE violations as a runtime exception with "duplicate" in message
        when(frigg.sql(anyString(), anyMap()))
                .thenReturn(Uni.createFrom().failure(
                        new RuntimeException("Cannot execute command: duplicate key on index")));

        boolean result = store.persist(SAMPLE);

        assertFalse(result);
    }

    @Test
    void persist_uniqueConstraintViaIndexMsg_returnsFalse() {
        when(frigg.sql(anyString(), anyMap()))
                .thenReturn(Uni.createFrom().failure(
                        new RuntimeException("index already has this entry")));

        boolean result = store.persist(SAMPLE);

        assertFalse(result);
    }

    @Test
    void persist_fatalDbError_throwsRuntimeException() {
        when(frigg.sql(anyString(), anyMap()))
                .thenReturn(Uni.createFrom().failure(
                        new RuntimeException("connection refused")));

        assertThrows(RuntimeException.class, () -> store.persist(SAMPLE));
    }

    @Test
    void persist_includesAllRequiredFields() {
        when(frigg.sql(anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        store.persist(SAMPLE);

        verify(frigg).sql(anyString(), argThat(params ->
                params.containsKey("id")           &&
                params.containsKey("tenantAlias")  &&
                params.containsKey("eventType")    &&
                params.containsKey("fenceToken")   &&
                params.containsKey("schemaVersion")&&
                params.containsKey("createdAt")    &&
                params.containsKey("payload")));
    }
}
