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
 */
public record EventFilter(String sessionId, String component, EventLevel minLevel, String eventType) {

    private static final Logger LOG = Logger.getLogger(EventFilter.class);

    public static EventFilter empty() {
        return new EventFilter(null, null, null, null);
    }

    public static EventFilter parse(String raw) {
        if (raw == null || raw.isBlank()) return empty();

        String sessionId  = null;
        String component  = null;
        EventLevel minLvl = null;
        String eventType  = null;

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
                case "level"      -> {
                    try { minLvl = EventLevel.valueOf(value.toUpperCase()); }
                    catch (IllegalArgumentException e) { LOG.warnf("EventFilter: unknown level '%s'", value); }
                }
                default -> LOG.warnf("EventFilter: unknown key '%s'", key);
            }
        }
        return new EventFilter(sessionId, component, minLvl, eventType);
    }

    public boolean matches(HeimdallEvent e) {
        if (sessionId != null && !sessionId.equals(e.sessionId()))       return false;
        if (component  != null && !component.equals(e.sourceComponent())) return false;
        if (eventType  != null && !eventType.equals(e.eventType()))       return false;
        if (minLevel   != null) {
            EventLevel lv = e.level();
            if (lv == null || lv.ordinal() < minLevel.ordinal())         return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return sessionId == null && component == null && minLevel == null && eventType == null;
    }
}
