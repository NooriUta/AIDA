package studio.seer.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * In-memory ring buffer для хранения последних N событий HEIMDALL.
 *
 * Ёмкость: 10 000 событий (настраивается через heimdall.ring-buffer.capacity).
 * Когда буфер заполнен — самое старое событие вытесняется (evict oldest).
 *
 * Thread-safety: push/snapshot/since/clear — synchronized на buffer.
 * Подписчики (subscribe/unsubscribe) используют CopyOnWriteArrayList —
 * итерируются без блокировки, что важно для WebSocket нотификаций.
 */
@ApplicationScoped
public class RingBuffer {

    @ConfigProperty(name = "heimdall.ring-buffer.capacity", defaultValue = "10000")
    int capacity;

    private final ArrayDeque<HeimdallEvent> buffer      = new ArrayDeque<>();
    private final List<Consumer<HeimdallEvent>> subscribers = new CopyOnWriteArrayList<>();

    /** Добавить событие в буфер и уведомить всех подписчиков. */
    public synchronized void push(HeimdallEvent event) {
        if (buffer.size() >= capacity) buffer.pollFirst();   // evict oldest
        buffer.addLast(event);
        subscribers.forEach(s -> s.accept(event));
    }

    /** Вернуть immutable snapshot всего буфера (для cold-start replay). */
    public synchronized List<HeimdallEvent> snapshot() {
        return List.copyOf(buffer);
    }

    /** Вернуть события начиная с timestampMs (inclusive). */
    public synchronized List<HeimdallEvent> since(long timestampMs) {
        return buffer.stream()
                .filter(e -> e.timestamp() >= timestampMs)
                .toList();
    }

    /**
     * Очистить буфер и записать DEMO_RESET-событие.
     * Вызывается из ControlResource при demo reset.
     */
    public synchronized void clear() {
        buffer.clear();
        push(HeimdallEvent.internal(EventType.DEMO_RESET, "Ring buffer cleared"));
    }

    /** Подписаться на live-поток событий (вызывается из WebSocket endpoint). */
    public void subscribe(Consumer<HeimdallEvent> subscriber) {
        subscribers.add(subscriber);
    }

    /** Отписаться при закрытии WebSocket-соединения. */
    public void unsubscribe(Consumer<HeimdallEvent> subscriber) {
        subscribers.remove(subscriber);
    }
}
