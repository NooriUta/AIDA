package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import studio.seer.heimdall.RingBuffer;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.shared.HeimdallEvent;

import java.util.List;

/**
 * HTTP endpoint для приёма событий от всех эмиттеров (Hound, Dali, SHUTTLE, etc.).
 *
 * Семантика: fire-and-forget, 202 Accepted.
 * Падение HEIMDALL не должно блокировать эмиттеров — они шлют POST и забывают.
 *
 * Эндпоинты:
 *   POST /events        — одно событие
 *   POST /events/batch  — массив событий (для batch-эмиттеров типа Hound)
 */
@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    private static final Logger LOG = Logger.getLogger(EventResource.class);

    @Inject
    RingBuffer ringBuffer;

    @Inject
    MetricsCollector metricsCollector;

    @POST
    public Response ingest(HeimdallEvent event) {
        if (event == null || event.sourceComponent() == null || event.eventType() == null) {
            return Response.status(400).entity("{\"error\":\"sourceComponent and eventType are required\"}").build();
        }

        // Обогащаем timestamp если эмиттер не передал (или передал 0)
        HeimdallEvent enriched = event.timestamp() > 0 ? event
                : new HeimdallEvent(
                        System.currentTimeMillis(),
                        event.sourceComponent(),
                        event.eventType(),
                        event.level(),
                        event.sessionId(),
                        event.userId(),
                        event.correlationId(),
                        event.durationMs(),
                        event.payload());

        ringBuffer.push(enriched);
        metricsCollector.record(enriched);
        LOG.debugf("Ingested event: %s from %s", enriched.eventType(), enriched.sourceComponent());
        return Response.accepted().build();
    }

    @POST
    @Path("/batch")
    public Response ingestBatch(List<HeimdallEvent> events) {
        if (events == null) {
            return Response.status(400).entity("{\"error\":\"events array is required\"}").build();
        }

        long count = events.stream()
                .filter(e -> e != null && e.sourceComponent() != null && e.eventType() != null)
                .peek(e -> { ringBuffer.push(e); metricsCollector.record(e); })
                .count();

        LOG.debugf("Ingested batch of %d events", count);
        return Response.accepted().build();
    }
}
