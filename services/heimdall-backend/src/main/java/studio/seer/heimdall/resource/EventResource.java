package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import studio.seer.heimdall.RingBuffer;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.metrics.TenantMetricsService;
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

    @Inject
    TenantMetricsService tenantMetrics;

    @POST
    public Response ingest(@Valid @NotNull HeimdallEvent event) {

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
        tenantMetrics.record(enriched);
        LOG.debugf("Ingested event: %s from %s", enriched.eventType(), enriched.sourceComponent());
        return Response.accepted().build();
    }

    /** HB-batch-valid: max events in one batch. Prevents DoS via oversized payloads. */
    static final int BATCH_MAX_SIZE = 100;

    @POST
    @Path("/batch")
    public Response ingestBatch(@NotNull @Valid List<@Valid HeimdallEvent> events) {

        // HB-batch-valid: atomic ingest — reject whole batch on any malformed
        // item (null / missing required field). Previously the filter silently
        // dropped bad items, which broke correlation chains and let partial-
        // success masking happen.
        if (events.size() > BATCH_MAX_SIZE) {
            return Response.status(413)
                    .entity("{\"error\":\"batch_too_large\",\"max\":" + BATCH_MAX_SIZE +
                            ",\"received\":" + events.size() + "}")
                    .build();
        }
        for (int i = 0; i < events.size(); i++) {
            HeimdallEvent e = events.get(i);
            if (e == null || e.sourceComponent() == null || e.eventType() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"malformed_event_in_batch\",\"index\":" + i + "}")
                        .build();
            }
        }
        for (HeimdallEvent e : events) {
            ringBuffer.push(e);
            metricsCollector.record(e);
            tenantMetrics.record(e);
        }

        LOG.debugf("Ingested batch of %d events", events.size());
        return Response.accepted().build();
    }
}
