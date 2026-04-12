package studio.seer.heimdall.ws;

import org.jboss.logging.Logger;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

/**
 * WebSocket stream filter. All non-null fields apply AND-logic.
 *
 * Supported formats (one key:value pair per connection in Sprint 2):
 *   component:mimir     → sourceComponent == "mimir"
 *   level:ERROR         → level.ordinal >= ERROR.ordinal
 *   session_id:abc123   → sessionId == "abc123"
 *   type:ATOM_EXTRACTED → eventType == "ATOM_EXTRACTED"
 *
 * Multi-filter (AND of multiple pairs) deferred to Sprint 3.
 */
public record EventFilter(String sessionId, String component, EventLevel minLevel, String eventType) {

    private static final Logger LOG = Logger.getLogger(EventFilter.class);

    public static EventFilter empty() {
        return new EventFilter(null, null, null, null);
    }

    public static EventFilter parse(String raw) {
        if (raw == null || raw.isBlank()) return empty();
        int colon = raw.indexOf(':');
        if (colon <= 0 || colon == raw.length() - 1) {
            LOG.warnf("EventFilter: malformed filter string '%s'", raw);
            return empty();
        }
        String key   = raw.substring(0, colon).trim().toLowerCase();
        String value = raw.substring(colon + 1).trim();

        return switch (key) {
            case "component"  -> new EventFilter(null, value, null, null);
            case "session_id" -> new EventFilter(value, null, null, null);
            case "type"       -> new EventFilter(null, null, null, value.toUpperCase());
            case "level"      -> {
                try {
                    yield new EventFilter(null, null, EventLevel.valueOf(value.toUpperCase()), null);
                } catch (IllegalArgumentException e) {
                    LOG.warnf("EventFilter: unknown level value '%s'", value);
                    yield empty();
                }
            }
            default -> {
                LOG.warnf("EventFilter: unknown filter key '%s'", key);
                yield empty();
            }
        };
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
