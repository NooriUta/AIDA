package studio.seer.heimdall;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RingBufferTest {

    @Inject
    RingBuffer ringBuffer;

    private HeimdallEvent event(EventType type) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "test", type.name(),
                EventLevel.INFO, null, null, null, 0, Map.of());
    }

    @BeforeEach
    void reset() {
        // Очищаем буфер перед каждым тестом
        ringBuffer.clear();
    }

    // ── subscribe / unsubscribe ────────────────────────────────────────────────

    @Test
    void subscriberReceivesEventsAfterSubscribe() {
        AtomicInteger count = new AtomicInteger();
        Consumer<HeimdallEvent> sub = e -> count.incrementAndGet();

        ringBuffer.subscribe(sub);
        ringBuffer.push(event(EventType.ATOM_EXTRACTED));
        ringBuffer.push(event(EventType.ATOM_EXTRACTED));

        // 2 явных события + 1 DEMO_RESET от BeforeEach.clear() не считается
        // (подписка произошла ПОСЛЕ clear)
        assertEquals(2, count.get());

        ringBuffer.unsubscribe(sub);
    }

    @Test
    void unsubscribeStopsDelivery() {
        AtomicInteger count = new AtomicInteger();
        Consumer<HeimdallEvent> sub = e -> count.incrementAndGet();

        ringBuffer.subscribe(sub);
        ringBuffer.push(event(EventType.FILE_PARSING_STARTED));
        ringBuffer.unsubscribe(sub);
        ringBuffer.push(event(EventType.FILE_PARSING_COMPLETED));  // должен не дойти

        assertEquals(1, count.get());
    }

    // ── snapshot ───────────────────────────────────────────────────────────────

    @Test
    void snapshotIsImmutable() {
        ringBuffer.push(event(EventType.SESSION_STARTED));
        List<HeimdallEvent> snap = ringBuffer.snapshot();

        // Snapshot должен быть unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> snap.add(event(EventType.SESSION_COMPLETED)));
    }

    @Test
    void snapshotContainsAllPushedEvents() {
        // BeforeEach добавил DEMO_RESET — snapshot contains it
        int before = ringBuffer.snapshot().size();

        ringBuffer.push(event(EventType.ATOM_EXTRACTED));
        ringBuffer.push(event(EventType.RESOLUTION_COMPLETED));

        assertEquals(before + 2, ringBuffer.snapshot().size());
    }

    // ── since ──────────────────────────────────────────────────────────────────

    @Test
    void sinceReturnsOnlyEventsAfterTimestamp() throws InterruptedException {
        long before = System.currentTimeMillis();
        Thread.sleep(1);  // убеждаемся что timestamp будет строго больше

        ringBuffer.push(event(EventType.FILE_PARSING_STARTED));

        List<HeimdallEvent> result = ringBuffer.since(before);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(e -> e.timestamp() >= before));
    }

    @Test
    void sinceReturnsEmptyForFutureTimestamp() {
        ringBuffer.push(event(EventType.FILE_PARSING_STARTED));
        long future = System.currentTimeMillis() + 60_000;

        assertTrue(ringBuffer.since(future).isEmpty());
    }

    // ── clear ──────────────────────────────────────────────────────────────────

    @Test
    void clearResetsBufferAndAddsDemoResetEvent() {
        ringBuffer.push(event(EventType.SESSION_STARTED));
        ringBuffer.push(event(EventType.WORKER_ASSIGNED));

        List<HeimdallEvent> before = ringBuffer.snapshot();
        assertTrue(before.size() >= 2);

        ringBuffer.clear();

        List<HeimdallEvent> after = ringBuffer.snapshot();
        assertEquals(1, after.size());
        assertEquals(EventType.DEMO_RESET.name(), after.get(0).eventType());
        assertEquals("heimdall", after.get(0).sourceComponent());
    }

    // ── multi-subscriber ───────────────────────────────────────────────────────

    @Test
    void multipleSubscribersAllReceiveEvents() {
        List<HeimdallEvent> received1 = new ArrayList<>();
        List<HeimdallEvent> received2 = new ArrayList<>();

        Consumer<HeimdallEvent> sub1 = received1::add;
        Consumer<HeimdallEvent> sub2 = received2::add;

        ringBuffer.subscribe(sub1);
        ringBuffer.subscribe(sub2);

        ringBuffer.push(event(EventType.JOB_COMPLETED));

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());

        ringBuffer.unsubscribe(sub1);
        ringBuffer.unsubscribe(sub2);
    }
}
