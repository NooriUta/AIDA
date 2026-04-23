package studio.seer.shared;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ControlEventFenceTest {

    @Test
    void read_returnsZero_whenKeyAbsent() {
        assertEquals(0L, ControlEventFence.read(Map.of()));
        assertEquals(0L, ControlEventFence.read(null));
        assertEquals(0L, ControlEventFence.read(Map.of("action", "tenant_invalidated")));
    }

    @Test
    void read_returnsExplicitToken() {
        assertEquals(42L, ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, 42L)));
        assertEquals(7L,  ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, 7)));
        assertEquals(99L, ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, "99")));
    }

    @Test
    void read_rejectsMalformedToken() {
        assertThrows(IllegalArgumentException.class,
                () -> ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, 0L)));
        assertThrows(IllegalArgumentException.class,
                () -> ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, -5L)));
        assertThrows(IllegalArgumentException.class,
                () -> ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, 1.5)));
        assertThrows(IllegalArgumentException.class,
                () -> ControlEventFence.read(Map.of(ControlEventFence.FENCE_TOKEN_KEY, "nope")));
    }

    @Test
    void stamp_addsTokenAndPreservesKeys() {
        Map<String, Object> base = Map.of("action", "tenant_invalidated", "tenantAlias", "acme");
        Map<String, Object> out = ControlEventFence.stamp(base, 100L);
        assertEquals(100L, out.get(ControlEventFence.FENCE_TOKEN_KEY));
        assertEquals("tenant_invalidated", out.get("action"));
        assertEquals("acme", out.get("tenantAlias"));
    }

    @Test
    void stamp_rejectsNonPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> ControlEventFence.stamp(Map.of(), 0));
        assertThrows(IllegalArgumentException.class,
                () -> ControlEventFence.stamp(Map.of(), -1));
    }

    @Test
    void stamp_compatibleWithSchemaVersion_bothCoexistInSamePayload() {
        // Common emit pattern: MTN-50 + MTN-52 together.
        Map<String, Object> base = Map.of("action", "tenant_invalidated", "tenantAlias", "acme");
        Map<String, Object> versioned = SchemaVersion.withCurrentSchemaVersion(base);
        Map<String, Object> stamped   = ControlEventFence.stamp(versioned, 42L);

        assertEquals(SchemaVersion.CURRENT, stamped.get(SchemaVersion.SCHEMA_VERSION_KEY));
        assertEquals(42L,                   stamped.get(ControlEventFence.FENCE_TOKEN_KEY));
        assertEquals("acme",                stamped.get("tenantAlias"));
    }

    @Test
    void isStale_returnsFalseForAbsentFence_legacyEmitter() {
        // Received token 0 means emitter pre-dates MTN-50 — don't reject.
        assertFalse(ControlEventFence.isStale(0L, 100L));
        assertFalse(ControlEventFence.isStale(0L, 0L));
    }

    @Test
    void isStale_detectsStaleToken() {
        assertTrue(ControlEventFence.isStale(50L, 100L));
    }

    @Test
    void isStale_rejectsEqualAsNotStale_sinceCallerDecidesDuplicateSemantic() {
        // Equal tokens are duplicates (replay); caller treats differently from stale.
        // isStale() returns false — caller checks `received == lastSeen` separately.
        assertFalse(ControlEventFence.isStale(100L, 100L));
    }

    @Test
    void isStale_returnsFalseWhenMonotonicallyAdvancing() {
        assertFalse(ControlEventFence.isStale(101L, 100L));
        assertFalse(ControlEventFence.isStale(Long.MAX_VALUE, 100L));
    }

    @Test
    void without_removesKey() {
        Map<String, Object> in = Map.of("action", "X", ControlEventFence.FENCE_TOKEN_KEY, 7L);
        Map<String, Object> out = ControlEventFence.without(in);
        assertFalse(out.containsKey(ControlEventFence.FENCE_TOKEN_KEY));
        assertEquals("X", out.get("action"));
    }

    @Test
    void without_handlesNull() {
        Map<String, Object> out = ControlEventFence.without(null);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }
}
