package com.mimir.heimdall;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import studio.seer.shared.EventLevel;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MC-05 emitter tests: 9 typed methods → правильные EventType + payload + sourceComponent="mimir".
 * Fire-and-forget: failures swallowed, no exceptions thrown.
 */
@QuarkusTest
class MimirEventEmitterTest {

    @Inject MimirEventEmitter emitter;

    @InjectMock @RestClient HeimdallClient client;

    @Test
    void queryReceivedEmitsCorrectEvent() {
        when(client.ingest(any())).thenReturn(Uni.createFrom().voidItem());

        emitter.queryReceived("session-1", "acme", 42, "deepseek");

        ArgumentCaptor<HeimdallEvent> captor = ArgumentCaptor.forClass(HeimdallEvent.class);
        verify(client).ingest(captor.capture());
        HeimdallEvent ev = captor.getValue();
        assertThat(ev.sourceComponent()).isEqualTo("mimir");
        assertThat(ev.eventType()).isEqualTo(EventType.QUERY_RECEIVED.name());
        assertThat(ev.level()).isEqualTo(EventLevel.INFO);
        assertThat(ev.sessionId()).isEqualTo("session-1");
        assertThat(ev.payload()).containsEntry("tenant_alias", "acme");
        assertThat(ev.payload()).containsEntry("question_length", 42);
        assertThat(ev.payload()).containsEntry("model", "deepseek");
    }

    @Test
    void modelSelectedEvent() {
        when(client.ingest(any())).thenReturn(Uni.createFrom().voidItem());

        emitter.modelSelected("session-1", "deepseek", "default");

        ArgumentCaptor<HeimdallEvent> captor = ArgumentCaptor.forClass(HeimdallEvent.class);
        verify(client).ingest(captor.capture());
        HeimdallEvent ev = captor.getValue();
        assertThat(ev.eventType()).isEqualTo(EventType.MODEL_SELECTED.name());
        assertThat(ev.payload()).containsEntry("model", "deepseek").containsEntry("reason", "default");
    }

    @Test
    void toolCallStartedAndCompleted() {
        when(client.ingest(any())).thenReturn(Uni.createFrom().voidItem());

        emitter.toolCallStarted("s1", "search_nodes", java.util.Map.of("pattern", "ORDERS"));
        emitter.toolCallCompleted("s1", "search_nodes", 145, 3);

        ArgumentCaptor<HeimdallEvent> captor = ArgumentCaptor.forClass(HeimdallEvent.class);
        verify(client, times(2)).ingest(captor.capture());

        HeimdallEvent started = captor.getAllValues().get(0);
        assertThat(started.eventType()).isEqualTo(EventType.TOOL_CALL_STARTED.name());
        assertThat(started.payload()).containsEntry("tool_name", "search_nodes");

        HeimdallEvent completed = captor.getAllValues().get(1);
        assertThat(completed.eventType()).isEqualTo(EventType.TOOL_CALL_COMPLETED.name());
        assertThat(completed.payload()).containsEntry("duration_ms", 145L);
        assertThat(completed.payload()).containsEntry("result_size", 3);
    }

    @Test
    void fallbackActivatedIsWarn() {
        when(client.ingest(any())).thenReturn(Uni.createFrom().voidItem());

        emitter.fallbackActivated("s1", "429", "unavailable");

        ArgumentCaptor<HeimdallEvent> captor = ArgumentCaptor.forClass(HeimdallEvent.class);
        verify(client).ingest(captor.capture());
        HeimdallEvent ev = captor.getValue();
        assertThat(ev.eventType()).isEqualTo(EventType.FALLBACK_ACTIVATED.name());
        assertThat(ev.level()).isEqualTo(EventLevel.WARN);
        assertThat(ev.payload()).containsEntry("reason", "429");
    }

    @Test
    void emitFailureIsSwallowed() {
        // Simulate HEIMDALL down — Mutiny failure
        when(client.ingest(any())).thenReturn(Uni.createFrom().failure(new RuntimeException("HEIMDALL down")));

        // No exception should escape
        emitter.queryReceived("s1", "acme", 10, "deepseek");

        // Verify ingest was attempted
        verify(client).ingest(any(HeimdallEvent.class));
    }
}
