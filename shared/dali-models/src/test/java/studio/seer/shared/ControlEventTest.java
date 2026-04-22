package studio.seer.shared;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ControlEventTest {

    @Test
    void newEvent_allocatesUuidAndTimestamp() {
        ControlEvent e = ControlEvent.newEvent("acme", "tenant_invalidated",
                42L, SchemaVersion.CURRENT, Map.of("cause", "reconnect"));
        assertNotNull(e.id());
        assertEquals(36, e.id().length()); // UUID v4 canonical length
        assertEquals("acme", e.tenantAlias());
        assertEquals("tenant_invalidated", e.eventType());
        assertEquals(42L, e.fenceToken());
        assertEquals(1, e.schemaVersion());
        assertTrue(e.createdAt() > 0);
        assertEquals("reconnect", e.payload().get("cause"));
    }

    @Test
    void newEvent_distinctIds() {
        ControlEvent a = ControlEvent.newEvent("acme", "X", 1L, 1, Map.of());
        ControlEvent b = ControlEvent.newEvent("acme", "X", 1L, 1, Map.of());
        assertNotEquals(a.id(), b.id());
    }

    @Test
    void constructor_rejectsBlankId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("", "acme", "X", 1L, 1, 1L, Map.of()));
    }

    @Test
    void constructor_rejectsBlankTenant() {
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("id", "", "X", 1L, 1, 1L, Map.of()));
    }

    @Test
    void constructor_rejectsBlankEventType() {
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("id", "acme", "", 1L, 1, 1L, Map.of()));
    }

    @Test
    void constructor_rejectsNonPositiveFence() {
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("id", "acme", "X", 0L, 1, 1L, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("id", "acme", "X", -1L, 1, 1L, Map.of()));
    }

    @Test
    void constructor_rejectsInvalidSchemaVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("id", "acme", "X", 1L, 0, 1L, Map.of()));
    }

    @Test
    void constructor_rejectsNullPayload() {
        assertThrows(IllegalArgumentException.class,
                () -> new ControlEvent("id", "acme", "X", 1L, 1, 1L, null));
    }
}
