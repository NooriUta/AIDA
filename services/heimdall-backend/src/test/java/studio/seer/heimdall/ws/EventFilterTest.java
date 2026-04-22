package studio.seer.heimdall.ws;

import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MTN-05 — Tests for the tenant-aware EventFilter (dart-of-the-suite: can a
 * non-superadmin client ever receive an event tagged with another tenant?).
 */
class EventFilterTest {

    private static HeimdallEvent event(String tenantAlias, String component) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                component,
                "TEST_EVENT",
                EventLevel.INFO,
                null, null, null, 0L,
                tenantAlias == null ? Map.of() : Map.of("tenantAlias", tenantAlias)
        );
    }

    @Test
    void empty_matchesEverything() {
        assertTrue(EventFilter.empty().matches(event("acme", "shuttle")));
        assertTrue(EventFilter.empty().matches(event(null,  "shuttle")));
    }

    @Test
    void parse_supportsTenantKey() {
        EventFilter f = EventFilter.parse("tenant:acme");
        assertEquals("acme", f.tenantAlias());
    }

    @Test
    void parse_combinesTenantAndComponent() {
        EventFilter f = EventFilter.parse("tenant:acme,component:shuttle");
        assertEquals("acme",    f.tenantAlias());
        assertEquals("shuttle", f.component());
    }

    @Test
    void matches_tenantFilterDropsOtherTenants() {
        EventFilter f = EventFilter.parse("tenant:acme");
        assertTrue (f.matches(event("acme", "shuttle")));
        assertFalse(f.matches(event("beta", "shuttle")));
    }

    @Test
    void matches_tenantFilterDropsEventsWithoutTenantAlias() {
        EventFilter f = EventFilter.parse("tenant:acme");
        assertFalse(f.matches(event(null, "shuttle")));
    }

    @Test
    void withForcedTenantAlias_replacesAnyClientProvidedValue() {
        // Client asked for tenant:beta; server forces acme based on session JWT.
        EventFilter f = EventFilter.parse("tenant:beta").withForcedTenantAlias("acme");
        assertEquals("acme", f.tenantAlias());
        assertTrue (f.matches(event("acme", "shuttle")));
        assertFalse(f.matches(event("beta", "shuttle")));
    }

    @Test
    void withForcedTenantAlias_keepsOtherFieldsIntact() {
        EventFilter f = EventFilter.parse("component:shuttle,level:WARN")
                .withForcedTenantAlias("acme");
        assertEquals("shuttle", f.component());
        assertEquals(EventLevel.WARN, f.minLevel());
        assertEquals("acme", f.tenantAlias());
    }

    @Test
    void matches_combinesTenantAndOtherFilters() {
        EventFilter f = EventFilter.parse("tenant:acme,component:shuttle");
        assertTrue (f.matches(event("acme", "shuttle")));
        assertFalse(f.matches(event("acme", "dali")));     // wrong component
        assertFalse(f.matches(event("beta", "shuttle")));  // wrong tenant
    }

    @Test
    void isEmpty_onlyWhenAllFieldsNull() {
        assertTrue(EventFilter.empty().isEmpty());
        assertFalse(EventFilter.parse("tenant:acme").isEmpty());
    }
}
