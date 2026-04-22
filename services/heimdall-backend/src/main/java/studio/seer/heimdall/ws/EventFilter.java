package studio.seer.heimdall.ws;

import org.jboss.logging.Logger;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

/**
 * WebSocket stream filter. All non-null fields apply AND-logic.
 *
 * Supported format: comma-separated key:value pairs
 *   component:mimir,level:WARN  → sourceComponent == "mimir" AND level >= WARN
 *   session_id:abc123           → sessionId == "abc123"
 *   type:ATOM_EXTRACTED         → eventType == "ATOM_EXTRACTED"
 *   tenant:acme                 → payload.tenantAlias == "acme"  (MTN-05)
 *
 * <p>MTN-05: non-superadmin sessions have {@code tenant} forced to their
 * own org by {@link EventStreamEndpoint#onOpen}. Clients cannot override it.
 */
public record EventFilter(
        String sessionId, String component, EventLevel minLevel, String eventType,
        String tenantAlias) {

    private static final Logger LOG = Logger.getLogger(EventFilter.class);

    public static EventFilter empty() {
        return new EventFilter(null, null, null, null, null);
    }

    public static EventFilter parse(String raw) {
        if (raw == null || raw.isBlank()) return empty();

        String sessionId  = null;
        String component  = null;
        EventLevel minLvl = null;
        String eventType  = null;
        String tenantAls  = null;

        for (String pair : raw.split(",")) {
            int colon = pair.indexOf(':');
            if (colon <= 0 || colon == pair.length() - 1) {
                LOG.warnf("EventFilter: malformed pair '%s'", pair);
                continue;
            }
            String key   = pair.substring(0, colon).trim().toLowerCase();
            String value = pair.substring(colon + 1).trim();
            switch (key) {
                case "component"  -> component = value;
                case "session_id" -> sessionId = value;
                case "type"       -> eventType = value.toUpperCase();
                case "tenant"     -> tenantAls = value;
                case "level"      -> {
                    try { minLvl = EventLevel.valueOf(value.toUpperCase()); }
                    catch (IllegalArgumentException e) { LOG.warnf("EventFilter: unknown level '%s'", value); }
                }
                default -> LOG.warnf("EventFilter: unknown key '%s'", key);
            }
        }
        return new EventFilter(sessionId, component, minLvl, eventType, tenantAls);
    }

    /** MTN-05: return a copy with {@code tenantAlias} forced (non-superadmin sessions). */
    public EventFilter withForcedTenantAlias(String alias) {
        return new EventFilter(sessionId, component, minLevel, eventType, alias);
    }

    public boolean matches(HeimdallEvent e) {
        if (sessionId != null && !sessionId.equals(e.sessionId()))       return false;
        if (component  != null && !component.equals(e.sourceComponent())) return false;
        if (eventType  != null && !eventType.equals(e.eventType()))       return false;
        if (minLevel   != null) {
            EventLevel lv = e.level();
            if (lv == null || lv.ordinal() < minLevel.ordinal())         return false;
        }
        if (tenantAlias != null) {
            Object t = e.payload() == null ? null : e.payload().get("tenantAlias");
            if (t == null || !tenantAlias.equals(String.valueOf(t)))     return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return sessionId == null && component == null && minLevel == null
                && eventType == null && tenantAlias == null;
    }
}
