package studio.seer.lineage.heimdall.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.graphql.Type;

/**
 * GraphQL output type for HeimdallEvent.
 * Converts Map<String,Object> payload → payloadJson (String) so that
 * SmallRye GraphQL can serialize it without a custom scalar.
 *
 * Used exclusively in SubscriptionResource — the REST path uses HeimdallEvent directly.
 */
@Type("HeimdallEvent")
public record HeimdallEventView(
        long   timestamp,
        String sourceComponent,
        String eventType,
        String level,          // EventLevel.name() — string for GQL simplicity
        String sessionId,
        String userId,
        String correlationId,
        long   durationMs,
        String payloadJson     // JSON-encoded payload
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static HeimdallEventView from(HeimdallEvent e) {
        String json;
        try {
            json = MAPPER.writeValueAsString(e.payload());
        } catch (JsonProcessingException ex) {
            json = "{}";
        }
        return new HeimdallEventView(
                e.timestamp(),
                e.sourceComponent(),
                e.eventType(),
                e.level() != null ? e.level().name() : "INFO",
                e.sessionId(),
                e.userId(),
                e.correlationId(),
                e.durationMs(),
                json
        );
    }
}
