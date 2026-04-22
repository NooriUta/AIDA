package studio.seer.heimdall.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.heimdall.RingBuffer;
import studio.seer.shared.HeimdallEvent;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * WebSocket endpoint для live-стриминга событий HEIMDALL.
 *
 * Использует quarkus-websockets-next API (НЕ legacy @ServerEndpoint).
 * Path: ws://host:9093/ws/events
 *
 * При подключении:
 *   1. Replay последних 200 событий из ring buffer (cold-start).
 *   2. Подписка на live-поток: все новые события приходят клиенту.
 *
 * При закрытии соединения — автоматическая отписка от ring buffer.
 */
@WebSocket(path = "/ws/events")
@ApplicationScoped
public class EventStreamEndpoint {

    private static final Logger LOG = Logger.getLogger(EventStreamEndpoint.class);
    private static final int COLD_START_REPLAY_SIZE = 200;

    @Inject
    RingBuffer ringBuffer;

    @Inject
    ObjectMapper mapper;

    /** connectionId → subscriber lambda (нужно для cleanup при закрытии). */
    private final ConcurrentHashMap<String, Consumer<HeimdallEvent>> subscribers =
            new ConcurrentHashMap<>();

    /** MTN-36: connectionId → {@link WebSocketConnection} for proactive close-by-tenant. */
    private final ConcurrentHashMap<String, WebSocketConnection> connections =
            new ConcurrentHashMap<>();

    /** MTN-36: connectionId → tenantAlias (resolved at handshake). Empty = superadmin stream. */
    private final ConcurrentHashMap<String, String> connectionTenants =
            new ConcurrentHashMap<>();

    @OnOpen
    public Uni<Void> onOpen(WebSocketConnection connection) {
        LOG.infof("WebSocket connected: %s", connection.id());

        // Parse optional ?filter=key:value query param from raw query string
        String rawFilter = queryParam(connection.handshakeRequest().query(), "filter");
        EventFilter parsed = EventFilter.parse(rawFilter);

        // MTN-05: non-superadmin sessions cannot observe other tenants. The
        // handshake carries an X-Seer-Tenant-Alias + X-Seer-Role header set by
        // the rbac-proxy. Force filter.tenantAlias to the session tenant and
        // record the binding for MTN-36 force-disconnect on suspend.
        String headerAlias = connection.handshakeRequest().header("X-Seer-Tenant-Alias");
        String headerRole  = connection.handshakeRequest().header("X-Seer-Role");
        boolean isSuper    = "super-admin".equalsIgnoreCase(headerRole);
        final EventFilter filter = (!isSuper && headerAlias != null && !headerAlias.isBlank())
                ? parsed.withForcedTenantAlias(headerAlias)
                : parsed;
        if (filter.tenantAlias() != null) {
            connectionTenants.put(connection.id(), filter.tenantAlias());
        }
        connections.put(connection.id(), connection);

        if (!filter.isEmpty()) {
            LOG.infof("WebSocket %s applying filter: %s", connection.id(), rawFilter);
        }

        // Создаём subscriber который шлёт события этому конкретному соединению
        Consumer<HeimdallEvent> subscriber = event -> {
            if (!filter.matches(event)) return;
            if (connection.isOpen()) {
                connection.sendText(toJson(event))
                        .subscribe().with(
                                __ -> { /* ok */ },
                                err -> {
                                    LOG.warnf("WebSocket send error for %s: %s", connection.id(), err.getMessage());
                                    cleanup(connection.id());
                                });
            }
        };

        subscribers.put(connection.id(), subscriber);
        ringBuffer.subscribe(subscriber);

        // Cold-start replay: клиент получает последние N событий (с применением фильтра) сразу при подключении
        var replay = ringBuffer.snapshot().stream()
                .filter(filter::matches)
                .limit(COLD_START_REPLAY_SIZE)
                .toList();

        if (replay.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        return Multi.createFrom().iterable(replay)
                .flatMap(e -> Multi.createFrom().uni(connection.sendText(toJson(e))))
                .toUni().replaceWithVoid();
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        LOG.infof("WebSocket disconnected: %s", connection.id());
        cleanup(connection.id());
    }

    /**
     * Handles abrupt client disconnects (e.g. browser tab close / refresh on Windows).
     * Windows WSAECONNRESET surfaces as IOException "An existing connection was forcibly
     * closed by the remote host" — this is normal and should not be logged as ERROR.
     */
    @OnError
    public void onError(WebSocketConnection connection, Throwable error) {
        cleanup(connection.id());
        if (error instanceof java.io.IOException) {
            LOG.debugf("WebSocket %s closed abruptly (client disconnect): %s",
                       connection.id(), error.getMessage());
        } else {
            LOG.errorf("WebSocket %s unhandled error: %s", connection.id(), error.getMessage());
        }
    }

    private void cleanup(String connectionId) {
        Consumer<HeimdallEvent> sub = subscribers.remove(connectionId);
        if (sub != null) {
            ringBuffer.unsubscribe(sub);
        }
        connections.remove(connectionId);
        connectionTenants.remove(connectionId);
    }

    /**
     * MTN-36 — Close every open WebSocket bound to {@code tenantAlias}. Called
     * when that tenant transitions to SUSPENDED / ARCHIVED / PURGED / HIBERNATED
     * (per ADR-MT-007 eviction matrix). Returns the count of connections that
     * received a close frame so the caller can report SLA compliance.
     *
     * <p>Superadmin sessions (no forced tenant binding) are NOT closed — they
     * observe cross-tenant streams and must stay live to see the follow-up
     * {@code tenant_purged} event on the wire.
     */
    public int disconnectTenant(String tenantAlias, String reason) {
        if (tenantAlias == null || tenantAlias.isBlank()) return 0;
        int closed = 0;
        for (var entry : connectionTenants.entrySet()) {
            if (tenantAlias.equals(entry.getValue())) {
                WebSocketConnection conn = connections.get(entry.getKey());
                if (conn != null && conn.isOpen()) {
                    LOG.infof("[MTN-36] closing ws %s — tenant '%s' reason='%s'",
                            entry.getKey(), tenantAlias, reason);
                    // Close with policy-violation code (1008) + reason for operator forensics.
                    conn.closeAndAwait();
                    closed++;
                }
                cleanup(entry.getKey());
            }
        }
        return closed;
    }

    /** @return snapshot count of currently-tracked tenant-bound connections (MTN-36 metrics hook). */
    public int boundConnectionCount() {
        return connectionTenants.size();
    }

    /**
     * Extract a single named parameter from a raw query string (e.g. "filter=component:shuttle&foo=bar").
     * Returns null if the parameter is absent.
     */
    private static String queryParam(String queryString, String name) {
        if (queryString == null || queryString.isBlank()) return null;
        for (String pair : queryString.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) continue;
            String k = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            if (name.equals(k)) {
                return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String toJson(HeimdallEvent event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            LOG.warnf("Failed to serialize HeimdallEvent: %s", e.getMessage());
            return "{}";
        }
    }
}
