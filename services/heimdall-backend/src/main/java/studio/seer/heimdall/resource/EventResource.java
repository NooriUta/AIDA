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
import studio.seer.heimdall.analytics.UxAggregator;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.metrics.TenantMetricsService;
import studio.seer.shared.HeimdallEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Inject
    UxAggregator uxAggregator;

    /**
     * HTA-14: Enforce tenant-tagging.
     *
     * Every event except the following must carry {@code tenantAlias} in its payload:
     * <ul>
     *   <li>{@code seer.platform.*} and {@code seer.audit.*} — system-level events</li>
     *   <li>Events from internal background components (hound, dali, shuttle) — these
     *       run single-tenant jobs; tenant context is captured via {@code sessionId}.
     *       They cannot include {@code tenantAlias} without a large refactor of the
     *       listener chain (DaliHoundListener has no CDI tenant scope).</li>
     * </ul>
     *
     * EV-BUG-01 (2026-04-29): added sourceComponent exemption for internal emitters.
     * Long-term: propagate tenantAlias through HeimdallEmitter.build() — tracked in Sprint 5.
     */
    static boolean requiresTenantTag(String eventType, String sourceComponent) {
        if (eventType == null) return false;
        if (eventType.startsWith("seer.platform.")) return false;
        if (eventType.startsWith("seer.audit."))    return false;
        // Internal background components are single-tenant per job; sessionId = tenant key.
        if ("hound".equals(sourceComponent))   return false;
        if ("dali".equals(sourceComponent))    return false;
        if ("shuttle".equals(sourceComponent)) return false;
        // Chur auth events (AUTH_LOGIN_SUCCESS, AUTH_LOGOUT, etc.) have no tenantAlias at
        // emission time (pre-session or cross-tenant boundary). Exempt by sourceComponent.
        if ("chur".equals(sourceComponent))    return false;
        return true;
    }

    /** @deprecated Use {@link #requiresTenantTag(String, String)} — kept for test compatibility. */
    @Deprecated
    static boolean requiresTenantTag(String eventType) {
        return requiresTenantTag(eventType, null);
    }

    static boolean hasTenantTag(HeimdallEvent event) {
        Map<String, Object> payload = event.payload();
        if (payload == null) return false;
        Object v = payload.get("tenantAlias");
        return v instanceof String s && !s.isBlank();
    }

    @POST
    public Response ingest(@Valid @NotNull HeimdallEvent event) {

        if (requiresTenantTag(event.eventType(), event.sourceComponent()) && !hasTenantTag(event)) {
            String correlationId = event.correlationId() != null
                    ? event.correlationId() : UUID.randomUUID().toString();
            LOG.warnf("[HTA-14] rejected event %s from %s — missing tenant tag (corr=%s)",
                    event.eventType(), event.sourceComponent(), correlationId);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "missing_tenant_tag",
                            "correlationId", correlationId,
                            "eventType", event.eventType()))
                    .build();
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
        tenantMetrics.record(enriched);
        uxAggregator.record(enriched);
        LOG.debugf("Ingested event: %s from %s", enriched.eventType(), enriched.sourceComponent());
        return Response.accepted().build();
    }

    /** HB-batch-valid: max events in one batch. Prevents DoS via oversized payloads. */
    static final int BATCH_MAX_SIZE = 100;

    @POST
    @Path("/batch")
    public Response ingestBatch(@NotNull @Valid List<@Valid HeimdallEvent> events) {

        // HB-batch-valid: reject whole batch on structural issues (too large / malformed)
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

        // HTA-14: skip untagged events (warn, don't reject the whole batch)
        long rejected = events.stream()
                .filter(e -> requiresTenantTag(e.eventType(), e.sourceComponent()) && !hasTenantTag(e))
                .count();
        if (rejected > 0) {
            LOG.warnf("[HTA-14] batch skipped %d of %d events — missing tenant tag",
                    rejected, events.size());
        }

        long count = events.stream()
                .filter(e -> !requiresTenantTag(e.eventType(), e.sourceComponent()) || hasTenantTag(e))
                .peek(e -> { ringBuffer.push(e); metricsCollector.record(e); tenantMetrics.record(e); uxAggregator.record(e); })
                .count();

        LOG.debugf("Ingested batch of %d events (skipped %d untagged)", count, rejected);
        return Response.accepted().build();
    }
}
